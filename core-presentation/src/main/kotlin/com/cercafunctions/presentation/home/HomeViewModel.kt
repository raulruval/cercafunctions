package com.cercafunctions.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cercafunctions.domain.journey.GetHabitualJourney
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHabitualJourney: GetHabitualJourney,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadStarted = false

    init { refresh() }

    fun refresh() {
        if (_uiState.value == HomeUiState.Loading && loadStarted) return
        loadStarted = true
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                getHabitualJourney()?.let(HomeUiState::Ready) ?: HomeUiState.NotConfigured
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                HomeUiState.Error
            }
        }
    }

}
