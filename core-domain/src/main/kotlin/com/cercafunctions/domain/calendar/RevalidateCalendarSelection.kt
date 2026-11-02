package com.cercafunctions.domain.calendar

class RevalidateCalendarSelection(private val getDay:GetCalendarDay) {
    suspend operator fun invoke(selection:CalendarSelection):SelectionValidation =
        when(val read=getDay(selection.day.date,selection.day.zone)) {
            CalendarRead.PermissionRequired -> SelectionValidation.PermissionRequired
            is CalendarRead.Unavailable -> SelectionValidation.Unavailable(read.reason)
            is CalendarRead.Data -> {
                val current=read.occurrences.singleOrNull {it.key==selection.key}
                if(current==null || current.end!=selection.expectedEnd) SelectionValidation.MissingOrChanged
                else SelectionValidation.Valid(current)
            }
        }
}
