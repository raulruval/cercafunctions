package com.cercafunctions.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Real Activity/Hilt/storage graph; fixtures remain deterministic on any device date. */
@RunWith(AndroidJUnit4::class)
class ArrivalFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Before fun openAdvancedScreen() {
        compose.onNodeWithTag("demo_advanced").performScrollTo().performClick()
    }


    @Test fun referenceAndAllDemosThenInvalidateAfterEditingJourney() {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("configure").fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodesWithTag("edit").fetchSemanticsNodes().isNotEmpty()
        }
        val action = if (compose.onAllNodesWithTag("configure").fetchSemanticsNodes().isNotEmpty()) "configure" else "edit"
        compose.onNodeWithTag(action).performScrollTo().performClick()
        compose.onNodeWithTag("origin").performScrollTo().performTextReplacement("Origen de prueba")
        compose.onNodeWithTag("destination").performScrollTo().performTextReplacement("Destino de prueba")
        compose.onNodeWithTag("duration").performScrollTo().performTextReplacement("35")
        compose.onNodeWithTag("save").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("saved_confirmation").fetchSemanticsNodes().isNotEmpty() }

        compose.onNodeWithTag("arrival_zone").performScrollTo().performTextReplacement("Europe/Madrid")
        compose.onNodeWithTag("arrival_tomorrow").performScrollTo().performClick()
        compose.onNodeWithTag("arrival_time").performScrollTo().performTextReplacement("08:00")
        compose.onNodeWithTag("arrival_consult").performScrollTo().performClick()
        waitResult()
        compose.onNodeWithTag("arrival_result").assertTextContains("08:35", substring = true)
        compose.onNodeWithTag("arrival_source").assertTextContains("Referencia", substring = true)
        compose.onNodeWithTag("arrival_time").performScrollTo().performTextReplacement("09:00")
        compose.onNodeWithTag("arrival_result").assertDoesNotExist()

        for ((mode, expected) in listOf("DEMO_NORMAL" to "08:35", "DEMO_DELAYED" to "08:50", "DEMO_UNKNOWN" to "desconocida")) {
            compose.onNodeWithTag("arrival_mode_$mode").performScrollTo().performClick()
            compose.onNodeWithTag("arrival_result").assertDoesNotExist()
            compose.onNodeWithTag("arrival_date").assertTextContains(java.time.LocalDate.now(java.time.ZoneId.of("Europe/Madrid")).plusDays(1).toString())
            compose.onNodeWithTag("arrival_date").assertIsNotEnabled()
            compose.onNodeWithTag("arrival_consult").performScrollTo().performClick()
            waitResult()
            compose.onNodeWithTag("arrival_result").assertTextContains(expected, substring = true)
            compose.onNodeWithTag("arrival_source").assertTextContains("Demostración", substring = true)
            compose.onNodeWithTag("arrival_observed").assertExists()
        }
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("arrival_result").assertExists()
        compose.onNodeWithTag("edit").performScrollTo().performClick()
        compose.onNodeWithTag("duration").performScrollTo().performTextReplacement("40")
        compose.onNodeWithTag("save").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("saved_confirmation").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("arrival_result").assertDoesNotExist()
        compose.onNodeWithTag("arrival_mode_REFERENCE").performScrollTo().performClick()
        compose.onNodeWithTag("arrival_time").assertTextContains("09:00")
        compose.onNodeWithTag("arrival_consult").performScrollTo().performClick()
        waitResult()
        compose.onNodeWithTag("arrival_result").assertTextContains("09:40", substring = true)
    }
    private fun waitResult() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("arrival_result").fetchSemanticsNodes().isNotEmpty() }
    }
}
