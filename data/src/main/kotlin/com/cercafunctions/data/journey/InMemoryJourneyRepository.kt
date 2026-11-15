package com.cercafunctions.data.journey

import com.cercafunctions.domain.journey.HabitualJourney
import com.cercafunctions.domain.journey.JourneyReadResult
import com.cercafunctions.domain.journey.JourneyRepository
import com.cercafunctions.domain.journey.JourneySaveResult
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow

/** Volatile storage for fixtures. The application uses the persistent adapter. */
class InMemoryJourneyRepository(initialJourney: HabitualJourney? = null) : JourneyRepository {
    private val journey = MutableStateFlow(initialJourney)

    override suspend fun getHabitualJourney(): JourneyReadResult {
        currentCoroutineContext().ensureActive()
        return journey.value?.let(JourneyReadResult::Configured) ?: JourneyReadResult.NotConfigured
    }

    override suspend fun saveHabitualJourney(journey: HabitualJourney): JourneySaveResult {
        currentCoroutineContext().ensureActive()
        this.journey.value = journey
        return JourneySaveResult.Saved(journey)
    }
}
