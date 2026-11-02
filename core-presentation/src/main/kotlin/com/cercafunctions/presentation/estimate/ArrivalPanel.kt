package com.cercafunctions.presentation.estimate

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cercafunctions.domain.estimate.*
import com.cercafunctions.presentation.R
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

internal data class ArrivalActions(
    val onDate: (String) -> Unit = {}, val onTime: (String) -> Unit = {},
    val onZone: (String) -> Unit = {}, val onTomorrow: () -> Unit = {},
    val onScenario: (ArrivalScenario) -> Unit = {}, val onOffset: (ZoneOffset) -> Unit = {},
    val onConsult: () -> Unit = {},
)

@Composable
fun ArrivalRoute(viewModel: ArrivalViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ArrivalPanel(state, ArrivalActions(viewModel::updateDate, viewModel::updateTime,
        viewModel::updateZone, viewModel::tomorrow, viewModel::selectScenario, viewModel::chooseOffset, viewModel::consult))
}

@Composable
internal fun ArrivalPanel(state: ArrivalUiState, actions: ArrivalActions = ArrivalActions()) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.arrival_heading), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.arrival_intro), style = MaterialTheme.typography.bodyMedium)
            val modes = ArrivalScenario.entries
            for (pair in modes.chunked(2)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (mode in pair) FilterChip(selected = state.scenario == mode, onClick = { actions.onScenario(mode) },
                        label = { Text(stringResource(mode.label())) }, modifier = Modifier.testTag("arrival_mode_$mode"))
                }
            }
            state.fixture?.let { demo ->
                Text(stringResource(R.string.arrival_demo_info, demo.journey.origin, demo.journey.destination,
                    demo.journey.estimatedDurationMinutes, demo.observedAt.toString()), modifier = Modifier.testTag("arrival_demo_info"))
            }
            val editable = state.fixture == null
            OutlinedTextField(state.date, actions.onDate, label = { Text(stringResource(R.string.arrival_date)) },
                singleLine = true, enabled = editable, isError = state.error == ArrivalInputError.DATE,
                modifier = Modifier.fillMaxWidth().testTag("arrival_date"))
            OutlinedTextField(state.time, actions.onTime, label = { Text(stringResource(R.string.arrival_time)) },
                singleLine = true, enabled = editable, isError = state.error == ArrivalInputError.TIME,
                modifier = Modifier.fillMaxWidth().testTag("arrival_time"))
            OutlinedTextField(state.zone, actions.onZone, label = { Text(stringResource(R.string.arrival_zone)) },
                singleLine = true, enabled = editable, isError = state.error == ArrivalInputError.ZONE,
                modifier = Modifier.fillMaxWidth().testTag("arrival_zone"))
            TextButton(onClick = actions.onTomorrow, enabled = editable, modifier = Modifier.testTag("arrival_tomorrow")) {
                Text(stringResource(R.string.arrival_tomorrow))
            }
            if (state.offsets.isNotEmpty()) {
                Text(stringResource(R.string.arrival_choose_offset))
                for (offset in state.offsets) FilterChip(selected = state.selectedOffset == offset,
                    onClick = { actions.onOffset(offset) }, label = { Text("UTC$offset") },
                    modifier = Modifier.testTag("arrival_offset_$offset"))
            }
            state.error?.let { error ->
                Text(stringResource(when (error) {
                    ArrivalInputError.DATE -> R.string.arrival_error_date
                    ArrivalInputError.TIME -> R.string.arrival_error_time
                    ArrivalInputError.ZONE -> R.string.arrival_error_zone
                    ArrivalInputError.UNAVAILABLE -> R.string.arrival_error_storage
                }), color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("arrival_input_error"))
            }
            Button(actions.onConsult, enabled = !state.busy, modifier = Modifier.testTag("arrival_consult")) {
                Text(stringResource(if (state.busy) R.string.arrival_loading else R.string.arrival_consult))
            }
            when (val result = state.result) {
                is ArrivalResult.Estimated -> ArrivalResultContent(result.value)
                null -> Unit
                else -> Text(stringResource(when (result) {
                    ArrivalResult.Gap -> R.string.arrival_gap
                    is ArrivalResult.Ambiguous -> R.string.arrival_ambiguous
                    ArrivalResult.InvalidOffset -> R.string.arrival_invalid_offset
                    ArrivalResult.PastDeparture -> R.string.arrival_past
                    ArrivalResult.MissingJourney -> R.string.arrival_missing
                    is ArrivalResult.StorageUnavailable -> R.string.arrival_error_storage
                    else -> error("handled above")
                }), color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("arrival_problem"))
            }
        }
    }
}

@Composable
private fun ArrivalResultContent(value: ArrivalEstimate) {
    val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm xxx")
    HorizontalDivider()
    Text(stringResource(if (value.source == ArrivalSource.FIXTURE) R.string.arrival_source_fixture else R.string.arrival_source_reference),
        style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("arrival_source"))
    Text("${value.journey.origin} → ${value.journey.destination}")
    Text(stringResource(R.string.arrival_departure_value, formatter.format(value.departure.atZone(value.zone)), value.zone.id))
    val arrival = value.arrival
    Text(if (arrival == null) stringResource(R.string.arrival_unknown) else
        stringResource(R.string.arrival_value, formatter.format(arrival.atZone(value.zone)), value.zone.id),
        style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag("arrival_result"))
    Text(stringResource(if (value.delayMinutes == null) R.string.arrival_delay_unknown else R.string.arrival_delay_demo,
        value.delayMinutes ?: 0))
    Text(stringResource(R.string.arrival_observed, value.observedAt.toString()), modifier = Modifier.testTag("arrival_observed"))
    Text(stringResource(R.string.arrival_horizon), style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("arrival_horizon"))
}

private fun ArrivalScenario.label(): Int = when (this) {
    ArrivalScenario.REFERENCE -> R.string.arrival_mode_reference
    ArrivalScenario.DEMO_NORMAL -> R.string.arrival_mode_normal
    ArrivalScenario.DEMO_DELAYED -> R.string.arrival_mode_delayed
    ArrivalScenario.DEMO_UNKNOWN -> R.string.arrival_mode_unknown
}
