package com.cercafunctions.presentation.home

import com.cercafunctions.domain.journey.HabitualJourney
import com.cercafunctions.domain.journey.JourneyField
import com.cercafunctions.domain.journey.JourneyStorageFailure

data class HomeUiState(
    val content: JourneyContent = JourneyContent.Loading,
    val editor: JourneyDraft? = null,
    val justSaved: Boolean = false,
)

sealed interface JourneyContent {
    data object Loading : JourneyContent
    data object NotConfigured : JourneyContent
    data class Ready(val journey: HabitualJourney) : JourneyContent
    data class Unavailable(val reason: JourneyStorageFailure) : JourneyContent
}

data class JourneyDraft(
    val origin: String = "",
    val destination: String = "",
    val durationMinutes: String = "",
    val errors: Set<JourneyField> = emptySet(),
    val isSaving: Boolean = false,
    val saveError: Boolean = false,
)
