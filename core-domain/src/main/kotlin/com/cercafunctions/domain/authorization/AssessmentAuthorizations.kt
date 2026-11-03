package com.cercafunctions.domain.authorization
import com.cercafunctions.domain.calendar.*
import java.time.Duration
import kotlinx.coroutines.CancellationException

/** Single-occurrence disclosure policy. No Android, UI, calendar text storage or token recovery. */
class AssessmentAuthorizations(private val repository:GrantRepository,private val validate:RevalidateCalendarSelection,
    private val calendar:GetCalendarDay,private val clock:GrantClock) {
    val available get()=repository.available
    private suspend fun <T> guarded(block:suspend()->T):T=try{block()}
        catch(cancelled:CancellationException){throw cancelled}
        catch(denied:AssessmentAccessDenied){throw denied}
        catch(_:Exception){throw AssessmentAccessDenied()}
    private fun validTime(grant:GrantRecord):Boolean {
        val now=clock.now()
        if(now.bootCount!=grant.issued.bootCount || now.elapsedMillis<grant.issued.elapsedMillis ||
            now.elapsedMillis>=grant.expiresElapsedMillis || now.wall<grant.issued.wall || now.wall>=grant.expiresAt)return false
        val predicted=grant.issued.wall.plusMillis(now.elapsedMillis-grant.issued.elapsedMillis)
        return Duration.between(predicted,now.wall).abs()<=Duration.ofSeconds(5)
    }
    private suspend fun reject(grant:GrantRecord):Nothing {
        repository.revokeIf(grant.referenceHash)
        throw AssessmentAccessDenied()
    }
    suspend fun issue(target:GrantTarget):IssuedGrant=guarded {
        repository.revokeAll()
        if(!available || !calendar.hasReadAccess())throw AssessmentAccessDenied()
        val event=(validate(target.selection) as? SelectionValidation.Valid)?.occurrence ?: throw AssessmentAccessDenied()
        if(event!=target.expected)throw AssessmentAccessDenied()
        val now=clock.now()
        if(now.bootCount<0 || now.elapsedMillis<0 || event.key.begin<=now.wall)throw AssessmentAccessDenied()
        val expiry=minOf(now.wall.plusSeconds(1800),event.key.begin)
        val reference=ReferenceCrypto.create()
        val record=GrantRecord(ReferenceCrypto.hash(reference),ReferenceCrypto.version(reference,event),target.selection,
            now,expiry,Math.addExact(now.elapsedMillis,Duration.between(now.wall,expiry).toMillis()))
        repository.publish(record)
        // Recheck observed version and current authorization after all publication IO.
        resolve(reference)
        IssuedGrant(reference,expiry,event.key.begin)
    }
    suspend fun status():GrantSummary?=guarded {
        val grant=repository.read() ?: return@guarded null
        if(!calendar.hasReadAccess() || !validTime(grant)) {
            repository.revokeIf(grant.referenceHash);return@guarded null
        }
        GrantSummary(grant.expiresAt,grant.selection.key.begin)
    }
    suspend fun resolve(reference:String):GrantRecord=guarded {
        if(!ReferenceCrypto.valid(reference))throw AssessmentAccessDenied()
        val grant=repository.read() ?: throw AssessmentAccessDenied()
        if(!ReferenceCrypto.equal(grant.referenceHash,ReferenceCrypto.hash(reference)))throw AssessmentAccessDenied()
        if(!validTime(grant) || !calendar.hasReadAccess())reject(grant)
        val event=(validate(grant.selection) as? SelectionValidation.Valid)?.occurrence ?: reject(grant)
        if(!ReferenceCrypto.equal(grant.versionHmac,ReferenceCrypto.version(reference,event)))reject(grant)
        // Revocation or replacement can happen while the provider query suspends.
        if(repository.read()!=grant)throw AssessmentAccessDenied()
        if(!calendar.hasReadAccess() || !validTime(grant))reject(grant)
        grant
    }
    suspend fun revoke():Unit=guarded{repository.revokeAll()}
    suspend fun invalidate(reference:String):Unit=guarded {
        if(ReferenceCrypto.valid(reference))repository.revokeIf(ReferenceCrypto.hash(reference))
    }
}
