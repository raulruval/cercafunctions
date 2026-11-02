package com.cercafunctions.domain.estimate

import com.cercafunctions.domain.journey.*
import java.time.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Pure read-only capability shared by UI and future AppFunctions adapters. */
class QueryJourneyArrival(
    private val journeys: JourneyRepository,
    private val fixtures: JourneyFixtureRepository,
    private val clock: Clock,
) {
    fun tomorrow(zone: ZoneId): LocalDate = clock.instant().atZone(zone).toLocalDate().plusDays(1)
    fun fixture(scenario: ArrivalScenario): JourneyFixture? = fixtures.find(scenario)

    /** Demos always use their entire fixed dataset, never a fictitious delay on a real route. */
    suspend operator fun invoke(input: DepartureInput, scenario: ArrivalScenario = ArrivalScenario.REFERENCE): ArrivalResult {
        currentCoroutineContext().ensureActive()
        val saved = when (val read = journeys.getHabitualJourney()) {
            JourneyReadResult.NotConfigured -> return ArrivalResult.MissingJourney
            is JourneyReadResult.Unavailable -> return ArrivalResult.StorageUnavailable(read.reason)
            is JourneyReadResult.Configured -> read.journey
        }
        currentCoroutineContext().ensureActive()
        val demo = if (scenario == ArrivalScenario.REFERENCE) null else
            requireNotNull(fixtures.find(scenario)) { "Missing demonstration: $scenario" }
        val departureInput = demo?.departure ?: input
        val now = demo?.observedAt ?: clock.instant()
        val valid = departureInput.zone.rules.getValidOffsets(departureInput.local)
        if (valid.isEmpty()) return ArrivalResult.Gap
        if (departureInput.offset != null && departureInput.offset !in valid) return ArrivalResult.InvalidOffset
        if (valid.size > 1 && departureInput.offset == null) return ArrivalResult.Ambiguous(valid.toList())
        val offset = departureInput.offset ?: valid.single()
        val departure = ZonedDateTime.ofStrict(departureInput.local, offset, departureInput.zone).toInstant()
        if (departure.isBefore(now)) return ArrivalResult.PastDeparture
        val journey = demo?.journey ?: saved
        val arrival = if (demo != null && demo.delayMinutes == null) null else
            departure.plus(Duration.ofMinutes(journey.estimatedDurationMinutes.toLong() + (demo?.delayMinutes ?: 0)))
        return ArrivalResult.Estimated(ArrivalEstimate(journey, departure, arrival, departureInput.zone,
            if (demo == null) ArrivalSource.REFERENCE else ArrivalSource.FIXTURE, scenario, now, demo?.delayMinutes))
    }
}
