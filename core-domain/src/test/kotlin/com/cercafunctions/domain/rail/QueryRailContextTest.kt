package com.cercafunctions.domain.rail

import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.journey.HabitualJourney
import java.time.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class QueryRailContextTest {
    private val now = Instant.parse("2026-11-02T07:00:00Z")
    private val reference = ArrivalEstimate(HabitualJourney("A", "B", 35), now.plusSeconds(86400), now.plusSeconds(88500), ZoneId.of("Europe/Madrid"), ArrivalSource.REFERENCE, ArrivalScenario.REFERENCE, now, null)
    private val selection = RailSelection("70100", "70108")
    private var reads = 0
    private var cancelled = false
    private var read: RailFeedRead = RailFeedRead.Success(RailFeed(now, emptyList()))
    private val repo = object : RailContextRepository {
        override val sourceUrl = "https://gtfsrt.renfe.com/alerts.json"
        override suspend fun read(): RailFeedRead { reads++; if (cancelled) throw CancellationException("injected"); return read }
    }
    private val query = QueryRailContext(repo, RailStationCatalog { listOf(RailStation("70100","A"),RailStation("70108","B")) }, Clock.fixed(now,ZoneOffset.UTC))
    private fun alert(selectors: List<RailSelector> = listOf(RailSelector("70100", false)), impact: List<AlertPeriod> = listOf(AlertPeriod(reference.departure,reference.arrival)), publication: List<AlertPeriod> = emptyList()) =
        RailAlert("id", "Ascensor sin servicio; aviso sin minutos", "ACCESSIBILITY_ISSUE", selectors, publication, impact)
    private fun feed(vararg alerts: RailAlert, stamp: Instant? = now) { read = RailFeedRead.Success(RailFeed(stamp,alerts.toList())) }

    @Test fun `matching endpoint and impact preserve provenance and reference`() = runTest {
        feed(alert())
        val before = reference.copy()
        val result = query(reference,selection)
        assertEquals(RailStatus.AVAILABLE,result.status)
        assertTrue(result.alerts.single().impactConfirmed)
        assertEquals(now,result.consultedAt); assertEquals(now,result.publishedAt)
        assertEquals(repo.sourceUrl,result.sourceUrl)
        assertEquals(before,reference)
    }
    @Test fun `selectors are OR but constraints inside each are AND`() = runTest {
        feed(alert(listOf(RailSelector("other",false),RailSelector("70108",false))))
        assertEquals(1,query(reference,selection).alerts.size)
        feed(alert(listOf(RailSelector("70100",true))))
        val result=query(reference,selection)
        assertTrue(result.alerts.isEmpty()); assertEquals(1,result.unresolvedCount)
    }
    @Test fun `unrelated endpoint never affects arrival`() = runTest {
        feed(alert(listOf(RailSelector("other",false))))
        val result=query(reference,selection)
        assertEquals(RailStatus.AVAILABLE,result.status); assertTrue(result.alerts.isEmpty())
        assertEquals(Instant.parse("2026-11-03T07:35:00Z"),reference.arrival)
    }
    @Test fun `known impact outside requested trip is omitted`() = runTest {
        feed(alert(impact=listOf(AlertPeriod(now,reference.departure))))
        assertTrue(query(reference,selection).alerts.isEmpty())
    }
    @Test fun `publication alone never proves tomorrow impact`() = runTest {
        feed(alert(impact=emptyList(),publication=listOf(AlertPeriod(now,null))))
        assertFalse(query(reference,selection).alerts.single().impactConfirmed)
        feed(alert(impact=emptyList(),publication=listOf(AlertPeriod(now,reference.departure))))
        assertTrue(query(reference,selection).alerts.isEmpty())
    }
    @Test fun `unknown periods remain unknown not zero`() = runTest {
        feed(alert(impact=emptyList()))
        assertFalse(query(reference,selection).alerts.single().impactConfirmed)
    }
    @Test fun `period bounds are half open and support one missing bound`() {
        val from=reference.departure; val until=reference.arrival!!
        assertTrue(AlertPeriod(from,null).overlaps(from,until))
        assertFalse(AlertPeriod(null,from).overlaps(from,until))
        assertFalse(AlertPeriod(until,null).overlaps(from,until))
        assertTrue(AlertPeriod(null,until).overlaps(from,until))
    }
    @Test fun `invalid ranges cannot be constructed`() {
        for ((a,b) in listOf(null to null, now to now, now.plusSeconds(1) to now)) {
            try { AlertPeriod(a,b); fail("invalid period") } catch (_:IllegalArgumentException) {}
        }
    }
    @Test fun `freshness states differ and original timestamps survive`() = runTest {
        for ((stamp,status) in listOf(null to RailStatus.FRESHNESS_UNKNOWN, now.minusSeconds(601) to RailStatus.STALE, now.plusSeconds(61) to RailStatus.FUTURE_TIMESTAMP, now.minusSeconds(600) to RailStatus.AVAILABLE)) {
            feed(alert(),stamp=stamp)
            val result=query(reference,selection)
            assertEquals(status,result.status); assertEquals(stamp,result.publishedAt)
            if(status!=RailStatus.AVAILABLE) assertTrue(result.alerts.isEmpty())
        }
    }
    @Test fun `unresolved absent duplicate and unknown ids never fetch`() = runTest {
        for(s in listOf(null,RailSelection("70100","70100"),RailSelection("invented","70100"))) {
            assertEquals(RailStatus.UNRESOLVED,query(reference,s).status)
        }
        assertEquals(0,reads)
    }
    @Test fun `demo cannot query real source`() = runTest {
        assertEquals(RailStatus.DEMO_SKIPPED,query(reference.copy(source=ArrivalSource.FIXTURE,scenario=ArrivalScenario.DEMO_DELAYED),selection).status)
        assertEquals(0,reads)
    }
    @Test fun `failure differs from no matching alerts`() = runTest {
        read=RailFeedRead.Failure(RailFailure.NETWORK)
        val result=query(reference,selection)
        assertEquals(RailStatus.UNAVAILABLE,result.status); assertEquals(RailFailure.NETWORK,result.failure)
    }
    @Test fun `cancellation propagates`() = runTest {
        cancelled=true
        try {query(reference,selection); fail("cancellation")} catch (_:CancellationException) {}
    }
}
