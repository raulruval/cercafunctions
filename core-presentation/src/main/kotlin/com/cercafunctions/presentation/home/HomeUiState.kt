package com.cercafunctions.presentation.home

import com.cercafunctions.domain.journey.HabitualJourney

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object NotConfigured : HomeUiState
    data class Ready(val journey: HabitualJourney) : HomeUiState
    data object Error : HomeUiState
}
