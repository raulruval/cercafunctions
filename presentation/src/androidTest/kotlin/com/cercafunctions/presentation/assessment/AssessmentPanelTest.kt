package com.cercafunctions.presentation.assessment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cercafunctions.domain.assessment.*
import com.cercafunctions.domain.calendar.*
import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.journey.HabitualJourney
import com.cercafunctions.presentation.theme.CopilotTheme
import java.time.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AssessmentPanelTest {
    @get:Rule val compose=createComposeRule()
    private val zone=ZoneId.of("Europe/Madrid")
    private val start=Instant.parse("2026-11-02T08:00:00Z")
    private val selection=CalendarSelection(OccurrenceKey(1,start),start.plusSeconds(3600),CalendarDay(LocalDate.of(2026,11,2),zone))
    private fun arrival(at:Instant=start.minusSeconds(600),source:ArrivalSource=ArrivalSource.REFERENCE)=
        ArrivalEstimate(HabitualJourney("Atocha","Chamartín",35),at.minusSeconds(2100),at,zone,source,
            if(source==ArrivalSource.REFERENCE)ArrivalScenario.REFERENCE else ArrivalScenario.DEMO_DELAYED,Instant.parse("2026-11-01T08:00:00Z"),if(source==ArrivalSource.FIXTURE)15 else null)
    private fun result(value:ArrivalEstimate=arrival(),minutes:Long=10,status:ComparisonStatus=ComparisonStatus.ON_TIME)=
        AssessmentResult.Compared(ComparedArrival(value,MeetingTiming(start,start.plusSeconds(3600),zone),status,minutes,start.minusSeconds(3600)))
    private fun show(value:ArrivalEstimate?=arrival(),result:AssessmentResult?=null,busy:Boolean=false,action:()->Unit={}) {
        compose.setContent{CopilotTheme{Column(Modifier.verticalScroll(rememberScrollState())){AssessmentPanel(value,selection,result,busy,action)}}}
    }
    @Test fun evaluationNeedsExplicitActionAndDisplaysSimplification() {
        var count=0;show(action={count++})
        assertEquals(0,count)
        compose.onNodeWithTag("assessment_simplification").assertTextContains("0 min",substring=true)
        compose.onNodeWithTag("assessment_evaluate").performScrollTo().performClick();assertEquals(1,count)
    }
    @Test fun referenceMarginHasFullDatesZonesAndNoGuarantee() {
        show(result=result())
        compose.onNodeWithTag("assessment_outcome").assertTextContains("10 min",substring=true)
        compose.onNodeWithTag("assessment_arrival").assertTextContains("2026-11-02T08:50:00+01:00",substring=true)
        compose.onNodeWithTag("assessment_meeting").assertTextContains("2026-11-02T09:00:00+01:00",substring=true)
        compose.onNodeWithTag("assessment_source").assertTextContains("Referencia",substring=true)
        compose.onNodeWithTag("assessment_uncertainty").assertExists()
    }
    @Test fun lateMinuteRoundingDoesNotHideNanosecondPrecision() {
        val value=arrival(start.plusNanos(1))
        show(value,result(value,minutes=-1,status=ComparisonStatus.LATE))
        compose.onNodeWithTag("assessment_outcome").assertTextContains("Retraso previsto: 1 min",substring=true)
        compose.onNodeWithTag("assessment_arrival").assertTextContains("09:00:00.000000001",substring=true)
    }
    @Test fun equalityShowsZeroWithoutCallingItLate() {
        show(arrival(start),result(arrival(start),minutes=0))
        compose.onNodeWithTag("assessment_outcome").assertTextContains("Margen previsto: 0 min",substring=true)
    }
    @Test fun fixturePreservesSourceAndFixedObservationTime() {
        val value=arrival(source=ArrivalSource.FIXTURE)
        show(value,result(value))
        compose.onNodeWithTag("assessment_source").assertTextContains("Demostración",substring=true)
        compose.onNodeWithTag("assessment_observed").assertTextContains("2026-11-01T08:00:00Z",substring=true)
    }
    @Test fun indeterminateNeverDisplaysAComparisonOrObsoleteMeeting() {
        show(result=AssessmentResult.Indeterminate(AssessmentReason.CALENDAR_CHANGED,arrival(),start))
        compose.onNodeWithTag("assessment_indeterminate").assertTextContains("cambió",substring=true)
        compose.onNodeWithTag("assessment_meeting").assertDoesNotExist()
        compose.onNodeWithTag("assessment_outcome").assertDoesNotExist()
    }
    @Test fun pendingEvaluationIsDisabled() {
        show(busy=true)
        compose.onNodeWithTag("assessment_evaluate").assertIsNotEnabled()
    }
}
