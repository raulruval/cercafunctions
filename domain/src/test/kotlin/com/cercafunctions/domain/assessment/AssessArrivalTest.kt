package com.cercafunctions.domain.assessment

import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.calendar.*
import com.cercafunctions.domain.journey.*
import java.time.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AssessArrivalTest {
    private val zone=ZoneId.of("Europe/Madrid")
    private var now=Instant.parse("2026-11-01T07:00:00Z")
    private val clock=object:Clock(){override fun getZone():ZoneId=ZoneOffset.UTC;override fun withZone(zone:ZoneId):Clock=this;override fun instant():Instant=now}
    private var journey=HabitualJourney("Atocha","Chamartín",35)
    private var journeyRead:JourneyReadResult?=null
    private var calendarCalls=0
    private var cancel=false
    private var meeting=CalendarOccurrence(OccurrenceKey(1,Instant.parse("2026-11-02T08:00:00Z")),Instant.parse("2026-11-02T09:00:00Z"),"POC secreto","POC privado")
    private var calendarRead:CalendarRead?=null
    private var access=true
    private val query=QueryJourneyArrival(object:JourneyRepository {
        override suspend fun getHabitualJourney()=journeyRead ?: JourneyReadResult.Configured(journey)
        override suspend fun saveHabitualJourney(journey:HabitualJourney)=error("read only")
    },JourneyFixtureRepository {scenario ->
        if(scenario==ArrivalScenario.REFERENCE)null else JourneyFixture(journey,
            DepartureInput(LocalDateTime.parse("2026-11-02T08:00"),zone),Instant.parse("2026-11-01T08:00:00Z"),
            when(scenario){ArrivalScenario.DEMO_UNKNOWN->null;ArrivalScenario.DEMO_DELAYED->15;else->0})
    },clock)
    private val get=GetCalendarDay(object:CalendarRepository {
        override fun hasReadAccess()=access
        override suspend fun read(day:CalendarDay):CalendarRead {calendarCalls++;if(cancel)throw CancellationException("injected");return calendarRead ?: CalendarRead.Data(listOf(meeting))}
    })
    private val assess=AssessArrival(query,RevalidateCalendarSelection(get),clock)
    private fun selection(queryZone:ZoneId=zone)=CalendarSelection(meeting.key,meeting.end,CalendarDay(meeting.key.begin.atZone(queryZone).toLocalDate(),queryZone))
    private suspend fun estimate(arrival:Instant,queryZone:ZoneId=zone):ArrivalEstimate {
        val departure=arrival.minusSeconds(35*60).atZone(queryZone)
        return (query(DepartureInput(departure.toLocalDateTime(),queryZone,departure.offset)) as ArrivalResult.Estimated).value
    }
    private suspend fun compare(deltaSeconds:Long):ComparedArrival =
        (assess(estimate(meeting.key.begin.plusSeconds(deltaSeconds)),selection()) as AssessmentResult.Compared).value

    @Test fun `early equal and late preserve 10 zero and negative 15 margins`()=runTest {
        for((seconds,expected) in listOf(-600L to 10L,0L to 0L,900L to -15L)) {
            val value=compare(seconds)
            assertEquals(expected,value.signedMarginMinutes)
            assertEquals(if(seconds>0)ComparisonStatus.LATE else ComparisonStatus.ON_TIME,value.status)
            assertEquals(meeting.key.begin,value.meeting.start)
            assertEquals(zone,value.meeting.zone)
        }
    }
    @Test fun `fractions floor availability but ceil late magnitude including one nanosecond`()=runTest {
        for((delta,expected) in listOf(Duration.ofMinutes(-1) to 1L,Duration.ofSeconds(-60).plusNanos(1) to 0L,
            Duration.ofNanos(-1) to 0L,Duration.ofNanos(1) to -1L,Duration.ofMinutes(1) to -1L,Duration.ofMinutes(1).plusNanos(1) to -2L)) {
            val arrival=meeting.key.begin.plus(delta)
            val result=(assess(estimate(arrival),selection()) as AssessmentResult.Compared).value
            assertEquals(expected,result.signedMarginMinutes)
            assertEquals(if(delta.isNegative)ComparisonStatus.ON_TIME else ComparisonStatus.LATE,result.status)
        }
    }
    @Test fun `equal instants with different display zones produce same margin`()=runTest {
        val at=meeting.key.begin.minusSeconds(600)
        val a=(assess(estimate(at,zone),selection()) as AssessmentResult.Compared).value
        val b=(assess(estimate(at,ZoneOffset.UTC),selection(ZoneId.of("America/New_York"))) as AssessmentResult.Compared).value
        assertEquals(a.signedMarginMinutes,b.signedMarginMinutes)
        assertNotEquals(a.meeting.zone,b.meeting.zone)
    }
    @Test fun `midnight and different dates never shift instants`()=runTest {
        meeting=meeting.copy(key=OccurrenceKey(1,Instant.parse("2026-11-02T23:15:00Z")),end=Instant.parse("2026-11-03T00:15:00Z"))
        val arrival=estimate(meeting.key.begin.minusSeconds(600))
        assertNotEquals(arrival.departure.atZone(zone).toLocalDate(),arrival.arrival!!.atZone(zone).toLocalDate())
        assertEquals(10L,(assess(arrival,selection()) as AssessmentResult.Compared).value.signedMarginMinutes)
    }
    @Test fun `missing inputs do not read calendar or invent timing`()=runTest {
        assertEquals(AssessmentReason.MISSING_ARRIVAL,(assess(null,selection()) as AssessmentResult.Indeterminate).reason)
        assertEquals(AssessmentReason.MISSING_SELECTION,(assess(estimate(meeting.key.begin),null) as AssessmentResult.Indeterminate).reason)
        assertEquals(0,calendarCalls)
    }
    @Test fun `unknown fixture never becomes zero delay or triggers unnecessary agenda read`()=runTest {
        val unknown=(query(DepartureInput(LocalDateTime.parse("2026-11-02T08:00"),zone),ArrivalScenario.DEMO_UNKNOWN) as ArrivalResult.Estimated).value
        val result=assess(unknown,selection()) as AssessmentResult.Indeterminate
        assertEquals(AssessmentReason.UNKNOWN_ARRIVAL,result.reason)
        assertNull(result.evidence!!.arrival);assertNull(result.evidence.delayMinutes)
        assertEquals(ArrivalSource.FIXTURE,result.evidence.source);assertEquals(0,calendarCalls)
    }
    @Test fun `fixture date clock and source survive current clock far in future`()=runTest {
        now=Instant.parse("2099-01-01T00:00:00Z")
        val snapshot=(query(DepartureInput(LocalDateTime.parse("2099-02-02T09:00"),ZoneOffset.UTC),ArrivalScenario.DEMO_DELAYED) as ArrivalResult.Estimated).value
        val value=(assess(snapshot,selection()) as AssessmentResult.Compared).value
        assertEquals(10L,value.signedMarginMinutes);assertEquals(ArrivalSource.FIXTURE,value.evidence.source)
        assertEquals(Instant.parse("2026-11-01T08:00:00Z"),value.evidence.observedAt);assertEquals(now,value.assessedAt)
    }
    @Test fun `changed journey cannot compare another arrival silently`()=runTest {
        val snapshot=estimate(meeting.key.begin.minusSeconds(600));journey=journey.copy(estimatedDurationMinutes=40)
        assertEquals(AssessmentReason.ARRIVAL_CHANGED,(assess(snapshot,selection()) as AssessmentResult.Indeterminate).reason)
        assertEquals(0,calendarCalls)
    }
    @Test fun `fresh reference timestamp can change without arbitrary TTL`()=runTest {
        val snapshot=estimate(meeting.key.begin.minusSeconds(600));now=now.plusSeconds(3600)
        val value=(assess(snapshot,selection()) as AssessmentResult.Compared).value
        assertEquals(10L,value.signedMarginMinutes);assertEquals(now,value.evidence.observedAt)
        assertFalse(value.toString().contains("POC secreto"));assertFalse(value.toString().contains("POC privado"))
    }
    @Test fun `past departure and unavailable journey return distinct causes`()=runTest {
        val snapshot=estimate(meeting.key.begin.minusSeconds(600));now=snapshot.departure.plusSeconds(1)
        assertEquals(AssessmentReason.DEPARTURE_PASSED,(assess(snapshot,selection()) as AssessmentResult.Indeterminate).reason)
        journeyRead=JourneyReadResult.NotConfigured
        assertEquals(AssessmentReason.ARRIVAL_UNAVAILABLE,(assess(snapshot,selection()) as AssessmentResult.Indeterminate).reason)
    }
    @Test fun `calendar removed or moved is indeterminate without obsolete meeting`()=runTest {
        val snapshot=estimate(meeting.key.begin);val selected=selection()
        for(rows in listOf(emptyList(),listOf(meeting.copy(key=OccurrenceKey(1,meeting.key.begin.plusSeconds(60)))))) {
            calendarRead=CalendarRead.Data(rows)
            assertEquals(AssessmentReason.CALENDAR_CHANGED,(assess(snapshot,selected) as AssessmentResult.Indeterminate).reason)
        }
    }
    @Test fun `revoked and inaccessible calendar have distinct causes`()=runTest {
        val snapshot=estimate(meeting.key.begin)
        access=false;assertEquals(AssessmentReason.CALENDAR_PERMISSION,(assess(snapshot,selection()) as AssessmentResult.Indeterminate).reason)
        access=true;calendarRead=CalendarRead.Unavailable(CalendarFailure.PROVIDER_UNAVAILABLE)
        assertEquals(AssessmentReason.CALENDAR_UNAVAILABLE,(assess(snapshot,selection()) as AssessmentResult.Indeterminate).reason)
    }
    @Test fun `cancellation propagates`()=runTest {
        val snapshot=estimate(meeting.key.begin);cancel=true
        try{assess(snapshot,selection());fail("cancellation")}catch(_:CancellationException){}
    }
}
