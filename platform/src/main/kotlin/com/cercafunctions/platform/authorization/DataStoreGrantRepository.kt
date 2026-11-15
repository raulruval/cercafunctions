package com.cercafunctions.platform.authorization
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.cercafunctions.domain.authorization.*
import com.cercafunctions.domain.calendar.*
import java.time.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Private minimal grant; raw reference and readable calendar fields never enter Preferences. */
class DataStoreGrantRepository(private val store:DataStore<Preferences>,override val available:Boolean,
    private val configureSystem:suspend(Boolean)->Unit):GrantRepository {
    private val mutex=Mutex()
    override suspend fun read():GrantRecord?=decode(store.data.first())
    override suspend fun publish(grant:GrantRecord):Unit=mutex.withLock {
        if(!available)throw UnsupportedOperationException("Assessment sharing unavailable")
        configureSystem(true)
        store.edit{p->
            clear(p)
            p[HASH]=grant.referenceHash;p[VERSION]=grant.versionHmac
            p[EVENT]=grant.selection.key.eventId;p[BEGIN]=grant.selection.key.begin.toString();p[END]=grant.selection.expectedEnd.toString()
            p[DAY]=grant.selection.day.date.toString();p[ZONE]=grant.selection.day.zone.id
            p[ISSUED]=grant.issued.wall.toString();p[EXPIRES]=grant.expiresAt.toString()
            p[ELAPSED]=grant.issued.elapsedMillis;p[DEADLINE]=grant.expiresElapsedMillis;p[BOOT]=grant.issued.bootCount
        }
        Unit
    }
    override suspend fun revokeIf(referenceHash:String):Unit=mutex.withLock {
        var removed=false
        store.edit{p->if(p[HASH]==referenceHash){clear(p);removed=true}}
        if(removed && available)configureSystem(false)
    }
    override suspend fun revokeAll():Unit=mutex.withLock {
        store.edit{clear(it)}
        if(available)configureSystem(false)
    }
    private fun decode(p:Preferences):GrantRecord?=try {
        val hash=p[HASH]
        if(hash==null) {
            if(KEYS.any{p.contains(it)})throw AssessmentAccessDenied()
            null
        } else GrantRecord(hash,requireNotNull(p[VERSION]),
            CalendarSelection(OccurrenceKey(requireNotNull(p[EVENT]),Instant.parse(requireNotNull(p[BEGIN]))),
                Instant.parse(requireNotNull(p[END])),CalendarDay(LocalDate.parse(requireNotNull(p[DAY])),ZoneId.of(requireNotNull(p[ZONE])))),
            GrantTime(Instant.parse(requireNotNull(p[ISSUED])),requireNotNull(p[ELAPSED]),requireNotNull(p[BOOT])),
            Instant.parse(requireNotNull(p[EXPIRES])),requireNotNull(p[DEADLINE]))
    } catch(_:Exception){throw AssessmentAccessDenied()}
    private fun clear(p:MutablePreferences){KEYS.forEach{p.remove(it)}}
    private companion object {
        val HASH=stringPreferencesKey("assessment_reference_hash")
        val VERSION=stringPreferencesKey("assessment_version_hmac")
        val EVENT=longPreferencesKey("assessment_event_id")
        val BEGIN=stringPreferencesKey("assessment_begin")
        val END=stringPreferencesKey("assessment_end")
        val DAY=stringPreferencesKey("assessment_day")
        val ZONE=stringPreferencesKey("assessment_zone")
        val ISSUED=stringPreferencesKey("assessment_issued")
        val EXPIRES=stringPreferencesKey("assessment_expires")
        val ELAPSED=longPreferencesKey("assessment_elapsed")
        val DEADLINE=longPreferencesKey("assessment_deadline")
        val BOOT=intPreferencesKey("assessment_boot")
        val KEYS=listOf(HASH,VERSION,EVENT,BEGIN,END,DAY,ZONE,ISSUED,EXPIRES,ELAPSED,DEADLINE,BOOT)
    }
}
