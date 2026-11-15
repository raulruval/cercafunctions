package com.cercafunctions.domain.rail

import java.time.Instant

data class RailStation(val id: String, val name: String)
data class RailSelection(val originId: String, val destinationId: String)
fun interface RailStationCatalog { fun stations(): List<RailStation> }

/** Half-open interval. Absent bounds are unbounded; both absent is invalid. */
data class AlertPeriod(val start: Instant?, val end: Instant?) {
    init { require(start != null || end != null); require(start == null || end == null || start < end) }
    fun overlaps(from: Instant, until: Instant): Boolean = (end == null || from < end) && (start == null || start < until)
}
/** Other AND constraints must never be silently discarded when matching a stop. */
data class RailSelector(val stopId: String?, val hasOtherConstraints: Boolean)
data class RailAlert(val id: String, val text: String, val effect: String?,
    val selectors: List<RailSelector>, val publication: List<AlertPeriod>, val impact: List<AlertPeriod>)
data class RailFeed(val publishedAt: Instant?, val alerts: List<RailAlert>)
enum class RailFailure { NETWORK, HTTP, INVALID_FORMAT, TOO_LARGE }
sealed interface RailFeedRead {
    data class Success(val feed: RailFeed) : RailFeedRead
    data class Failure(val reason: RailFailure, val publishedAt: Instant? = null) : RailFeedRead
}
interface RailContextRepository {
    val sourceUrl: String
    suspend fun read(): RailFeedRead
}
enum class RailStatus { AVAILABLE, UNRESOLVED, UNAVAILABLE, STALE, FRESHNESS_UNKNOWN, FUTURE_TIMESTAMP, DEMO_SKIPPED }
data class RelevantRailAlert(val alert: RailAlert, val impactConfirmed: Boolean)
/** Qualitative context only. No operation here produces minutes or a replacement arrival. */
data class RailContext(val status: RailStatus, val consultedAt: Instant, val sourceUrl: String,
    val publishedAt: Instant? = null, val alerts: List<RelevantRailAlert> = emptyList(),
    val unresolvedCount: Int = 0, val failure: RailFailure? = null)
