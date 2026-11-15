package com.cercafunctions.data.estimate

import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.journey.HabitualJourney
import java.time.*

/** Synthetic data only. No operational data is read or inferred. */
class ReproducibleJourneyFixtures(private val clock: Clock) : JourneyFixtureRepository {
    override fun find(scenario: ArrivalScenario): JourneyFixture? {
        if (scenario == ArrivalScenario.REFERENCE) return null
        val now = clock.instant()
        val zone = ZoneId.of("Europe/Madrid")
        val tomorrow = now.atZone(zone).toLocalDate().plusDays(1)
        return JourneyFixture(HabitualJourney("Origen demo", "Destino demo", 35),
            DepartureInput(tomorrow.atTime(8, 0), zone),
            now, when (scenario) {
                ArrivalScenario.DEMO_NORMAL -> 0
                ArrivalScenario.DEMO_DELAYED -> 15
                ArrivalScenario.DEMO_UNKNOWN -> null
                ArrivalScenario.REFERENCE -> error("handled above")
            })
    }
}
