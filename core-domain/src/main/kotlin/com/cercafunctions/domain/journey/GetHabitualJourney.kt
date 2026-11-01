package com.cercafunctions.domain.journey

/** Reads the same user configuration for UI and future capability adapters. */
class GetHabitualJourney(private val repository: JourneyRepository) {
    suspend operator fun invoke(): HabitualJourney? = repository.getHabitualJourney()
}
