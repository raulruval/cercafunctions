package com.cercafunctions.presentation.demo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cercafunctions.domain.calendar.*
import com.cercafunctions.domain.demo.*
import com.cercafunctions.domain.assessment.*
import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.journey.HabitualJourney
import com.cercafunctions.presentation.theme.CopilotTheme
import java.time.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class DemoScreenTest {
    @get:Rule val compose=createComposeRule()
    @Test fun permissionIsRequestedByExplicitButtonAndAdvancedRemainsAccessible() {
        var requested=false;var advanced=false
        compose.setContent{CopilotTheme{DemoScreen(DemoUiState(busy=false,result=DemoResult.PermissionRequired),{}, {}, {},{requested=true},{},{advanced=true})}}
        compose.onNodeWithTag("demo_permission").performScrollTo().performClick()
        assertTrue(requested)
        compose.onNodeWithTag("demo_advanced").performScrollTo().performClick()
        assertTrue(advanced)
        compose.onNodeWithText("Próximamente").assertDoesNotExist()
    }
    @Test fun scenarioAndSharingRequireSeparateUserActions() {
        var selected:DemoScenario?=null;var shared=false
        compose.setContent{CopilotTheme{DemoScreen(DemoUiState(busy=false),{selected=it},{shared=it},{},{},{},{})}}
        compose.onNodeWithTag("demo_ON_TIME").performClick()
        assertEquals(DemoScenario.ON_TIME,selected);assertFalse(shared)
        compose.onNodeWithTag("demo_sharing").performScrollTo().performClick()
        assertTrue(shared)
    }
    @Test fun realMeetingAndSyntheticLateResultAreVisibleWithoutTechnicalFields() {
        val begin=Instant.parse("2026-11-05T09:00:00Z")
        val meeting=CalendarOccurrence(OccurrenceKey(42,begin),begin.plusSeconds(3600),"Mi reunión real","Personal")
        val evidence=ArrivalEstimate(HabitualJourney("Atocha (demo)","Chamartín (demo)",35),begin.minusSeconds(35*60),
            begin.plusSeconds(15*60),ZoneOffset.UTC,ArrivalSource.FIXTURE,ArrivalScenario.DEMO_DELAYED,begin.minusSeconds(86400),15)
        val result=DemoResult.Ready(DemoEvaluation(evidence,meeting,compareArrivalTiming(evidence,MeetingTiming(begin,meeting.end,ZoneOffset.UTC),begin)))
        compose.setContent{CopilotTheme{DemoScreen(DemoUiState(busy=false,result=result),{},{},{},{},{},{})}}
        compose.onNodeWithText("Mi reunión real").assertExists()
        compose.onNodeWithTag("demo_result").performScrollTo().assertTextEquals("Llegarías 15 min tarde")
        compose.onNodeWithText("Referencia temporal").assertDoesNotExist()
    }
}
