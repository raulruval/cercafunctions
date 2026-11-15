package com.cercafunctions.presentation.estimate

import androidx.lifecycle.ViewModelStore
import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.journey.*
import com.cercafunctions.presentation.home.MainDispatcherRule
import java.time.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArrivalViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val store = ViewModelStore()
    private var reads = 0
    private var gate: CompletableDeferred<Unit>? = null
    private var cancel = false
    private val zone = ZoneId.of("Europe/Madrid")
    private var fixture = JourneyFixture(HabitualJourney("Demo A", "Demo B", 35), DepartureInput(LocalDateTime.parse("2026-11-02T08:00"), zone), Instant.parse("2026-11-01T08:00:00Z"), 15)
    private fun model(): ArrivalViewModel {
        val repo = object : JourneyRepository {
            override suspend fun getHabitualJourney(): JourneyReadResult {
                reads++; gate?.await()
                if (cancel) throw CancellationException("injected")
                return JourneyReadResult.Configured(HabitualJourney("A", "B", 35))
            }
            override suspend fun saveHabitualJourney(journey: HabitualJourney) = error("unused")
        }
        return ArrivalViewModel(QueryJourneyArrival(repo, JourneyFixtureRepository { if (it == ArrivalScenario.REFERENCE) null else fixture }, Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)), zone).also { store.put("vm", it) }
    }
    @After fun clear() { store.clear() }
    @Test fun `tomorrow date and explicit time available without auto query`() = runTest {
        val m = model()
        assertEquals("2026-01-02", m.uiState.value.date)
        assertEquals("08:00", m.uiState.value.time)
        assertNull(m.uiState.value.result)
        assertEquals(0, reads)
        m.updateZone("America/Los_Angeles"); m.tomorrow()
        assertEquals("2026-01-01", m.uiState.value.date)
    }
    @Test fun `formats rejected before domain query`() = runTest {
        val m = model()
        m.updateDate("2026-02-30"); m.consult()
        assertEquals(ArrivalInputError.DATE, m.uiState.value.error)
        m.updateDate("2026-11-02"); m.updateTime("8:00"); m.consult()
        assertEquals(ArrivalInputError.TIME, m.uiState.value.error)
        m.updateTime("24:00"); m.consult()
        assertEquals(ArrivalInputError.TIME, m.uiState.value.error)
        m.updateTime("08:00"); m.updateZone("Bad/Zone"); m.consult()
        assertEquals(ArrivalInputError.ZONE, m.uiState.value.error)
        assertEquals(0, reads)
    }
    @Test fun `consult then edit invalidates displayed estimate`() = runTest {
        val m = model(); m.consult(); runCurrent()
        assertTrue(m.uiState.value.result is ArrivalResult.Estimated)
        m.updateTime("09:00")
        assertNull(m.uiState.value.result)
        m.consult(); runCurrent(); m.invalidate()
        assertNull(m.uiState.value.result)
    }
    @Test fun `demo locks input and reference restores user draft`() = runTest {
        val m = model(); m.updateDate("2026-12-02"); m.updateTime("09:00")
        m.selectScenario(ArrivalScenario.DEMO_DELAYED)
        assertEquals("2026-11-02", m.uiState.value.date)
        assertEquals(fixture, m.uiState.value.fixture)
        m.updateTime("10:00"); m.tomorrow()
        assertEquals("08:00", m.uiState.value.time)
        m.consult(); runCurrent()
        assertEquals(ArrivalSource.FIXTURE, (m.uiState.value.result as ArrivalResult.Estimated).value.source)
        m.selectScenario(ArrivalScenario.REFERENCE)
        assertEquals("09:00", m.uiState.value.time)
        assertEquals("2026-12-02", m.uiState.value.date)
        assertNull(m.uiState.value.result)
    }
    @Test fun `overlap exposes choices and consult requires chosen valid offset`() = runTest {
        val m = model(); m.updateDate("2026-10-25"); m.updateTime("02:30"); m.consult(); runCurrent()
        assertEquals(2, m.uiState.value.offsets.size)
        m.chooseOffset(ZoneOffset.UTC)
        assertNull(m.uiState.value.selectedOffset)
        m.chooseOffset(ZoneOffset.of("+01:00")); m.consult(); runCurrent()
        assertTrue(m.uiState.value.result is ArrivalResult.Estimated)
        m.updateZone("UTC")
        assertTrue(m.uiState.value.offsets.isEmpty())
        assertNull(m.uiState.value.selectedOffset)
    }
    @Test fun `editing cancels pending query and prevents stale result`() = runTest {
        gate = CompletableDeferred()
        val m = model(); m.consult(); m.consult(); runCurrent()
        assertEquals(1, reads)
        assertTrue(m.uiState.value.busy)
        m.updateTime("10:00"); gate!!.complete(Unit); runCurrent()
        assertFalse(m.uiState.value.busy)
        assertNull(m.uiState.value.result)
        m.consult(); runCurrent()
        assertEquals(2, reads)
        assertTrue(m.uiState.value.result is ArrivalResult.Estimated)
    }
    @Test fun `same journey after recreation preserves query but changed journey invalidates`() = runTest {
        val m = model()
        val journey = HabitualJourney("A", "B", 35)
        m.onJourneyChanged(journey); m.consult(); runCurrent()
        val result = m.uiState.value.result
        assertTrue(result is ArrivalResult.Estimated)
        m.onJourneyChanged(journey.copy())
        assertEquals(result, m.uiState.value.result)
        m.onJourneyChanged(journey.copy(estimatedDurationMinutes = 40))
        assertNull(m.uiState.value.result)
    }
    @Test fun `cancellation is not failure or success`() = runTest {
        cancel = true
        val m = model(); m.consult(); runCurrent()
        assertFalse(m.uiState.value.busy)
        assertNull(m.uiState.value.result)
        assertNull(m.uiState.value.error)
    }

    @Test fun `consult refreshes a selected demo after the fixture date changes`() = runTest {
        val m = model()
        m.selectScenario(ArrivalScenario.DEMO_DELAYED)
        fixture = fixture.copy(departure = fixture.departure.copy(local = LocalDateTime.parse("2026-11-03T08:00")))
        m.consult(); runCurrent()
        val result = (m.uiState.value.result as ArrivalResult.Estimated).value
        assertEquals("2026-11-03", m.uiState.value.date)
        assertEquals(LocalDate.parse("2026-11-03"), result.departure.atZone(zone).toLocalDate())
        assertEquals("08:50", result.arrival!!.atZone(zone).toLocalTime().toString())
        fixture = fixture.copy(departure = fixture.departure.copy(local = LocalDateTime.parse("2026-11-04T08:00")))
        m.selectScenario(ArrivalScenario.DEMO_DELAYED)
        assertEquals("2026-11-04", m.uiState.value.date)
        assertNull(m.uiState.value.result)
    }
}
