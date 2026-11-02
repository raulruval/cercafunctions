package com.cercafunctions.domain.journey

/** A normalized, user-provided baseline; never a prediction of rail service. */
data class HabitualJourney(
    val origin: String,
    val destination: String,
    val estimatedDurationMinutes: Int,
) {
    init {
        require(origin.isNotBlank() && origin == origin.trim()) { "Origin must be normalized" }
        require(destination.isNotBlank() && destination == destination.trim()) { "Destination must be normalized" }
        require(!origin.equals(destination, ignoreCase = true)) { "Stations must differ" }
        require(estimatedDurationMinutes in 1..1440) { "Duration must be between 1 and 1440 minutes" }
    }
}
