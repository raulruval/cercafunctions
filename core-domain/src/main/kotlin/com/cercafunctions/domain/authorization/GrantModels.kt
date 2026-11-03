package com.cercafunctions.domain.authorization
import com.cercafunctions.domain.calendar.*
import java.time.*
data class GrantTime(val wall:Instant,val elapsedMillis:Long,val bootCount:Int)
fun interface GrantClock { fun now():GrantTime }
data class GrantTarget(val selection:CalendarSelection,val expected:CalendarOccurrence)
data class GrantRecord(val referenceHash:String,val versionHmac:String,val selection:CalendarSelection,
    val issued:GrantTime,val expiresAt:Instant,val expiresElapsedMillis:Long) {
    init {
        require(Regex("[0-9a-f]{64}").matches(referenceHash));require(Regex("[0-9a-f]{64}").matches(versionHmac))
        require(issued.bootCount>=0 && issued.elapsedMillis>=0)
        require(expiresAt>issued.wall && expiresAt<=issued.wall.plusSeconds(1800) && expiresAt<=selection.key.begin)
        require(expiresElapsedMillis>issued.elapsedMillis && expiresElapsedMillis-issued.elapsedMillis<=1_800_000)
        require(expiresElapsedMillis-issued.elapsedMillis==Duration.between(issued.wall,expiresAt).toMillis())
    }
}
data class IssuedGrant(val reference:String,val expiresAt:Instant,val meetingStart:Instant)
data class GrantSummary(val expiresAt:Instant,val meetingStart:Instant)
interface GrantRepository {
    val available:Boolean
    suspend fun read():GrantRecord?
    suspend fun publish(grant:GrantRecord)
    suspend fun revokeIf(referenceHash:String)
    suspend fun revokeAll()
}
class AssessmentAccessDenied:IllegalStateException("Assessment authorization is unavailable.")
class InvalidAssessmentDeparture:IllegalArgumentException("Invalid assessment departure.")
