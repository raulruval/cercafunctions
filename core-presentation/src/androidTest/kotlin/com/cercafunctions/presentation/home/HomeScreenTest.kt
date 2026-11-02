package com.cercafunctions.presentation.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cercafunctions.domain.journey.*
import com.cercafunctions.presentation.theme.CopilotTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule val compose = createComposeRule()
    private val journey = HabitualJourney("A", "B", 35)
    private fun show(state: HomeUiState, actions: HomeActions = HomeActions()) {
        compose.setContent { CopilotTheme { HomeScreen(state, actions) } }
    }

    @Test fun emptyStateOffersConfiguration() {
        var clicked = false
        show(HomeUiState(JourneyContent.NotConfigured), HomeActions(onEdit = { clicked = true }))
        compose.onNodeWithTag("configure").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(clicked) }
    }

    @Test fun formEmitsTheThreeValuesAndSave() {
        val state = mutableStateOf(HomeUiState(JourneyContent.NotConfigured, JourneyDraft()))
        var saves = 0
        val actions = HomeActions(
            onOriginChange = { state.value = state.value.copy(editor = state.value.editor!!.copy(origin = it)) },
            onDestinationChange = { state.value = state.value.copy(editor = state.value.editor!!.copy(destination = it)) },
            onDurationChange = { state.value = state.value.copy(editor = state.value.editor!!.copy(durationMinutes = it)) },
            onSave = { saves++ },
        )
        compose.setContent { CopilotTheme { HomeScreen(state.value, actions) } }
        compose.onNodeWithTag("origin").performScrollTo().performTextInput("A")
        compose.onNodeWithTag("destination").performScrollTo().performTextInput("B")
        compose.onNodeWithTag("duration").performScrollTo().performTextInput("35")
        compose.onNodeWithTag("save").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(JourneyDraft("A", "B", "35"), state.value.editor)
            assertEquals(1, saves)
        }
    }

    @Test fun invalidNamesAndDurationAreExplained() {
        val draft = JourneyDraft("A", "a", "1441", errors = JourneyField.entries.toSet())
        show(HomeUiState(JourneyContent.NotConfigured, draft))
        compose.onNodeWithTag("origin").assertIsDisplayed()
        compose.onNodeWithTag("origin_error", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("destination_error", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("duration_error", useUnmergedTree = true).assertExists()
    }

    @Test fun readFailureCannotStartBlankConfiguration() {
        var retried = false
        show(HomeUiState(JourneyContent.Unavailable(JourneyStorageFailure.INVALID_STORED_DATA)), HomeActions(onRetry = { retried = true }))
        compose.onNodeWithTag("read_error").assertExists()
        compose.onNodeWithTag("configure").assertDoesNotExist()
        compose.onNodeWithTag("retry").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(retried) }
    }

    @Test fun failedSaveKeepsTheDraftAndDoesNotShowSuccess() {
        show(HomeUiState(JourneyContent.Ready(journey), JourneyDraft("A", "B", "40", saveError = true)))
        compose.onNodeWithTag("duration").assertTextContains("40")
        compose.onNodeWithTag("save_error").assertExists()
        compose.onNodeWithTag("saved_confirmation").assertDoesNotExist()
        compose.onNodeWithTag("save").assertIsEnabled()
    }

    @Test fun savingDisablesDuplicateActionsAndEditing() {
        show(HomeUiState(JourneyContent.NotConfigured, JourneyDraft("A", "B", "35", isSaving = true)))
        compose.onNodeWithTag("save").assertIsNotEnabled()
        compose.onNodeWithTag("cancel").assertIsNotEnabled()
        compose.onNodeWithTag("origin").assertIsNotEnabled()
    }

    @Test fun savedSummaryOffersEditAndLabelsBaseline() {
        var edited = false
        show(HomeUiState(JourneyContent.Ready(journey), justSaved = true), HomeActions(onEdit = { edited = true }))
        compose.onNodeWithTag("saved_confirmation").assertExists()
        compose.onNodeWithText("Duración de referencia: 35 min").assertExists()
        compose.onNodeWithTag("edit").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(edited) }
    }

    @Test fun cancelLeavesTheConfirmedSummaryIntact() {
        val state = mutableStateOf(HomeUiState(JourneyContent.Ready(journey), JourneyDraft("A", "B", "40")))
        compose.setContent {
            CopilotTheme { HomeScreen(state.value, HomeActions(onCancel = { state.value = state.value.copy(editor = null) })) }
        }
        compose.onNodeWithTag("cancel").performScrollTo().performClick()
        compose.onNodeWithText("Duración de referencia: 35 min").assertExists()
        compose.onNodeWithTag("duration").assertDoesNotExist()
    }
}
