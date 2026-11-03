package com.cercafunctions.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Real graph, deterministic validation. Does not contact the public endpoint. */
@RunWith(AndroidJUnit4::class)
class RailContextFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun stationAssociationRequiresSelectionAndSurvivesRotationButNotNewQuery() {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("configure").fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodesWithTag("edit").fetchSemanticsNodes().isNotEmpty()
        }
        val action=if(compose.onAllNodesWithTag("configure").fetchSemanticsNodes().isNotEmpty())"configure" else "edit"
        compose.onNodeWithTag(action).performScrollTo().performClick()
        compose.onNodeWithTag("origin").performScrollTo().performTextReplacement("Vicálvaro")
        compose.onNodeWithTag("destination").performScrollTo().performTextReplacement("Coslada")
        compose.onNodeWithTag("duration").performScrollTo().performTextReplacement("35")
        compose.onNodeWithTag("save").performScrollTo().performClick()
        compose.waitUntil(10_000){compose.onAllNodesWithTag("saved_confirmation").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithTag("arrival_tomorrow").performScrollTo().performClick()
        compose.onNodeWithTag("arrival_consult").performScrollTo().performClick()
        compose.waitUntil(10_000){compose.onAllNodesWithTag("rail_consult").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithTag("rail_consult").performScrollTo().performClick()
        compose.waitUntil(10_000){compose.onAllNodesWithTag("rail_status").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithTag("rail_status").assertTextContains("Cobertura no resuelta",substring=true)
        compose.onNodeWithTag("rail_origin").performScrollTo().performTextReplacement("70100")
        compose.onNodeWithTag("rail_origin_70100").performScrollTo().performClick()
        compose.waitUntil(10_000){compose.onAllNodesWithTag("rail_origin").fetchSemanticsNodes().any{hasText("Vicálvaro · 70100",substring=true).matches(it)}}
        compose.onNodeWithTag("rail_destination").performScrollTo().performTextReplacement("70108")
        compose.onNodeWithTag("rail_destination_70108").performScrollTo().performClick()
        compose.waitUntil(10_000){compose.onAllNodesWithTag("rail_destination").fetchSemanticsNodes().any{hasText("Coslada · 70108",substring=true).matches(it)}}
        compose.onNodeWithTag("rail_status").assertDoesNotExist()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("rail_origin").assertTextContains("Vicálvaro · 70100")
        compose.onNodeWithTag("rail_destination").assertTextContains("Coslada · 70108")
        compose.onNodeWithTag("arrival_mode_DEMO_DELAYED").performScrollTo().performClick()
        compose.onNodeWithTag("rail_consult").assertDoesNotExist()
        compose.onNodeWithTag("arrival_consult").performScrollTo().performClick()
        compose.waitUntil(10_000){compose.onAllNodesWithTag("rail_demo").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithTag("rail_demo").assertExists()
        compose.onNodeWithTag("rail_consult").assertDoesNotExist()
        compose.onNodeWithTag("arrival_mode_REFERENCE").performScrollTo().performClick()
        compose.onNodeWithTag("arrival_consult").performScrollTo().performClick()
        compose.waitUntil(10_000){compose.onAllNodesWithTag("rail_consult").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithTag("rail_origin").assertTextEquals("Estación de origen (nombre o ID)","")
    }
}
