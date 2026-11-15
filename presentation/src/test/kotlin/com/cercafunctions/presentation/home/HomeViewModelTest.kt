package com.cercafunctions.presentation.home

import androidx.lifecycle.ViewModelStore
import com.cercafunctions.domain.journey.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val models = ViewModelStore()
    private var modelCount = 0
    private val journey = HabitualJourney("A", "B", 35)
    private fun model(repository: ControlledRepository): HomeViewModel =
        HomeViewModel(GetHabitualJourney(repository), SaveHabitualJourney(repository)).also { models.put("${modelCount++}", it) }
    @After fun clear() { models.clear() }

    @Test fun `load distinguishes absent and unavailable`() = runTest {
        val repository = ControlledRepository()
        val model = model(repository)
        assertEquals(JourneyContent.Loading, model.uiState.value.content)
        runCurrent()
        assertEquals(JourneyContent.NotConfigured, model.uiState.value.content)
        repository.readResult = JourneyReadResult.Unavailable(JourneyStorageFailure.READ_FAILED)
        model.refresh(); runCurrent()
        assertEquals(JourneyContent.Unavailable(JourneyStorageFailure.READ_FAILED), model.uiState.value.content)
        model.beginEditing()
        assertNull(model.uiState.value.editor)
    }

    @Test fun `edit starts with saved values and cancel discards draft`() = runTest {
        val model = model(ControlledRepository(JourneyReadResult.Configured(journey)))
        runCurrent(); model.beginEditing()
        assertEquals(JourneyDraft("A", "B", "35"), model.uiState.value.editor)
        model.updateDuration("40"); model.cancelEditing()
        assertNull(model.uiState.value.editor)
        assertEquals(JourneyContent.Ready(journey), model.uiState.value.content)
        model.beginEditing()
        assertEquals("35", model.uiState.value.editor?.durationMinutes)
    }

    @Test fun `non integer text is rejected without calling repository`() = runTest {
        val repository = ControlledRepository()
        val model = model(repository)
        runCurrent(); model.beginEditing(); model.updateOrigin("A"); model.updateDestination("B")
        for (input in listOf("", "2.5", "abc", "99999999999999999999999")) {
            model.updateDuration(input); model.save(); runCurrent()
            assertEquals(setOf(JourneyField.DURATION), model.uiState.value.editor?.errors)
        }
        assertEquals(0, repository.saves)
    }

    @Test fun `domain validation returns field errors without losing text`() = runTest {
        val repository = ControlledRepository()
        val model = model(repository)
        runCurrent(); model.beginEditing(); model.updateOrigin(" A "); model.updateDestination("a"); model.updateDuration("1441")
        model.save(); runCurrent()
        assertEquals(setOf(JourneyField.ORIGIN, JourneyField.DESTINATION, JourneyField.DURATION), model.uiState.value.editor?.errors)
        assertEquals(" A ", model.uiState.value.editor?.origin)
        assertEquals(0, repository.saves)
    }

    @Test fun `guard rejects double save and success waits for storage`() = runTest {
        val repository = ControlledRepository().apply { saveGate = CompletableDeferred() }
        val model = model(repository)
        runCurrent(); model.beginEditing(); model.updateOrigin(" A "); model.updateDestination("B"); model.updateDuration("35")
        model.save(); model.save(); runCurrent()
        assertEquals(1, repository.saves)
        assertEquals(true, model.uiState.value.editor?.isSaving)
        assertFalse(model.uiState.value.justSaved)
        model.cancelEditing(); model.updateOrigin("other")
        assertEquals(" A ", model.uiState.value.editor?.origin)
        repository.saveGate!!.complete(JourneySaveResult.Saved(journey)); runCurrent()
        assertEquals(JourneyContent.Ready(journey), model.uiState.value.content)
        assertNull(model.uiState.value.editor)
        assertTrue(model.uiState.value.justSaved)
    }

    @Test fun `failed write preserves draft and can be retried`() = runTest {
        val repository = ControlledRepository(JourneyReadResult.Configured(journey)).apply {
            saveResult = JourneySaveResult.Unavailable(JourneyStorageFailure.WRITE_FAILED)
        }
        val model = model(repository)
        runCurrent(); model.beginEditing(); model.updateDuration("40"); model.save(); runCurrent()
        assertEquals("40", model.uiState.value.editor?.durationMinutes)
        assertEquals(true, model.uiState.value.editor?.saveError)
        assertEquals(JourneyContent.Ready(journey), model.uiState.value.content)
        assertFalse(model.uiState.value.justSaved)
        repository.saveResult = null
        model.save(); runCurrent()
        assertEquals(JourneyContent.Ready(journey.copy(estimatedDurationMinutes = 40)), model.uiState.value.content)
    }

    @Test fun `read cancellation is not shown as failure`() = runTest {
        val model = model(ControlledRepository().apply { cancelRead = true })
        runCurrent()
        assertEquals(JourneyContent.Loading, model.uiState.value.content)
    }

    @Test fun `save cancellation is not shown as error or success`() = runTest {
        val repository = ControlledRepository().apply { cancelSave = true }
        val model = model(repository)
        runCurrent(); model.beginEditing(); model.updateOrigin("A"); model.updateDestination("B"); model.updateDuration("35")
        model.save(); runCurrent()
        assertEquals(false, model.uiState.value.editor?.isSaving)
        assertEquals(false, model.uiState.value.editor?.saveError)
        assertFalse(model.uiState.value.justSaved)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    val dispatcher = StandardTestDispatcher()
    override fun starting(description: Description) { Dispatchers.setMain(dispatcher) }
    override fun finished(description: Description) { Dispatchers.resetMain() }
}

private class ControlledRepository(var readResult: JourneyReadResult = JourneyReadResult.NotConfigured) : JourneyRepository {
    var saveGate: CompletableDeferred<JourneySaveResult>? = null
    var saveResult: JourneySaveResult? = null
    var cancelRead = false
    var cancelSave = false
    var saves = 0
    override suspend fun getHabitualJourney(): JourneyReadResult {
        if (cancelRead) throw CancellationException("injected")
        return readResult
    }
    override suspend fun saveHabitualJourney(journey: HabitualJourney): JourneySaveResult {
        saves++
        if (cancelSave) throw CancellationException("injected")
        val result = saveGate?.await() ?: saveResult ?: JourneySaveResult.Saved(journey)
        if (result is JourneySaveResult.Saved) readResult = JourneyReadResult.Configured(result.journey)
        return result
    }
}
