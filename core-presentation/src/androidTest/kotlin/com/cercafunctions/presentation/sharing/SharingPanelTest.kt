package com.cercafunctions.presentation.sharing
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cercafunctions.presentation.theme.CopilotTheme
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
class SharingPanelTest {
    @get:Rule val compose=createComposeRule()
    @Test fun optInIsExplicitAndScopedToJourney() {
        var calls=0
        compose.setContent{CopilotTheme{SharingPanel(SharingUiState(true,loading=false),{calls++})}}
        compose.onNodeWithTag("sharing_privacy").assertTextContains("calendario",substring=true)
        compose.onNodeWithTag("sharing_change").assertTextContains("Autorizar",substring=true).performClick()
        assertEquals(1,calls)
    }
    @Test fun currentConsentOffersRevocation() {
        compose.setContent{CopilotTheme{SharingPanel(SharingUiState(true,allowed=true,loading=false))}}
        compose.onNodeWithTag("sharing_status").assertTextContains("Autorizado",substring=true)
        compose.onNodeWithTag("sharing_change").assertTextContains("Revocar",substring=true).assertIsEnabled()
    }
    @Test fun unavailableCannotEnableAndBusyBlocksActions() {
        compose.setContent{CopilotTheme{SharingPanel(SharingUiState(false,loading=false))}}
        compose.onNodeWithTag("sharing_change").assertIsNotEnabled()
        compose.onNodeWithTag("sharing_status").assertTextContains("no disponible",substring=true)
    }
    @Test fun errorHasExplicitRetryAndNoPrivateDetails() {
        compose.setContent{CopilotTheme{SharingPanel(SharingUiState(true,loading=false,busy=true,error=true))}}
        compose.onNodeWithTag("sharing_error").assertExists()
        compose.onNodeWithTag("sharing_retry").assertExists()
        compose.onNodeWithTag("sharing_change").assertIsNotEnabled()
    }
}
