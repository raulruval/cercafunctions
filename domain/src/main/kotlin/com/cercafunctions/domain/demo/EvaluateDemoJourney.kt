package com.cercafunctions.domain.demo

import com.cercafunctions.domain.assessment.*
import com.cercafunctions.domain.calendar.*
import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.journey.HabitualJourney
import java.time.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Local titles stay in this model, never in the AppFunction response. */
data class DemoEvaluation(val evidence:ArrivalEstimate,val occurrence:CalendarOccurrence,val comparison:ComparedArrival?)
sealed interface DemoResult {
    data class Ready(val value:DemoEvaluation):DemoResult
    data object PermissionRequired:DemoResult
    data class NoMeeting(val date:LocalDate,val zone:ZoneId):DemoResult
    data object CalendarUnavailable:DemoResult
}

/** Real next calendar meeting tomorrow, with an explicitly synthetic train aligned to its start. */
class EvaluateDemoJourney(private val calendar:GetCalendarDay,private val settings:DemoSettingsRepository,
    private val clock:Clock,private val zone:ZoneId) {
    fun tomorrow():LocalDate=clock.instant().atZone(zone).toLocalDate().plusDays(1)
    fun hasCalendarAccess():Boolean=calendar.hasReadAccess()
    suspend operator fun invoke():DemoResult {
        currentCoroutineContext().ensureActive()
        val scenario=settings.get().scenario
        val day=tomorrow()
        val occurrence=when(val result=calendar(day,zone)) {
            CalendarRead.PermissionRequired -> return DemoResult.PermissionRequired
            is CalendarRead.Unavailable -> return DemoResult.CalendarUnavailable
            is CalendarRead.Data -> result.occurrences.firstOrNull() ?: return DemoResult.NoMeeting(day,zone)
        }
        // This controlled input demonstrates all outcomes without editing the real calendar.
        val duration=35
        val departure=occurrence.key.begin.minusSeconds(duration*60L)
        val delay=when(scenario){DemoScenario.ON_TIME->0;DemoScenario.DELAYED->15;DemoScenario.UNKNOWN->null}
        val evidence=ArrivalEstimate(HabitualJourney("Atocha (demo)","Chamartín (demo)",duration),departure,
            delay?.let{departure.plusSeconds((duration+it)*60L)},zone,ArrivalSource.FIXTURE,
            when(scenario){DemoScenario.ON_TIME->ArrivalScenario.DEMO_NORMAL;DemoScenario.DELAYED->ArrivalScenario.DEMO_DELAYED;DemoScenario.UNKNOWN->ArrivalScenario.DEMO_UNKNOWN},
            clock.instant(),delay)
        currentCoroutineContext().ensureActive()
        if(!calendar.hasReadAccess())return DemoResult.PermissionRequired
        return DemoResult.Ready(DemoEvaluation(evidence,occurrence,compareArrivalTiming(evidence,
            MeetingTiming(occurrence.key.begin,occurrence.end,zone),clock.instant())))
    }
}
