package com.cercafunctions.data.estimate

import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.journey.HabitualJourney
import java.time.*

/** Synthetic data only. No operational data is read or inferred. */
class ReproducibleJourneyFixtures : JourneyFixtureRepository {
    override fun find(scenario: ArrivalScenario): JourneyFixture? {
        if (scenario == ArrivalScenario.REFERENCE) return null
        return JourneyFixture(HabitualJourney("Origen demo", "Destino demo", 35),
            DepartureInput(LocalDateTime.of(2026, 11, 2, 8, 0), ZoneId.of("Europe/Madrid")),
            Instant.parse("2026-11-01T08:00:00Z"), when (scenario) {
                ArrivalScenario.DEMO_NORMAL -> 0
                ArrivalScenario.DEMO_DELAYED -> 15
                ArrivalScenario.DEMO_UNKNOWN -> null
                ArrivalScenario.REFERENCE -> error("handled above")
            })
    }
}
