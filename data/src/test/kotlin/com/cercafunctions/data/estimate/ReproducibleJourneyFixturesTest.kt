package com.cercafunctions.data.estimate

import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.journey.*
import java.time.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ReproducibleJourneyFixturesTest {
    private val clock = Clock.fixed(Instant.parse("2026-11-01T08:00:00Z"), ZoneOffset.UTC)
    private val fixtures = ReproducibleJourneyFixtures(clock)
    private val arbitrary = DepartureInput(LocalDateTime.parse("2000-01-01T00:00"), ZoneOffset.UTC)
    private fun query(configured: Boolean = true) = QueryJourneyArrival(object : JourneyRepository {
        override suspend fun getHabitualJourney() = if (configured) JourneyReadResult.Configured(HabitualJourney("Real A", "Real B", 90)) else JourneyReadResult.NotConfigured
        override suspend fun saveHabitualJourney(journey: HabitualJourney) = error("unused")
    }, fixtures, clock)

    @Test fun `catalog keeps reference separate and uses tomorrow with a fixed injected clock`() {
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
    @Test fun `delayed is deterministic 0850 with injected clock independent of saved route and input`() = runTest {
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

    @Test fun `all demos use tomorrow in Madrid even when UTC has another date`() {
        val now = Instant.parse("2026-11-04T23:30:00Z")
        val catalog = ReproducibleJourneyFixtures(Clock.fixed(now, ZoneOffset.UTC))
        for (scenario in ArrivalScenario.entries.filter { it != ArrivalScenario.REFERENCE }) {
            val demo = catalog.find(scenario)!!
            assertEquals(LocalDateTime.parse("2026-11-06T08:00"), demo.departure.local)
            assertEquals(now, demo.observedAt)
        }
    }
    @Test fun `tomorrow rolls over the year`() {
        val catalog = ReproducibleJourneyFixtures(Clock.fixed(Instant.parse("2026-12-31T12:00:00Z"), ZoneOffset.UTC))
        assertEquals(LocalDateTime.parse("2027-01-01T08:00"), catalog.find(ArrivalScenario.DEMO_NORMAL)!!.departure.local)
    }
    @Test fun `fresh lookup advances after midnight and preserves local hour across DST`() {
        var now = Instant.parse("2026-03-27T12:00:00Z")
        val movingClock = object : Clock() {
            override fun instant() = now
            override fun getZone(): ZoneId = ZoneOffset.UTC
            override fun withZone(zone: ZoneId): Clock = Clock.fixed(now, zone)
        }
        val catalog = ReproducibleJourneyFixtures(movingClock)
        val first = catalog.find(ArrivalScenario.DEMO_DELAYED)!!
        now = Instant.parse("2026-03-28T12:00:00Z")
        val second = catalog.find(ArrivalScenario.DEMO_DELAYED)!!
        assertEquals(LocalDateTime.parse("2026-03-28T08:00"), first.departure.local)
        assertEquals(LocalDateTime.parse("2026-03-29T08:00"), second.departure.local)
        assertEquals(23, Duration.between(first.departure.local.atZone(first.departure.zone),
            second.departure.local.atZone(second.departure.zone)).toHours())
        assertEquals(15, second.delayMinutes)
    }
}
