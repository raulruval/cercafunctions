package com.cercafunctions.domain.journey

sealed interface JourneyReadResult {
    data object NotConfigured : JourneyReadResult
    data class Configured(val journey: HabitualJourney) : JourneyReadResult
    data class Unavailable(val reason: JourneyStorageFailure) : JourneyReadResult
}

enum class JourneyStorageFailure { READ_FAILED, WRITE_FAILED, INVALID_STORED_DATA }
