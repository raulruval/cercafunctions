package com.cercafunctions.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the real Activity, Hilt graph, ViewModel and persistent repository. */
@RunWith(AndroidJUnit4::class)
class JourneyFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun configureRotateDiscardEditAndSaveThroughRealGraph() {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("configure").fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodesWithTag("edit").fetchSemanticsNodes().isNotEmpty()
        }
        val action = if (compose.onAllNodesWithTag("configure").fetchSemanticsNodes().isNotEmpty()) "configure" else "edit"
        compose.onNodeWithTag(action).performScrollTo().performClick()
        compose.onNodeWithTag("origin").performScrollTo().performTextReplacement("Origen de prueba")
        compose.onNodeWithTag("destination").performScrollTo().performTextReplacement("Destino de prueba")
        compose.onNodeWithTag("duration").performScrollTo().performTextReplacement("35")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("duration").assertTextContains("35")
        compose.onNodeWithTag("save").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("saved_confirmation").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("saved_confirmation").assertExists()
        compose.onNodeWithText("Duración de referencia: 35 min").assertExists()

        compose.onNodeWithTag("edit").performScrollTo().performClick()
        compose.onNodeWithTag("duration").performScrollTo().performTextReplacement("40")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("duration").assertTextContains("40")
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithTag("duration").assertDoesNotExist()
        compose.onNodeWithText("Duración de referencia: 35 min").assertExists()

        compose.onNodeWithTag("edit").performScrollTo().performClick()
        compose.onNodeWithTag("duration").assertTextContains("35")
        compose.onNodeWithTag("duration").performScrollTo().performTextReplacement("40")
        compose.onNodeWithTag("save").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("saved_confirmation").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("saved_confirmation").assertExists()
        compose.onNodeWithText("Duración de referencia: 40 min").assertExists()
    }
}
