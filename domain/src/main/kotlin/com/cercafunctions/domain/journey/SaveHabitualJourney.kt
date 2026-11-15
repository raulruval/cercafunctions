package com.cercafunctions.domain.journey

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Validates before any write. All consumers share the same business rules. */
class SaveHabitualJourney(private val repository: JourneyRepository) {
    suspend operator fun invoke(origin: String, destination: String, durationMinutes: Int): JourneySaveResult {
        currentCoroutineContext().ensureActive()
        val normalizedOrigin = origin.trim()
        val normalizedDestination = destination.trim()
        val invalid = buildSet {
            if (normalizedOrigin.isBlank()) add(JourneyField.ORIGIN)
            if (normalizedDestination.isBlank()) add(JourneyField.DESTINATION)
            if (normalizedOrigin.isNotBlank() && normalizedOrigin.equals(normalizedDestination, ignoreCase = true)) {
                add(JourneyField.ORIGIN)
                add(JourneyField.DESTINATION)
            }
            if (durationMinutes !in 1..1440) add(JourneyField.DURATION)
        }
        if (invalid.isNotEmpty()) return JourneySaveResult.InvalidInput(invalid)
        return repository.saveHabitualJourney(HabitualJourney(normalizedOrigin, normalizedDestination, durationMinutes))
    }
}
