package com.cercafunctions.presentation.authorization
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cercafunctions.presentation.theme.CopilotTheme
import com.cercafunctions.domain.authorization.*
import com.cercafunctions.domain.calendar.*
import java.time.*
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
class AssessmentSharingPanelTest {
    @get:Rule val compose=createComposeRule()
    private val at=Instant.parse("2026-11-04T09:00:00Z")
    private val target=GrantTarget(CalendarSelection(OccurrenceKey(1,at),at.plusSeconds(30),CalendarDay(LocalDate.parse("2026-11-04"),ZoneOffset.UTC)),CalendarOccurrence(OccurrenceKey(1,at),at.plusSeconds(30),"POC","POC"))
    @Test fun scopeAndNoSelectionAreExplicit() {
        compose.setContent{CopilotTheme{AssessmentSharingPanel(AuthorizationUiState(true,busy=false))}}
        compose.onNodeWithTag("authorization_privacy").assertTextContains("30 minutos",substring=true)
        compose.onNodeWithTag("authorization_issue").assertIsNotEnabled()
    }
    @Test fun issueAndCopyAreSeparateExplicitActions() {
        var copies=0
        compose.setContent{CopilotTheme{AssessmentSharingPanel(AuthorizationUiState(true,target,GrantSummary(at,at),"synthetic-reference",false),onCopy={copies++})}}
        compose.onNodeWithTag("authorization_reference").assertTextContains("synthetic-reference",substring=true)
        assertEquals(0,copies);compose.onNodeWithTag("authorization_copy").performClick();assertEquals(1,copies)
    }
    @Test fun activeGrantWithoutRawTokenCanBeRevoked() {
        compose.setContent{CopilotTheme{AssessmentSharingPanel(AuthorizationUiState(true,active=GrantSummary(at,at),busy=false))}}
        compose.onNodeWithTag("authorization_revoke").assertIsEnabled()
        compose.onNodeWithTag("authorization_reference").assertDoesNotExist()
    }
    @Test fun failureAndUnsupportedDoNotOfferAutomaticPermissionRequests() {
        compose.setContent{CopilotTheme{AssessmentSharingPanel(AuthorizationUiState(false,target=target,busy=false,error=true))}}
        compose.onNodeWithTag("authorization_issue").assertIsNotEnabled()
        compose.onNodeWithTag("authorization_error").assertExists()
        compose.onNodeWithTag("authorization_refresh").assertExists()
    }
}
