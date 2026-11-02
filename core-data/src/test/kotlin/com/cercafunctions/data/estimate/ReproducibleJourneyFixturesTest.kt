package com.cercafunctions.data.estimate

import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.journey.*
import java.time.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ReproducibleJourneyFixturesTest {
    private val fixtures = ReproducibleJourneyFixtures()
    private val arbitrary = DepartureInput(LocalDateTime.parse("2000-01-01T00:00"), ZoneOffset.UTC)
    private fun query(configured: Boolean = true) = QueryJourneyArrival(object : JourneyRepository {
        override suspend fun getHabitualJourney() = if (configured) JourneyReadResult.Configured(HabitualJourney("Real A", "Real B", 90)) else JourneyReadResult.NotConfigured
        override suspend fun saveHabitualJourney(journey: HabitualJourney) = error("unused")
    }, fixtures, Clock.fixed(Instant.parse("2099-01-01T00:00:00Z"), ZoneOffset.UTC))

    @Test fun `catalog keeps reference separate and fixes all demo inputs`() {
        assertNull(fixtures.find(ArrivalScenario.REFERENCE))
        for (scenario in ArrivalScenario.entries.filter { it != ArrivalScenario.REFERENCE }) {
            val fixture = fixtures.find(scenario)!!
            assertEquals(HabitualJourney("Origen demo", "Destino demo", 35), fixture.journey)
            assertEquals("2026-11-02T08:00", fixture.departure.local.toString())
            assertEquals("Europe/Madrid", fixture.departure.zone.id)
            assertEquals(Instant.parse("2026-11-01T08:00:00Z"), fixture.observedAt)
        }
    }
    @Test fun `normal is explicitly fixture zero delay and 0835`() = runTest {
        val value = (query()(arbitrary, ArrivalScenario.DEMO_NORMAL) as ArrivalResult.Estimated).value
        assertEquals("08:35", value.arrival!!.atZone(value.zone).toLocalTime().toString())
        assertEquals(0, value.delayMinutes)
        assertEquals(ArrivalSource.FIXTURE, value.source)
    }
    @Test fun `delayed is deterministic 0850 independent of real clock route and input`() = runTest {
        val q = query()
        val result = q(arbitrary, ArrivalScenario.DEMO_DELAYED)
        assertEquals(result, q(arbitrary.copy(local = LocalDateTime.parse("2100-01-01T00:00")), ArrivalScenario.DEMO_DELAYED))
        val value = (result as ArrivalResult.Estimated).value
        assertEquals("08:50", value.arrival!!.atZone(value.zone).toLocalTime().toString())
        assertEquals(15, value.delayMinutes)
        assertEquals(35, value.journey.estimatedDurationMinutes)
        assertEquals(ArrivalSource.FIXTURE, value.source)
    }
    @Test fun `unknown never becomes baseline or zero delay`() = runTest {
        val value = (query()(arbitrary, ArrivalScenario.DEMO_UNKNOWN) as ArrivalResult.Estimated).value
        assertNull(value.arrival)
        assertNull(value.delayMinutes)
        assertEquals(ArrivalSource.FIXTURE, value.source)
        assertEquals(Instant.parse("2026-11-01T08:00:00Z"), value.observedAt)
    }
    @Test fun `demos also require configured journey`() = runTest {
        assertEquals(ArrivalResult.MissingJourney, query(false)(arbitrary, ArrivalScenario.DEMO_DELAYED))
    }
}
