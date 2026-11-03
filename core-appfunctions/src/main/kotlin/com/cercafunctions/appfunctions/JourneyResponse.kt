package com.cercafunctions.appfunctions
import androidx.appfunctions.AppFunctionSerializable
import java.time.Instant
import java.time.ZoneId

/** Arrival evidence for one departure; a reference or fixture is not an operational forecast. */
@AppFunctionSerializable(isDescribedByKDoc=true)
data class JourneyResponse(
    /** Origin name of the saved journey, or the explicitly requested fixed demo. */
    val origin:String,
    /** Destination name of that journey. */
    val destination:String,
    /** Reference duration in minutes; excludes station-to-meeting travel. */
    val referenceDurationMinutes:Int,
    /** Effective departure instant. Demos retain their entire fixed date and clock. */
    val departure:Instant,
    /** Estimated station arrival, or null when no quantitative evidence exists. */
    val arrival:Instant?,
    /** Display zone of the effective departure and arrival. */
    val zone:ZoneId,
    /** REFERENCE uses the saved duration; FIXTURE is synthetic demonstration data. */
    val source:String,
    /** Effective REFERENCE, DEMO_NORMAL, DEMO_DELAYED or DEMO_UNKNOWN scenario. */
    val scenario:String,
    /** Instant when arrival evidence was calculated; fixed for demos, not a validity guarantee. */
    val observedAt:Instant,
    /** Synthetic delay only for known demos; null means unknown, never assumed zero. */
    val delayMinutes:Int?,
    /** Qualitative railway context that never modifies arrival or supplies delay minutes. */
    val rail:RailSummary,
)
/** Bounded qualitative summary; raw remote alert text and calendar data are excluded. */
@AppFunctionSerializable(isDescribedByKDoc=true)
data class RailSummary(
    /** AVAILABLE, UNRESOLVED, UNAVAILABLE, STALE, FRESHNESS_UNKNOWN, FUTURE_TIMESTAMP or DEMO_SKIPPED. */
    val status:String,
    /** Instant the railway context was consulted. */
    val consultedAt:Instant,
    /** Public source URL; not proof of coverage or freshness. */
    val sourceUrl:String,
    /** Feed publication instant when known, including rejected stale or invalid content. */
    val publishedAt:Instant?,
    /** Number of alerts matched under the existing endpoint and time-window rules. */
    val relevantAlertCount:Int,
    /** Number of unresolvable selectors; zero does not prove absence of disruption. */
    val unresolvedCount:Int,
    /** ENDPOINTS_ONLY: no claim about line, train, direction, direct service or intermediate stations. */
    val coverage:String,
    /** NETWORK, HTTP, INVALID_FORMAT or TOO_LARGE when unavailable; otherwise null. */
    val failure:String?,
)
