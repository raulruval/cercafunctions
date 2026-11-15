package com.cercafunctions.domain.estimate

import com.cercafunctions.domain.journey.*
import java.time.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class QueryJourneyArrivalTest {
    private val madrid = ZoneId.of("Europe/Madrid")
    private val journey = HabitualJourney("A", "B", 35)
    private val fixtures = JourneyFixtureRepository { null }
    private fun query(now: String = "2026-01-01T00:00:00Z", result: JourneyReadResult = JourneyReadResult.Configured(journey), clock: Clock = Clock.fixed(Instant.parse(now), ZoneOffset.UTC)): QueryJourneyArrival =
        QueryJourneyArrival(object : JourneyRepository {
            override suspend fun getHabitualJourney() = result
            override suspend fun saveHabitualJourney(journey: HabitualJourney) = error("read only")
        }, fixtures, clock)
    private fun input(local: String, offset: String? = null) = DepartureInput(LocalDateTime.parse(local), madrid, offset?.let(ZoneOffset::of))
    private suspend fun estimate(local: String, offset: String? = null) =
        (query()(input(local, offset)) as ArrivalResult.Estimated).value

    @Test fun `reference uses saved duration and keeps delay unknown`() = runTest {
        val value = estimate("2026-11-02T08:00")
        assertEquals("2026-11-02T07:35:00Z", value.arrival.toString())
        assertEquals(ArrivalSource.REFERENCE, value.source)
        assertEquals(ArrivalScenario.REFERENCE, value.scenario)
        assertNull(value.delayMinutes)
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), value.observedAt)
        assertEquals(journey, value.journey)
        assertEquals(madrid, value.zone)
    }
    @Test fun `arrival keeps next date at midnight`() = runTest {
        assertEquals("2026-11-03T00:25+01:00[Europe/Madrid]", estimate("2026-11-02T23:50").arrival!!.atZone(madrid).toString())
    }
    @Test fun `tomorrow is next local date not instant plus 24h`() {
        val q = query("2026-03-28T22:30:00Z")
        assertEquals(LocalDate.parse("2026-03-29"), q.tomorrow(madrid))
        assertEquals(LocalDate.parse("2026-03-30"), Instant.parse("2026-03-28T22:30:00Z").plusSeconds(86400).atZone(madrid).toLocalDate())
    }
    @Test fun `tomorrow respects supplied zone at UTC boundary`() {
        val q = query("2026-11-01T23:30:00Z")
        assertEquals(LocalDate.parse("2026-11-03"), q.tomorrow(madrid))
        assertEquals(LocalDate.parse("2026-11-02"), q.tomorrow(ZoneOffset.UTC))
    }
    @Test fun `past rejected but equality allowed`() = runTest {
        val q = query("2026-11-02T07:00:00Z")
        assertEquals(ArrivalResult.PastDeparture, q(input("2026-11-02T07:59")))
        assertTrue(q(input("2026-11-02T08:00")) is ArrivalResult.Estimated)
    }
    @Test fun `spring gap is never silently shifted`() = runTest {
        assertEquals(ArrivalResult.Gap, query()(input("2026-03-29T02:30")))
    }
    @Test fun `autumn overlap requires explicit valid offset`() = runTest {
        val q = query()
        assertEquals(ArrivalResult.Ambiguous(listOf(ZoneOffset.of("+02:00"), ZoneOffset.of("+01:00"))), q(input("2026-10-25T02:30")))
        assertEquals(ArrivalResult.InvalidOffset, q(input("2026-10-25T02:30", "+03:00")))
        val early = (q(input("2026-10-25T02:30", "+02:00")) as ArrivalResult.Estimated).value
        val late = (q(input("2026-10-25T02:30", "+01:00")) as ArrivalResult.Estimated).value
        assertEquals(Duration.ofHours(1), Duration.between(early.arrival, late.arrival))
    }
    @Test fun `invalid explicit offset rejected even outside overlap`() = runTest {
        assertEquals(ArrivalResult.InvalidOffset, query()(input("2026-11-02T08:00", "+02:00")))
    }
    @Test fun `duration is elapsed across both clock changes`() = runTest {
        assertEquals("2026-03-29T03:25+02:00[Europe/Madrid]", estimate("2026-03-29T01:50").arrival!!.atZone(madrid).toString())
        assertEquals("2026-10-25T02:05+01:00[Europe/Madrid]", estimate("2026-10-25T02:30", "+02:00").arrival!!.atZone(madrid).toString())
        assertEquals("2026-10-25T03:05+01:00[Europe/Madrid]", estimate("2026-10-25T02:30", "+01:00").arrival!!.atZone(madrid).toString())
    }
    @Test fun `missing and unavailable do not invent arrival`() = runTest {
        assertEquals(ArrivalResult.MissingJourney, query(result = JourneyReadResult.NotConfigured)(input("2026-11-02T08:00")))
        assertEquals(ArrivalResult.StorageUnavailable(JourneyStorageFailure.READ_FAILED), query(result = JourneyReadResult.Unavailable(JourneyStorageFailure.READ_FAILED))(input("2026-11-02T08:00")))
    }
    @Test fun `clock is captured exactly once and repeated fixed input matches`() = runTest {
        var reads = 0
        val instant = Instant.parse("2026-01-01T00:00:00Z")
        val clock = object : Clock() {
            override fun getZone(): ZoneId = ZoneOffset.UTC
            override fun withZone(zone: ZoneId): Clock = this
            override fun instant(): Instant { reads++; return instant }
        }
        val q = query(clock = clock)
        val first = q(input("2026-11-02T08:00"))
        assertEquals(1, reads)
        assertEquals(first, q(input("2026-11-02T08:00")))
    }
    @Test fun `repository cancellation propagates`() = runTest {
        val q = QueryJourneyArrival(object : JourneyRepository {
            override suspend fun getHabitualJourney(): JourneyReadResult = throw CancellationException("injected")
            override suspend fun saveHabitualJourney(journey: HabitualJourney) = error("unused")
        }, fixtures, Clock.systemUTC())
        try { q(input("2026-11-02T08:00")); fail("expected cancellation") } catch (_: CancellationException) { }
    }
}
