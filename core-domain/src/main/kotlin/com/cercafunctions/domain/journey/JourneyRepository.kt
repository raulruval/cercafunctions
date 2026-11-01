package com.cercafunctions.domain.journey

interface JourneyRepository {
    /** Returns null when no habitual journey has been configured. */
    suspend fun getHabitualJourney(): HabitualJourney?
}
