package com.cercafunctions.domain.journey

/** Reads the same persisted configuration for every consumer. */
class GetHabitualJourney(private val repository: JourneyRepository) {
    suspend operator fun invoke(): JourneyReadResult = repository.getHabitualJourney()
}
