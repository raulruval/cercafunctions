package com.cercafunctions.domain.estimate

import com.cercafunctions.domain.journey.HabitualJourney
import com.cercafunctions.domain.journey.JourneyStorageFailure
import java.time.*

enum class ArrivalScenario { REFERENCE, DEMO_NORMAL, DEMO_DELAYED, DEMO_UNKNOWN }
enum class ArrivalSource { REFERENCE, FIXTURE }

data class DepartureInput(val local: LocalDateTime, val zone: ZoneId, val offset: ZoneOffset? = null)

/** A complete, reproducible demonstration. Null delay means no estimate. */
data class JourneyFixture(
    val journey: HabitualJourney,
    val departure: DepartureInput,
    val observedAt: Instant,
    val delayMinutes: Int?,
) {
    init { require(delayMinutes == null || delayMinutes >= 0) }
}

fun interface JourneyFixtureRepository {
    /** REFERENCE has no fixture. All demonstration scenarios must be present. */
    fun find(scenario: ArrivalScenario): JourneyFixture?
}

/** Applies only to [departure]; no timetable, operational forecast or validity TTL. */
data class ArrivalEstimate(
    val journey: HabitualJourney,
    val departure: Instant,
    val arrival: Instant?,
    val zone: ZoneId,
    val source: ArrivalSource,
    val scenario: ArrivalScenario,
    val observedAt: Instant,
    val delayMinutes: Int?,
)

sealed interface ArrivalResult {
    data class Estimated(val value: ArrivalEstimate) : ArrivalResult
    data object MissingJourney : ArrivalResult
    data class StorageUnavailable(val reason: JourneyStorageFailure) : ArrivalResult
    data object Gap : ArrivalResult
    data class Ambiguous(val offsets: List<ZoneOffset>) : ArrivalResult
    data object InvalidOffset : ArrivalResult
    data object PastDeparture : ArrivalResult
}
