package com.cercafunctions.presentation.rail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cercafunctions.domain.rail.*
import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.journey.HabitualJourney
import com.cercafunctions.presentation.theme.CopilotTheme
import java.time.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class RailContextPanelTest {
    @get:Rule val compose=createComposeRule()
    private val now=Instant.parse("2026-11-02T07:00:00Z")
    private val arrival=ArrivalEstimate(HabitualJourney("A","B",35),now.plusSeconds(86400),now.plusSeconds(88500),ZoneOffset.UTC,ArrivalSource.REFERENCE,ArrivalScenario.REFERENCE,now,null)
    private fun show(state:RailUiState,actions:RailActions=RailActions(),suggestions:(String)->List<RailStation> ={emptyList()}) {
        compose.setContent {CopilotTheme {Column(Modifier.verticalScroll(rememberScrollState())){RailContextPanel(state,actions,suggestions)}}}
    }
    @Test fun explicitStationSelectionUsesIdentifiedSuggestion() {
        var chosen=""
        show(RailUiState(arrival,originText="70100"),RailActions(onSelectOrigin={chosen=it})) {listOf(RailStation("70100","Vicálvaro"))}
        assertEquals("",chosen)
        compose.onNodeWithTag("rail_origin_70100").performScrollTo().performClick()
        assertEquals("70100",chosen)
        compose.onNodeWithTag("rail_coverage").assertExists()
    }
    @Test fun staleSourceDisplaysBothTimesAndNeverClaimsNoIncidents() {
        show(RailUiState(arrival,result=RailContext(RailStatus.STALE,now,"https://gtfsrt.renfe.com/alerts.json",now.minusSeconds(86400))))
        compose.onNodeWithTag("rail_status").assertTextContains("caducado",substring=true)
        compose.onNodeWithTag("rail_times").assertTextContains("2026-11-01",substring=true)
        compose.onNodeWithTag("rail_times").assertTextContains("2026-11-02",substring=true)
        compose.onNodeWithTag("rail_empty").assertDoesNotExist()
        compose.onNodeWithTag("rail_source").assertTextContains("Renfe Operadora",substring=true)
    }
    @Test fun noMatchesIsExplicitlyPartialAndDoesNotAssertPunctuality() {
        show(RailUiState(arrival,result=RailContext(RailStatus.AVAILABLE,now,"https://gtfsrt.renfe.com/alerts.json",now)))
        compose.onNodeWithTag("rail_empty").assertTextContains("no demuestra puntualidad",substring=true)
    }
    @Test fun demoOffersNoNetworkAction() {
        show(RailUiState(arrival.copy(source=ArrivalSource.FIXTURE,scenario=ArrivalScenario.DEMO_DELAYED)))
        compose.onNodeWithTag("rail_demo").assertExists()
        compose.onNodeWithTag("rail_consult").assertDoesNotExist()
    }
    @Test fun unknownImpactIsClearlyMarkedAndHasNoDelayMinutes() {
        val alert=RailAlert("id","Ascensor sin servicio","ACCESSIBILITY_ISSUE",listOf(RailSelector("70100",false)),listOf(AlertPeriod(now,null)),emptyList())
        show(RailUiState(arrival,result=RailContext(RailStatus.AVAILABLE,now,"https://gtfsrt.renfe.com/alerts.json",now,listOf(RelevantRailAlert(alert,false)))))
        compose.onNodeWithTag("rail_alert_id").assertTextContains("sin confirmar",substring=true)
        compose.onNodeWithTag("rail_unchanged").assertExists()
    }
}
