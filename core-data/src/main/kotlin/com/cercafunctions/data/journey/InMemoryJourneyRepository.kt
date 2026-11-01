package com.cercafunctions.data.journey

import com.cercafunctions.domain.journey.HabitualJourney
import com.cercafunctions.domain.journey.JourneyRepository

/** Bootstrap storage. Starts empty; no persistence or Renfe integration yet. */
class InMemoryJourneyRepository(
    private val initialJourney: HabitualJourney? = null,
) : JourneyRepository {
    override suspend fun getHabitualJourney(): HabitualJourney? = initialJourney
}
