package com.cercafunctions.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.cercafunctions.presentation.calendar.CalendarRoute
import com.cercafunctions.presentation.calendar.CalendarViewModel
import com.cercafunctions.presentation.rail.RailContextViewModel
import com.cercafunctions.presentation.estimate.ArrivalRoute
import com.cercafunctions.presentation.estimate.ArrivalViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cercafunctions.domain.journey.HabitualJourney
import com.cercafunctions.domain.journey.JourneyField
import com.cercafunctions.domain.journey.JourneyStorageFailure
import com.cercafunctions.presentation.R
import com.cercafunctions.presentation.theme.CopilotTheme

internal data class HomeActions(
    val onRetry: () -> Unit = {},
    val onEdit: () -> Unit = {},
    val onCancel: () -> Unit = {},
    val onOriginChange: (String) -> Unit = {},
    val onDestinationChange: (String) -> Unit = {},
    val onDurationChange: (String) -> Unit = {},
    val onSave: () -> Unit = {},
)

@Composable
fun HomeRoute(viewModel: HomeViewModel, arrivalViewModel: ArrivalViewModel, railViewModel: RailContextViewModel, calendarViewModel: CalendarViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.content) { arrivalViewModel.onJourneyChanged((state.content as? JourneyContent.Ready)?.journey) }
    BackHandler(enabled = state.editor != null, onBack = viewModel::cancelEditing)
    HomeScreen(state, HomeActions(viewModel::refresh, viewModel::beginEditing, viewModel::cancelEditing,
        viewModel::updateOrigin, viewModel::updateDestination, viewModel::updateDuration, viewModel::save), calendarContent={CalendarRoute(calendarViewModel)}) { ArrivalRoute(arrivalViewModel, railViewModel) }
}

@Composable
internal fun HomeScreen(state: HomeUiState, actions: HomeActions = HomeActions(), calendarContent: @Composable () -> Unit = {}, arrivalContent: @Composable () -> Unit = {}) {
    Scaffold { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).imePadding()
                .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(stringResource(R.string.home_brand), style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            Text(stringResource(if (state.editor == null) R.string.home_title else R.string.journey_editor_title),
                style = MaterialTheme.typography.headlineLarge)
            if (state.editor == null) Text(stringResource(R.string.home_subtitle), style = MaterialTheme.typography.bodyLarge)
            if(state.editor==null)calendarContent()
            Card(modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.journey_heading), style = MaterialTheme.typography.titleMedium)
                    if (state.editor != null) JourneyForm(state.editor, actions)
                    else JourneySummary(state, actions)
                }
            }
            if (state.editor == null) {
                if (state.content is JourneyContent.Ready) arrivalContent()
                Text(stringResource(R.string.home_upcoming_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.home_upcoming_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.home_privacy), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun JourneySummary(state: HomeUiState, actions: HomeActions) {
    when (val content = state.content) {
        JourneyContent.Loading -> {
            CircularProgressIndicator()
            Text(stringResource(R.string.journey_loading))
        }
        JourneyContent.NotConfigured -> {
            Text(stringResource(R.string.journey_not_configured), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.journey_empty_description))
            Button(onClick = actions.onEdit, modifier = Modifier.testTag("configure")) {
                Text(stringResource(R.string.journey_configure))
            }
        }
        is JourneyContent.Ready -> {
            if (state.justSaved) Text(stringResource(R.string.journey_saved), modifier = Modifier.testTag("saved_confirmation"))
            Text("${content.journey.origin} → ${content.journey.destination}",
                style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag("journey_summary"))
            Text(stringResource(R.string.journey_duration, content.journey.estimatedDurationMinutes))
            Text(stringResource(R.string.journey_reference_note), style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = actions.onEdit, modifier = Modifier.testTag("edit")) {
                Text(stringResource(R.string.journey_edit))
            }
        }
        is JourneyContent.Unavailable -> {
            Text(stringResource(if (content.reason == JourneyStorageFailure.INVALID_STORED_DATA)
                R.string.journey_invalid_stored else R.string.journey_error), modifier = Modifier.testTag("read_error"))
            Button(onClick = actions.onRetry, modifier = Modifier.testTag("retry")) { Text(stringResource(R.string.journey_retry)) }
        }
    }
}

@Composable
private fun JourneyForm(draft: JourneyDraft, actions: HomeActions) {
    JourneyFieldInput(draft.origin, actions.onOriginChange, R.string.journey_origin,
        R.string.journey_origin_error, JourneyField.ORIGIN in draft.errors, "origin", !draft.isSaving)
    JourneyFieldInput(draft.destination, actions.onDestinationChange, R.string.journey_destination,
        R.string.journey_destination_error, JourneyField.DESTINATION in draft.errors, "destination", !draft.isSaving)
    JourneyFieldInput(draft.durationMinutes, actions.onDurationChange, R.string.journey_minutes,
        R.string.journey_duration_error, JourneyField.DURATION in draft.errors, "duration", !draft.isSaving,
        KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done), KeyboardActions(onDone = { actions.onSave() }))
    Text(stringResource(R.string.journey_reference_note), style = MaterialTheme.typography.bodySmall)
    if (draft.saveError) Text(stringResource(R.string.journey_save_error),
        color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("save_error"))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = actions.onCancel, enabled = !draft.isSaving, modifier = Modifier.testTag("cancel")) {
            Text(stringResource(R.string.journey_cancel))
        }
        Button(onClick = actions.onSave, enabled = !draft.isSaving, modifier = Modifier.testTag("save")) {
            Text(stringResource(if (draft.isSaving) R.string.journey_saving else R.string.journey_save))
        }
    }
}

@Composable
private fun JourneyFieldInput(
    value: String, onChange: (String) -> Unit, label: Int, error: Int, isError: Boolean,
    tag: String, enabled: Boolean,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(stringResource(label)) },
        isError = isError, enabled = enabled, singleLine = true,
        keyboardOptions = keyboardOptions, keyboardActions = keyboardActions,
        supportingText = if (isError) ({ Text(stringResource(error), modifier = Modifier.testTag("${tag}_error")) }) else null,
        modifier = Modifier.fillMaxWidth().testTag(tag))
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    CopilotTheme { HomeScreen(HomeUiState(JourneyContent.NotConfigured)) }
}

@Preview(showBackground = true)
@Composable
private fun JourneyFormPreview() {
    CopilotTheme { HomeScreen(HomeUiState(JourneyContent.NotConfigured, JourneyDraft("Origen", "Destino", "35"))) }
}
