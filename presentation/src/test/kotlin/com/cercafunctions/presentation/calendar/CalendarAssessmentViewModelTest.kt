package com.cercafunctions.presentation.calendar

import androidx.lifecycle.ViewModelStore
import com.cercafunctions.domain.assessment.*
import com.cercafunctions.domain.calendar.*
import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.journey.*
import com.cercafunctions.presentation.home.MainDispatcherRule
import java.time.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarAssessmentViewModelTest {
    @get:Rule val main=MainDispatcherRule()
    private val store=ViewModelStore()
    private val zone=ZoneId.of("Europe/Madrid")
    private val clock=Clock.fixed(Instant.parse("2026-11-01T07:00:00Z"),ZoneOffset.UTC)
    private val meeting=CalendarOccurrence(OccurrenceKey(1,Instant.parse("2026-11-02T08:00:00Z")),Instant.parse("2026-11-02T09:00:00Z"),"POC reunión","POC")
    private var access=true
    private var read:CalendarRead=CalendarRead.Data(listOf(meeting))
    private var calls=0
    private var gate:CompletableDeferred<Unit>?=null
    private var cancel=false
    private val query=QueryJourneyArrival(object:JourneyRepository {
        override suspend fun getHabitualJourney()=JourneyReadResult.Configured(HabitualJourney("A","B",35))
        override suspend fun saveHabitualJourney(journey:HabitualJourney)=error("unused")
    },JourneyFixtureRepository{null},clock)
    private fun model():CalendarViewModel {
        val get=GetCalendarDay(object:CalendarRepository {
            override fun hasReadAccess()=access
            override suspend fun read(day:CalendarDay):CalendarRead {calls++;gate?.await();if(cancel)throw CancellationException("injected");return read}
        })
        val validator=RevalidateCalendarSelection(get)
        return CalendarViewModel(get,validator,clock,zone,AssessArrival(query,validator,clock)).also{store.put("vm",it)}
    }
    private suspend fun TestScope.ready(m:CalendarViewModel) {
        m.updateDate("2026-11-02");m.consult();runCurrent();m.select(meeting.key)
        m.setArrival((query(DepartureInput(LocalDateTime.parse("2026-11-02T08:15"),zone)) as ArrivalResult.Estimated).value)
    }
    @After fun clear(){store.clear()}
    @Test fun `evaluation is explicit and preserves selection until inputs change`()=runTest {
        val m=model();ready(m);assertNull(m.uiState.value.assessment)
        m.evaluate();runCurrent()
        assertEquals(10L,(m.uiState.value.assessment as AssessmentResult.Compared).value.signedMarginMinutes)
        assertEquals(meeting.key,m.uiState.value.selection?.key)
        m.setArrival(null)
        assertNull(m.uiState.value.assessment);assertEquals(meeting.key,m.uiState.value.selection?.key)
    }
    @Test fun `input changes while evaluating cancel and suppress stale result`()=runTest {
        val m=model();ready(m);gate=CompletableDeferred()
        m.evaluate();m.evaluate();runCurrent();assertEquals(2,calls)
        assertTrue(m.uiState.value.assessmentBusy)
        m.setArrival(null);gate!!.complete(Unit);runCurrent()
        assertFalse(m.uiState.value.assessmentBusy);assertNull(m.uiState.value.assessment)
    }
    @Test fun `invalid calendar clears selection but preserves indeterminate explanation`()=runTest {
        val m=model();ready(m);read=CalendarRead.Data(emptyList())
        m.evaluate();runCurrent()
        assertNull(m.uiState.value.selection);assertNull(m.uiState.value.selected);assertTrue(m.uiState.value.items.isEmpty())
        assertEquals(CalendarStatus.CHANGED,m.uiState.value.status)
        assertEquals(AssessmentReason.CALENDAR_CHANGED,(m.uiState.value.assessment as AssessmentResult.Indeterminate).reason)
    }
    @Test fun `permission and provider failure clear obsolete agenda details immediately`()=runTest {
        val m=model();ready(m);access=false;m.evaluate();runCurrent()
        assertNull(m.uiState.value.selected);assertEquals(CalendarStatus.ACCESS_REQUIRED,m.uiState.value.status)
        assertEquals(AssessmentReason.CALENDAR_PERMISSION,(m.uiState.value.assessment as AssessmentResult.Indeterminate).reason)
        access=true;ready(m);read=CalendarRead.Unavailable(CalendarFailure.QUERY_FAILED);m.evaluate();runCurrent()
        assertNull(m.uiState.value.selection);assertEquals(CalendarStatus.UNAVAILABLE,m.uiState.value.status)
    }
    @Test fun `resuming selecting or revalidating withdraws old evaluation`()=runTest {
        val m=model();ready(m);m.evaluate();runCurrent();m.onResume()
        assertNull(m.uiState.value.assessment);assertNotNull(m.uiState.value.selection)
        m.evaluate();runCurrent();m.select(meeting.key);assertNull(m.uiState.value.assessment)
        m.evaluate();runCurrent();m.revalidate();runCurrent();assertNull(m.uiState.value.assessment)
    }
    @Test fun `editing calendar invalidates pending evaluation without stale publication`()=runTest {
        val m=model();ready(m);gate=CompletableDeferred();m.evaluate();runCurrent()
        m.updateZone("UTC");gate!!.complete(Unit);runCurrent()
        assertNull(m.uiState.value.assessment);assertNull(m.uiState.value.selection)
    }
    @Test fun `leaving foreground clears completed evaluation and cancels pending work`()=runTest {
        val m=model();ready(m);m.evaluate();runCurrent()
        assertNotNull(m.uiState.value.assessment)
        m.onStop();assertNull(m.uiState.value.assessment);assertNotNull(m.uiState.value.selection)
        gate=CompletableDeferred();m.evaluate();runCurrent();assertTrue(m.uiState.value.assessmentBusy)
        m.onStop();gate!!.complete(Unit);runCurrent()
        assertNull(m.uiState.value.assessment);assertFalse(m.uiState.value.assessmentBusy)
        assertNotNull(m.uiState.value.selection)
    }
    @Test fun `missing inputs return indeterminate and cancellation is not an error`()=runTest {
        val m=model();m.evaluate();runCurrent()
        assertEquals(AssessmentReason.MISSING_ARRIVAL,(m.uiState.value.assessment as AssessmentResult.Indeterminate).reason)
        ready(m);cancel=true;m.evaluate();runCurrent()
        assertNull(m.uiState.value.assessment);assertFalse(m.uiState.value.assessmentBusy)
    }
}
