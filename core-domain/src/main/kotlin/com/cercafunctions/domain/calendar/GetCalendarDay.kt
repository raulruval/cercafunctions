package com.cercafunctions.domain.calendar

import java.time.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class GetCalendarDay(private val repository:CalendarRepository) {
    fun hasReadAccess():Boolean=repository.hasReadAccess()
    suspend operator fun invoke(date:LocalDate,zone:ZoneId):CalendarRead {
        currentCoroutineContext().ensureActive()
        if(!hasReadAccess())return CalendarRead.PermissionRequired
        val day=CalendarDay(date,zone)
        if(day.start==day.end)return CalendarRead.Data(emptyList())
        val read=repository.read(day)
        currentCoroutineContext().ensureActive()
        if(!hasReadAccess())return CalendarRead.PermissionRequired
        if(read !is CalendarRead.Data)return read
        val items=read.occurrences.filter {day.contains(it.key.begin)}
        if(items.map {it.key}.toSet().size!=items.size)return CalendarRead.Unavailable(CalendarFailure.INVALID_DATA)
        return CalendarRead.Data(items.sortedWith(compareBy<CalendarOccurrence>{it.key.begin}.thenBy{it.key.eventId}))
    }
}
