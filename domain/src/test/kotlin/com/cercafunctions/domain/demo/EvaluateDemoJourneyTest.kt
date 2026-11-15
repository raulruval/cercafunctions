package com.cercafunctions.domain.demo

import com.cercafunctions.domain.calendar.*
import com.cercafunctions.domain.assessment.ComparisonStatus
import java.time.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class EvaluateDemoJourneyTest {
    private val zone=ZoneId.of("Europe/Madrid")
    private val clock=Clock.fixed(Instant.parse("2026-10-24T22:30:00Z"),ZoneOffset.UTC)
    private val day=LocalDate.parse("2026-10-26")
    private val begin=day.atTime(9,0).atZone(zone).toInstant()
    private val meeting=CalendarOccurrence(OccurrenceKey(42,begin),begin.plusSeconds(3600),"PRIVATE TITLE","PRIVATE CALENDAR")
    private var permitted=true
    private var calls=0
    private var read:CalendarRead=CalendarRead.Data(listOf(meeting))
    private var duringRead:()->Unit={}
    private val settings=MemoryDemoSettings()
    private val calendar=GetCalendarDay(object:CalendarRepository {
        override fun hasReadAccess()=permitted
        override suspend fun read(day:CalendarDay):CalendarRead {
            calls++;assertEquals(this@EvaluateDemoJourneyTest.day,day.date)
            duringRead();return read
        }
    })
    private fun query()=EvaluateDemoJourney(calendar,settings,clock,zone)
    @Test fun `three scenarios compare synthetic journey with real occurrence`()=runTest {
        for(scenario in DemoScenario.entries) {
            settings.setScenario(scenario)
            val value=(query()() as DemoResult.Ready).value
            assertEquals(meeting,value.occurrence)
            assertEquals(begin.minusSeconds(35*60),value.evidence.departure)
            assertEquals("FIXTURE",value.evidence.source.name)
            when(scenario) {
                DemoScenario.ON_TIME -> {assertEquals(ComparisonStatus.ON_TIME,value.comparison?.status);assertEquals(0L,value.comparison?.signedMarginMinutes)}
                DemoScenario.DELAYED -> {assertEquals(ComparisonStatus.LATE,value.comparison?.status);assertEquals(-15L,value.comparison?.signedMarginMinutes)}
                DemoScenario.UNKNOWN -> {assertNull(value.evidence.arrival);assertNull(value.comparison)}
            }
        }
    }
    @Test fun `no permission never reads calendar`()=runTest {
        permitted=false;assertEquals(DemoResult.PermissionRequired,query()());assertEquals(0,calls)
    }
    @Test fun `permission revoked during query discloses no occurrence`()=runTest {
        duringRead={permitted=false};assertEquals(DemoResult.PermissionRequired,query()())
    }
    @Test fun `empty and failed calendar remain explicit`()=runTest {
        read=CalendarRead.Data(emptyList());assertEquals(DemoResult.NoMeeting(day,zone),query()())
        read=CalendarRead.Unavailable(CalendarFailure.QUERY_FAILED)
        assertEquals(DemoResult.CalendarUnavailable,query()())
    }
    @Test fun `first meeting is selected from sorted eligible occurrences`()=runTest {
        read=CalendarRead.Data(listOf(meeting.copy(key=OccurrenceKey(43,begin.plusSeconds(60))),meeting))
        assertEquals(meeting,(query()() as DemoResult.Ready).value.occurrence)
    }
    @Test fun `tomorrow follows local date at DST boundary and refreshes clock`()=runTest {
        val beforeMidnight=Clock.fixed(Instant.parse("2026-10-24T21:30:00Z"),ZoneOffset.UTC)
        assertEquals(LocalDate.parse("2026-10-25"),EvaluateDemoJourney(calendar,settings,beforeMidnight,zone).tomorrow())
        assertEquals(day,query().tomorrow())
    }
    @Test fun `cancellation propagates`()=runTest {
        duringRead={throw CancellationException("cancelled")}
        try{query()();fail()}catch(_:CancellationException){}
    }
}

internal class MemoryDemoSettings:DemoSettingsRepository {
    var value=DemoSettings()
    override suspend fun get()=value
    override suspend fun setScenario(scenario:DemoScenario){value=value.copy(scenario=scenario)}
    override suspend fun setAgentAllowed(allowed:Boolean){value=value.copy(agentAllowed=allowed)}
}
