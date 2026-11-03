package com.cercafunctions.app
import androidx.appfunctions.AppFunctionManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.filters.SdkSuppress
import com.cercafunctions.appfunctions.JourneyAppFunctionService
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@SdkSuppress(minSdkVersion=36)
class SharingFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun has(tag:String)=compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    @Test fun explicitOptInRecreationAndRevocationUpdateSystem() {
        compose.waitUntil(10_000){has("sharing_change")}
        val manager=requireNotNull(AppFunctionManager.getInstance(compose.activity))
        val id=JourneyAppFunctionService.FUNCTION_ID_QUERY_JOURNEY
        compose.onNodeWithTag("sharing_change").performScrollTo().assertTextContains("Autorizar",substring=true)
        assertFalse(runBlocking{manager.isAppFunctionEnabled(id)})
        compose.onNodeWithTag("sharing_change").performClick()
        compose.waitUntil(10_000){compose.onAllNodesWithTag("sharing_status").fetchSemanticsNodes().any{hasText("Autorizado",substring=true).matches(it)}}
        assertTrue(runBlocking{manager.isAppFunctionEnabled(id)})
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("sharing_change").performScrollTo().assertTextContains("Revocar",substring=true).performClick()
        compose.waitUntil(10_000){compose.onAllNodesWithTag("sharing_status").fetchSemanticsNodes().any{hasText("Sin autorización",substring=true).matches(it)}}
        assertFalse(runBlocking{manager.isAppFunctionEnabled(id)})
    }
}
