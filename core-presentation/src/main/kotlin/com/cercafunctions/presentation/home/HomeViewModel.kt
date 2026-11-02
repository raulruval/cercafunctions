package com.cercafunctions.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cercafunctions.domain.journey.GetHabitualJourney
import com.cercafunctions.domain.journey.JourneyField
import com.cercafunctions.domain.journey.JourneyReadResult
import com.cercafunctions.domain.journey.JourneySaveResult
import com.cercafunctions.domain.journey.JourneyStorageFailure
import com.cercafunctions.domain.journey.SaveHabitualJourney
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHabitualJourney: GetHabitualJourney,
    private val saveHabitualJourney: SaveHabitualJourney,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init { refresh() }

    fun refresh() {
        if (loadJob?.isActive == true || _uiState.value.editor != null) return
        _uiState.update { it.copy(content = JourneyContent.Loading, justSaved = false) }
        loadJob = viewModelScope.launch {
            val content = try {
                when (val result = getHabitualJourney()) {
                    JourneyReadResult.NotConfigured -> JourneyContent.NotConfigured
                    is JourneyReadResult.Configured -> JourneyContent.Ready(result.journey)
                    is JourneyReadResult.Unavailable -> JourneyContent.Unavailable(result.reason)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                JourneyContent.Unavailable(JourneyStorageFailure.READ_FAILED)
            }
            _uiState.update { it.copy(content = content) }
        }
    }

    fun beginEditing() {
        val state = _uiState.value
        if (state.editor != null) return
        val draft = when (val content = state.content) {
            JourneyContent.NotConfigured -> JourneyDraft()
            is JourneyContent.Ready -> JourneyDraft(content.journey.origin, content.journey.destination, content.journey.estimatedDurationMinutes.toString())
            else -> return
        }
        _uiState.value = state.copy(editor = draft, justSaved = false)
    }

    fun cancelEditing() {
        if (_uiState.value.editor?.isSaving == true) return
        _uiState.update { it.copy(editor = null) }
    }

    fun updateOrigin(value: String) = edit(JourneyField.ORIGIN) { it.copy(origin = value) }
    fun updateDestination(value: String) = edit(JourneyField.DESTINATION) { it.copy(destination = value) }
    fun updateDuration(value: String) = edit(JourneyField.DURATION) { it.copy(durationMinutes = value) }

    private fun edit(field: JourneyField, transform: (JourneyDraft) -> JourneyDraft) {
        _uiState.update { state ->
            val draft = state.editor
            if (draft == null || draft.isSaving) state
            else state.copy(editor = transform(draft).copy(errors = draft.errors - field, saveError = false))
        }
    }

    fun save() {
        val draft = _uiState.value.editor ?: return
        if (draft.isSaving) return
        val duration = draft.durationMinutes.trim().toIntOrNull()
        if (duration == null) {
            _uiState.update { it.copy(editor = draft.copy(errors = draft.errors + JourneyField.DURATION, saveError = false)) }
            return
        }
        // Update synchronously: the next tap sees Saving before any suspension.
        _uiState.update { it.copy(editor = draft.copy(isSaving = true, errors = emptySet(), saveError = false), justSaved = false) }
        viewModelScope.launch {
            try {
                when (val result = saveHabitualJourney(draft.origin, draft.destination, duration)) {
                    is JourneySaveResult.Saved -> _uiState.value = HomeUiState(JourneyContent.Ready(result.journey), justSaved = true)
                    is JourneySaveResult.InvalidInput -> _uiState.update { it.copy(editor = draft.copy(errors = result.fields)) }
                    is JourneySaveResult.Unavailable -> _uiState.update { it.copy(editor = draft.copy(saveError = true)) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiState.update { it.copy(editor = draft.copy(saveError = true)) }
            } finally {
                _uiState.update { it.copy(editor = it.editor?.copy(isSaving = false)) }
            }
        }
    }
}
