package com.cercafunctions.presentation.estimate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.journey.HabitualJourney
import com.cercafunctions.presentation.theme.CopilotTheme
import java.time.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ArrivalPanelTest {
    @get:Rule val compose = createComposeRule()
    private val zone = ZoneId.of("Europe/Madrid")
    private val journey = HabitualJourney("Demo A", "Demo B", 35)
    private val fixture = JourneyFixture(journey, DepartureInput(LocalDateTime.parse("2026-11-02T08:00"), zone), Instant.parse("2026-11-01T08:00:00Z"), 15)
    private val initial = ArrivalUiState("2026-11-02", zone = zone.id)
    private fun show(state: ArrivalUiState = initial, actions: ArrivalActions = ArrivalActions()) {
        compose.setContent { CopilotTheme { Column(Modifier.verticalScroll(rememberScrollState())) { ArrivalPanel(state, actions) } } }
    }
    @Test fun referenceRequiresExplicitConsultAndExposesInputs() {
        var consulted = 0
        var time = ""
        show(actions = ArrivalActions(onTime = { time = it }, onConsult = { consulted++ }))
        compose.onNodeWithTag("arrival_time").performScrollTo().performTextReplacement("09:00")
        assertEquals("09:00", time)
        assertEquals(0, consulted)
        compose.onNodeWithTag("arrival_consult").performScrollTo().performClick()
        assertEquals(1, consulted)
    }
    @Test fun fixtureInputsAreLockedAndSourceAndClockVisible() {
        show(initial.copy(scenario = ArrivalScenario.DEMO_DELAYED, fixture = fixture))
        compose.onNodeWithTag("arrival_date").assertIsNotEnabled()
        compose.onNodeWithTag("arrival_time").assertIsNotEnabled()
        compose.onNodeWithTag("arrival_zone").assertIsNotEnabled()
        compose.onNodeWithTag("arrival_tomorrow").assertIsNotEnabled()
        compose.onNodeWithTag("arrival_demo_info").assertTextContains("2026-11-01T08:00:00Z", substring = true)
        compose.onNodeWithTag("arrival_demo_info").assertTextContains("Demo A", substring = true)
    }
    @Test fun resultHasFullDateOffsetSourceAndHorizon() {
        val result = ArrivalResult.Estimated(ArrivalEstimate(journey, Instant.parse("2026-11-02T07:00:00Z"), Instant.parse("2026-11-02T07:50:00Z"), zone, ArrivalSource.FIXTURE, ArrivalScenario.DEMO_DELAYED, fixture.observedAt, 15))
        show(initial.copy(result = result, fixture = fixture, scenario = ArrivalScenario.DEMO_DELAYED))
        compose.onNodeWithTag("arrival_result").assertTextContains("2026-11-02 08:50 +01:00", substring = true)
        compose.onNodeWithTag("arrival_source").assertTextContains("Demostración", substring = true)
        compose.onNodeWithTag("arrival_horizon").assertExists()
        compose.onNodeWithTag("arrival_observed").assertTextContains(fixture.observedAt.toString(), substring = true)
    }
    @Test fun unknownShowsNoInventedArrival() {
        val result = ArrivalResult.Estimated(ArrivalEstimate(journey, Instant.parse("2026-11-02T07:00:00Z"), null, zone, ArrivalSource.FIXTURE, ArrivalScenario.DEMO_UNKNOWN, fixture.observedAt, null))
        show(initial.copy(result = result, fixture = fixture, scenario = ArrivalScenario.DEMO_UNKNOWN))
        compose.onNodeWithTag("arrival_result").assertTextContains("desconocida", substring = true)
        compose.onNodeWithTag("arrival_source").assertTextContains("Demostración", substring = true)
    }
    @Test fun ambiguousTimeOffersExplicitOffsetsWithoutAutoSelection() {
        val offsets = listOf(ZoneOffset.of("+02:00"), ZoneOffset.of("+01:00"))
        var selected: ZoneOffset? = null
        show(initial.copy(result = ArrivalResult.Ambiguous(offsets), offsets = offsets), ArrivalActions(onOffset = { selected = it }))
        assertNull(selected)
        compose.onNodeWithTag("arrival_offset_+01:00").performScrollTo().performClick()
        assertEquals(offsets[1], selected)
    }
}
