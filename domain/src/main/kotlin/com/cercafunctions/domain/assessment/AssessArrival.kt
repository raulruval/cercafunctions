package com.cercafunctions.domain.assessment

import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.calendar.*
import java.time.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Read-only composition of existing capabilities. No UI state or calendar titles escape. */
class AssessArrival(private val query:QueryJourneyArrival,private val revalidate:RevalidateCalendarSelection,private val clock:Clock) {
    suspend operator fun invoke(estimate:ArrivalEstimate?,selection:CalendarSelection?):AssessmentResult {
        currentCoroutineContext().ensureActive()
        fun unknown(reason:AssessmentReason,evidence:ArrivalEstimate?=estimate)=
            AssessmentResult.Indeterminate(reason,evidence,clock.instant())
        if(estimate==null)return unknown(AssessmentReason.MISSING_ARRIVAL)
        if(selection==null)return unknown(AssessmentReason.MISSING_SELECTION)
        val departure=estimate.departure.atZone(estimate.zone)
        val refreshed=query(DepartureInput(departure.toLocalDateTime(),estimate.zone,departure.offset),estimate.scenario)
        if(refreshed==ArrivalResult.PastDeparture)return unknown(AssessmentReason.DEPARTURE_PASSED)
        if(refreshed !is ArrivalResult.Estimated)return unknown(AssessmentReason.ARRIVAL_UNAVAILABLE)
        val fresh=refreshed.value
        if(fresh.copy(observedAt=estimate.observedAt)!=estimate)return unknown(AssessmentReason.ARRIVAL_CHANGED)
        if(fresh.arrival==null)return unknown(AssessmentReason.UNKNOWN_ARRIVAL,fresh)
        val meeting=when(val result=revalidate(selection)) {
            is SelectionValidation.Valid -> result.occurrence
            SelectionValidation.MissingOrChanged -> return unknown(AssessmentReason.CALENDAR_CHANGED,fresh)
            SelectionValidation.PermissionRequired -> return unknown(AssessmentReason.CALENDAR_PERMISSION,fresh)
            is SelectionValidation.Unavailable -> return unknown(AssessmentReason.CALENDAR_UNAVAILABLE,fresh)
        }
        currentCoroutineContext().ensureActive()
        return AssessmentResult.Compared(requireNotNull(compareArrivalTiming(fresh,
            MeetingTiming(meeting.key.begin,meeting.end,selection.day.zone),clock.instant())))
    }
}
