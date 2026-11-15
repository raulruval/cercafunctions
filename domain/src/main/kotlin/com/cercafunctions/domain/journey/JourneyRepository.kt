package com.cercafunctions.domain.journey

interface JourneyRepository {
    suspend fun getHabitualJourney(): JourneyReadResult
    suspend fun saveHabitualJourney(journey: HabitualJourney): JourneySaveResult
}
