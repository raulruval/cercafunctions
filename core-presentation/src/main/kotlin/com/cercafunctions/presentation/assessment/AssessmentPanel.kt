package com.cercafunctions.presentation.assessment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cercafunctions.domain.assessment.*
import com.cercafunctions.domain.calendar.CalendarSelection
import com.cercafunctions.domain.estimate.*
import com.cercafunctions.presentation.calendar.CalendarViewModel
import com.cercafunctions.presentation.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable fun AssessmentRoute(viewModel:CalendarViewModel,arrival:ArrivalEstimate?) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Hide old output immediately, even before the host's input synchronization effect runs.
    val result=state.assessment.takeIf{state.arrival==arrival}
    AssessmentPanel(arrival,state.selection,result,state.busy || (state.assessmentBusy && state.arrival==arrival)) {
        viewModel.setArrival(arrival);viewModel.evaluate()
    }
}

@Composable
internal fun AssessmentPanel(arrival:ArrivalEstimate?,selection:CalendarSelection?,result:AssessmentResult?,busy:Boolean=false,onEvaluate:()->Unit={}) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.assessment_heading),style=MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.assessment_simplification),style=MaterialTheme.typography.bodySmall,
                modifier=Modifier.testTag("assessment_simplification"))
            if(arrival==null || selection==null)Text(stringResource(if(arrival==null)R.string.assessment_missing_arrival else R.string.assessment_missing_selection))
            Button(onEvaluate,enabled=!busy,modifier=Modifier.testTag("assessment_evaluate")) {
                Text(stringResource(if(busy)R.string.assessment_loading else R.string.assessment_evaluate))
            }
            when(result) {
                null -> Unit
                is AssessmentResult.Compared -> {
                    val value=result.value
                    Text(stringResource(if(value.status==ComparisonStatus.LATE)R.string.assessment_late else R.string.assessment_margin,
                        if(value.signedMarginMinutes<0)-value.signedMarginMinutes else value.signedMarginMinutes),
                        style=MaterialTheme.typography.titleLarge,modifier=Modifier.testTag("assessment_outcome"))
                    Evidence(value.evidence)
                    Text(stringResource(R.string.assessment_arrival,format(requireNotNull(value.evidence.arrival),value.evidence.zone),value.evidence.zone.id),modifier=Modifier.testTag("assessment_arrival"))
                    Text(stringResource(R.string.assessment_meeting,format(value.meeting.start,value.meeting.zone),value.meeting.zone.id),modifier=Modifier.testTag("assessment_meeting"))
                    Text(stringResource(R.string.assessment_checked,value.assessedAt.toString()))
                }
                is AssessmentResult.Indeterminate -> {
                    Text(stringResource(R.string.assessment_indeterminate,stringResource(result.reason.label())),modifier=Modifier.testTag("assessment_indeterminate"))
                    result.evidence?.let{Evidence(it)}
                    Text(stringResource(R.string.assessment_checked,result.assessedAt.toString()))
                }
            }
        }
    }
}

@Composable private fun Evidence(value:ArrivalEstimate) {
    Text(stringResource(if(value.source==ArrivalSource.FIXTURE)R.string.assessment_fixture else R.string.assessment_reference),
        style=MaterialTheme.typography.titleMedium,modifier=Modifier.testTag("assessment_source"))
    Text(stringResource(when(value.scenario){
        ArrivalScenario.REFERENCE->R.string.arrival_mode_reference
        ArrivalScenario.DEMO_NORMAL->R.string.arrival_mode_normal
        ArrivalScenario.DEMO_DELAYED->R.string.arrival_mode_delayed
        ArrivalScenario.DEMO_UNKNOWN->R.string.arrival_mode_unknown
    }))
    Text(stringResource(R.string.assessment_departure,format(value.departure,value.zone),value.zone.id))
    Text(stringResource(R.string.assessment_observed,value.observedAt.toString()),modifier=Modifier.testTag("assessment_observed"))
    Text(stringResource(R.string.assessment_uncertainty),style=MaterialTheme.typography.bodySmall,modifier=Modifier.testTag("assessment_uncertainty"))
}
private fun format(instant:Instant,zone:ZoneId)=DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(instant.atZone(zone))
private fun AssessmentReason.label():Int=when(this) {
    AssessmentReason.MISSING_ARRIVAL->R.string.assessment_missing_arrival
    AssessmentReason.MISSING_SELECTION->R.string.assessment_missing_selection
    AssessmentReason.UNKNOWN_ARRIVAL->R.string.assessment_unknown_arrival
    AssessmentReason.ARRIVAL_CHANGED->R.string.assessment_arrival_changed
    AssessmentReason.ARRIVAL_UNAVAILABLE->R.string.assessment_arrival_unavailable
    AssessmentReason.DEPARTURE_PASSED->R.string.assessment_departure_passed
    AssessmentReason.CALENDAR_CHANGED->R.string.assessment_calendar_changed
    AssessmentReason.CALENDAR_PERMISSION->R.string.assessment_calendar_permission
    AssessmentReason.CALENDAR_UNAVAILABLE->R.string.assessment_calendar_unavailable
}
