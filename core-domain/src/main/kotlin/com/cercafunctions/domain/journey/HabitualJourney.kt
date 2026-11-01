package com.cercafunctions.domain.journey

/** A user-provided duration is a baseline, never a live rail prediction. */
data class HabitualJourney(
    val origin: String,
    val destination: String,
    val estimatedDurationMinutes: Int,
) {
    init {
        require(origin.isNotBlank()) { "Origin must not be blank" }
        require(destination.isNotBlank()) { "Destination must not be blank" }
        require(estimatedDurationMinutes > 0) { "Duration must be positive" }
    }
}
