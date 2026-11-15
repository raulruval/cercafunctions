package com.cercafunctions.domain.journey

sealed interface JourneySaveResult {
    data class Saved(val journey: HabitualJourney) : JourneySaveResult
    data class InvalidInput(val fields: Set<JourneyField>) : JourneySaveResult
    data class Unavailable(val reason: JourneyStorageFailure) : JourneySaveResult
}

enum class JourneyField { ORIGIN, DESTINATION, DURATION }
