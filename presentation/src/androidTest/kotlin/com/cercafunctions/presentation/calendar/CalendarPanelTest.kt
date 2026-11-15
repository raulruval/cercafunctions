package com.cercafunctions.presentation.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cercafunctions.domain.calendar.*
import com.cercafunctions.presentation.theme.CopilotTheme
import java.time.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class CalendarPanelTest {
    @get:Rule val compose=createComposeRule()
    private val zone=ZoneId.of("Europe/Madrid")
    private val day=CalendarDay(LocalDate.parse("2026-11-03"),zone)
    private val event=CalendarOccurrence(OccurrenceKey(1,Instant.parse("2026-11-03T08:00:00Z")),Instant.parse("2026-11-03T09:00:00Z"),"POC reunión","POC local")
    private val initial=CalendarUiState("2026-11-03",zone.id)
    private fun show(state:CalendarUiState,actions:CalendarActions=CalendarActions()) {
        compose.setContent{CopilotTheme{Column(Modifier.verticalScroll(rememberScrollState())){CalendarPanel(state,actions)}}}
    }
    @Test fun accessRequestOnlyOccursAfterUserAction() {
        var queries=0;var settings=0
        show(initial.copy(status=CalendarStatus.ACCESS_REQUIRED),CalendarActions(onConsult={queries++},onSettings={settings++}))
        assertEquals(0,queries)
        compose.onNodeWithTag("calendar_consult").performScrollTo().performClick();assertEquals(1,queries)
        compose.onNodeWithTag("calendar_settings").performScrollTo().performClick();assertEquals(1,settings)
        compose.onNodeWithTag("calendar_privacy").assertExists()
    }
    @Test fun singleOccurrenceStillRequiresExplicitSelection() {
        var selection:OccurrenceKey?=null
        show(initial.copy(status=CalendarStatus.DATA,items=listOf(event)),CalendarActions(onSelect={selection=it}))
        compose.onNodeWithTag("calendar_selected").assertDoesNotExist();assertNull(selection)
        compose.onNodeWithTag("calendar_choose_1_${event.key.begin.toEpochMilli()}").performScrollTo().performClick()
        assertEquals(event.key,selection)
    }
    @Test fun selectedOccurrenceShowsDateOffsetZoneAndRevalidationAction() {
        var validations=0
        show(initial.copy(status=CalendarStatus.DATA,items=listOf(event),selection=CalendarSelection(event.key,event.end,day),selected=event),CalendarActions(onRevalidate={validations++}))
        compose.onNodeWithTag("calendar_selected").assertExists()
        compose.onNodeWithTag("calendar_selected_time").assertTextContains("2026-11-03 09:00 +01:00",substring=true)
        compose.onNodeWithTag("calendar_selected_time").assertTextContains("Europe/Madrid",substring=true)
        compose.onNodeWithTag("calendar_revalidate").performScrollTo().performClick();assertEquals(1,validations)
    }
    @Test fun emptyCalendarAndProviderFailureAreDifferent() {
        show(initial.copy(status=CalendarStatus.DATA))
        compose.onNodeWithTag("calendar_empty").assertExists()
        compose.onNodeWithTag("calendar_selected").assertDoesNotExist()
    }
    @Test fun denialOffersSettingsWithoutInventingMeetings() {
        show(initial.copy(status=CalendarStatus.DENIED))
        compose.onNodeWithTag("calendar_status").assertTextContains("No has concedido",substring=true)
        compose.onNodeWithTag("calendar_settings").assertExists()
        compose.onNodeWithTag("calendar_empty").assertDoesNotExist()
    }
    @Test fun changedSelectionAndUnavailableNeverShowASuccess() {
        show(initial.copy(status=CalendarStatus.CHANGED))
        compose.onNodeWithTag("calendar_status").assertTextContains("cambió",substring=true)
        compose.onNodeWithTag("calendar_selected").assertDoesNotExist()
    }
    @Test fun providerFailureHasItsOwnMessage() {
        show(initial.copy(status=CalendarStatus.UNAVAILABLE,failure=CalendarFailure.PROVIDER_UNAVAILABLE))
        compose.onNodeWithTag("calendar_status").assertTextContains("proveedor",substring=true)
        compose.onNodeWithTag("calendar_empty").assertDoesNotExist()
    }
}
