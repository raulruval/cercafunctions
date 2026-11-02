package com.cercafunctions.domain.rail

import com.cercafunctions.domain.estimate.*
import java.time.Clock
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class QueryRailContext(private val repository: RailContextRepository, private val catalog: RailStationCatalog, private val clock: Clock) {
    fun stations(): List<RailStation> = catalog.stations()

    suspend operator fun invoke(reference: ArrivalEstimate, selection: RailSelection?): RailContext {
        currentCoroutineContext().ensureActive()
        fun result(status: RailStatus) = RailContext(status,clock.instant(),repository.sourceUrl)
        if (reference.source != ArrivalSource.REFERENCE || reference.scenario != ArrivalScenario.REFERENCE)
            return result(RailStatus.DEMO_SKIPPED)
        val until = reference.arrival ?: return result(RailStatus.UNRESOLVED)
        val ids = stations().map { it.id }.toSet()
        if (selection == null || selection.originId == selection.destinationId ||
            selection.originId !in ids || selection.destinationId !in ids) return result(RailStatus.UNRESOLVED)
        val read = repository.read()
        currentCoroutineContext().ensureActive()
        val now = clock.instant()
        if (read is RailFeedRead.Failure) return RailContext(RailStatus.UNAVAILABLE,now,repository.sourceUrl,publishedAt=read.publishedAt,failure=read.reason)
        val feed = (read as RailFeedRead.Success).feed
        val stamp = feed.publishedAt
        val status = when {
            stamp == null -> RailStatus.FRESHNESS_UNKNOWN
            stamp > now.plusSeconds(60) -> RailStatus.FUTURE_TIMESTAMP
            stamp < now.minusSeconds(600) -> RailStatus.STALE
            else -> RailStatus.AVAILABLE
        }
        if (status != RailStatus.AVAILABLE) return RailContext(status,now,repository.sourceUrl,stamp)
        var unresolved = 0
        val related = feed.alerts.mapNotNull { alert ->
            currentCoroutineContext().ensureActive()
            val matches = alert.selectors.any { it.stopId in setOf(selection.originId,selection.destinationId) && !it.hasOtherConstraints }
            if (!matches) {
                if (alert.selectors.any { it.stopId == null || (it.stopId in setOf(selection.originId,selection.destinationId) && it.hasOtherConstraints) }) unresolved++
                return@mapNotNull null
            }
            val periods = alert.impact.ifEmpty { alert.publication }
            if (periods.isNotEmpty() && periods.none { it.overlaps(reference.departure,until) }) return@mapNotNull null
            RelevantRailAlert(alert,alert.impact.isNotEmpty())
        }
        return RailContext(status,now,repository.sourceUrl,stamp,related,unresolved)
    }
}
