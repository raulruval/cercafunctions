package com.cercafunctions.domain.calendar

import java.time.*

/** Local calendar day, including DST transitions. Never assumes 24 elapsed hours. */
data class CalendarDay(val date:LocalDate,val zone:ZoneId) {
    val start:Instant=date.atStartOfDay(zone).toInstant()
    val end:Instant=date.plusDays(1).atStartOfDay(zone).toInstant()
    fun contains(instant:Instant):Boolean=instant>=start && instant<end
}
data class OccurrenceKey(val eventId:Long,val begin:Instant) { init { require(eventId>0) } }
/** Title and calendar name are local selection details, not an agent disclosure DTO. */
data class CalendarOccurrence(val key:OccurrenceKey,val end:Instant,val title:String,val calendarName:String) {
    init {require(end>=key.begin)}
}
/** Local session selection. Explicit UC-007 grants may persist its minimal fields, never readable details. */
data class CalendarSelection(val key:OccurrenceKey,val expectedEnd:Instant,val day:CalendarDay) {
    init {require(day.contains(key.begin));require(expectedEnd>=key.begin)}
}
enum class CalendarFailure { PROVIDER_UNAVAILABLE, QUERY_FAILED, INVALID_DATA }
sealed interface CalendarRead {
    data class Data(val occurrences:List<CalendarOccurrence>):CalendarRead
    data object PermissionRequired:CalendarRead
    data class Unavailable(val reason:CalendarFailure):CalendarRead
}
interface CalendarRepository {
    fun hasReadAccess():Boolean
    /** Only timed, non-cancelled occurrences in visible calendars. Never requests permission. */
    suspend fun read(day:CalendarDay):CalendarRead
}
sealed interface SelectionValidation {
    data class Valid(val occurrence:CalendarOccurrence):SelectionValidation
    data object MissingOrChanged:SelectionValidation
    data object PermissionRequired:SelectionValidation
    data class Unavailable(val reason:CalendarFailure):SelectionValidation
}
