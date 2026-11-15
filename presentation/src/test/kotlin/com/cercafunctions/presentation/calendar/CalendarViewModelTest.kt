package com.cercafunctions.presentation.calendar

import androidx.lifecycle.ViewModelStore
import com.cercafunctions.domain.calendar.*
import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.assessment.AssessArrival
import com.cercafunctions.domain.journey.*
import com.cercafunctions.presentation.home.MainDispatcherRule
import java.time.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {
    @get:Rule val main=MainDispatcherRule()
    private val store=ViewModelStore()
    private val zone=ZoneId.of("Europe/Madrid")
    private val now=Instant.parse("2026-11-02T12:00:00Z")
    private val day=CalendarDay(LocalDate.parse("2026-11-03"),zone)
    private val event=CalendarOccurrence(OccurrenceKey(1,day.start.plusSeconds(9*3600)),day.start.plusSeconds(10*3600),"POC reunión","POC")
    private var access=true
    private var calls=0
    private var gate:CompletableDeferred<Unit>?=null
    private var cancel=false
    private var result:CalendarRead=CalendarRead.Data(listOf(event))
    private fun model():CalendarViewModel {
        val repo=object:CalendarRepository {
            override fun hasReadAccess()=access
            override suspend fun read(day:CalendarDay):CalendarRead {calls++;gate?.await();if(cancel)throw CancellationException("injected");return result}
        }
        val get=GetCalendarDay(repo)
        val clock=Clock.fixed(now,ZoneOffset.UTC)
        val arrivalQuery=QueryJourneyArrival(object:JourneyRepository {
            override suspend fun getHabitualJourney()=JourneyReadResult.Configured(HabitualJourney("A","B",35))
            override suspend fun saveHabitualJourney(journey:HabitualJourney)=error("unused")
        },JourneyFixtureRepository{null},clock)
        return CalendarViewModel(get,RevalidateCalendarSelection(get),clock,zone,AssessArrival(arrivalQuery,RevalidateCalendarSelection(get),clock)).also{store.put("vm",it)}
    }
    @After fun clear(){store.clear()}
    @Test fun `initialization and resume never read calendar automatically`()=runTest {
        val m=model();m.onResume();runCurrent()
        assertEquals("2026-11-03",m.uiState.value.date);assertEquals(0,calls)
        m.updateZone("America/Los_Angeles");m.tomorrow()
        assertEquals("2026-11-03",m.uiState.value.date);assertEquals(0,calls)
    }
    @Test fun `invalid inputs are rejected before permission request or query`()=runTest {
        val m=model();m.updateDate("2026-02-30")
        assertFalse(m.prepareConsult());assertEquals(CalendarInputError.DATE,m.uiState.value.inputError)
        m.updateDate("2026-11-03");m.updateZone("Bad/Zone")
        assertFalse(m.prepareConsult());assertEquals(CalendarInputError.ZONE,m.uiState.value.inputError)
        assertEquals(0,calls)
    }
    @Test fun `denial clears local details and later grant consults explicitly`()=runTest {
        access=false
        val m=model();m.onPermissionResult(false);runCurrent()
        assertEquals(CalendarStatus.DENIED,m.uiState.value.status);assertTrue(m.uiState.value.items.isEmpty());assertEquals(0,calls)
        access=true;m.onPermissionResult(true);runCurrent()
        assertEquals(CalendarStatus.DATA,m.uiState.value.status);assertEquals(1,calls)
    }
    @Test fun `one item still requires explicit selection and key must come from list`()=runTest {
        val m=model();m.consult();runCurrent()
        assertNull(m.uiState.value.selection)
        m.select(OccurrenceKey(99,event.key.begin));assertNull(m.uiState.value.selection)
        m.select(event.key)
        assertEquals(event.key,m.uiState.value.selection?.key);assertEquals(event,m.uiState.value.selected)
        assertFalse(m.uiState.value.validated)
    }
    @Test fun `editing date or zone removes details selection and invalidates pending query`()=runTest {
        val m=model();m.consult();runCurrent();m.select(event.key);m.updateZone("UTC")
        assertTrue(m.uiState.value.items.isEmpty());assertNull(m.uiState.value.selection)
        gate=CompletableDeferred();m.consult();m.consult();runCurrent()
        assertEquals(2,calls)
        m.updateDate("2026-11-04");gate!!.complete(Unit);runCurrent()
        assertFalse(m.uiState.value.busy);assertTrue(m.uiState.value.items.isEmpty())
    }
    @Test fun `revalidation updates details and changed event removes selection`()=runTest {
        val m=model();m.consult();runCurrent();m.select(event.key)
        result=CalendarRead.Data(listOf(event.copy(title="POC actualizado")))
        m.revalidate();runCurrent()
        assertTrue(m.uiState.value.validated);assertEquals("POC actualizado",m.uiState.value.selected?.title)
        result=CalendarRead.Data(emptyList());m.revalidate();runCurrent()
        assertNull(m.uiState.value.selection);assertEquals(CalendarStatus.CHANGED,m.uiState.value.status)
    }
    @Test fun `revocation on resume erases agenda and granting in settings does not read`()=runTest {
        val m=model();m.consult();runCurrent();m.select(event.key)
        access=false;m.onResume()
        assertTrue(m.uiState.value.items.isEmpty());assertNull(m.uiState.value.selected);assertNull(m.uiState.value.selection)
        assertEquals(CalendarStatus.ACCESS_REQUIRED,m.uiState.value.status)
        access=true;m.onResume();runCurrent()
        assertEquals(CalendarStatus.IDLE,m.uiState.value.status);assertEquals(1,calls)
    }
    @Test fun `resume with access keeps selection but removes revalidated mark`()=runTest {
        val m=model();m.consult();runCurrent();m.select(event.key);m.revalidate();runCurrent()
        assertTrue(m.uiState.value.validated)
        m.onResume();assertEquals(event.key,m.uiState.value.selection?.key);assertFalse(m.uiState.value.validated)
    }
    @Test fun `provider failure differs from empty calendar and revocation while validating`()=runTest {
        val m=model();result=CalendarRead.Data(emptyList());m.consult();runCurrent()
        assertEquals(CalendarStatus.DATA,m.uiState.value.status);assertTrue(m.uiState.value.items.isEmpty())
        result=CalendarRead.Unavailable(CalendarFailure.PROVIDER_UNAVAILABLE);m.consult();runCurrent()
        assertEquals(CalendarStatus.UNAVAILABLE,m.uiState.value.status)
        result=CalendarRead.Data(listOf(event));m.consult();runCurrent();m.select(event.key)
        gate=CompletableDeferred();m.revalidate();runCurrent();access=false;m.onResume();gate!!.complete(Unit);runCurrent()
        assertEquals(CalendarStatus.ACCESS_REQUIRED,m.uiState.value.status);assertNull(m.uiState.value.selection)
    }
    @Test fun `cancellation is not success or provider error`()=runTest {
        cancel=true
        val m=model();m.consult();runCurrent()
        assertFalse(m.uiState.value.busy);assertTrue(m.uiState.value.items.isEmpty());assertNull(m.uiState.value.failure)
    }
}
