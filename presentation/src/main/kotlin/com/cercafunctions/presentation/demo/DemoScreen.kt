package com.cercafunctions.presentation.demo

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cercafunctions.domain.demo.*
import com.cercafunctions.domain.assessment.ComparisonStatus
import com.cercafunctions.presentation.R
import java.time.format.DateTimeFormatter

@Composable
fun DemoRoute(viewModel:DemoViewModel,onAdvanced:()->Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context=LocalContext.current
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){viewModel.refresh()}
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME){viewModel.refresh()}
    DemoScreen(state,viewModel::select,viewModel::setAgentAllowed,viewModel::refresh,
        {permission.launch(Manifest.permission.READ_CALENDAR)},
        {context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:${context.packageName}")))},onAdvanced)
}

@Composable
internal fun DemoScreen(state:DemoUiState,onScenario:(DemoScenario)->Unit,onSharing:(Boolean)->Unit,
    onRefresh:()->Unit,onPermission:()->Unit,onSettings:()->Unit,onAdvanced:()->Unit) {
    Scaffold { insets ->
        Column(Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.demo_brand),style=MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.demo_intro),style=MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                DemoScenario.entries.forEach { scenario ->
                    FilterChip(selected=state.settings.scenario==scenario,onClick={onScenario(scenario)},
                        enabled=!state.busy,label={Text(stringResource(when(scenario){
                            DemoScenario.ON_TIME->R.string.demo_normal;DemoScenario.DELAYED->R.string.demo_delayed;DemoScenario.UNKNOWN->R.string.demo_unknown}))},
                        modifier=Modifier.testTag("demo_${scenario.name}"))
                }
            }
            if(state.busy)LinearProgressIndicator(Modifier.fillMaxWidth())
            if(state.failed)Text(stringResource(R.string.demo_error),color=MaterialTheme.colorScheme.error)
            when(val result=state.result) {
                DemoResult.PermissionRequired -> Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.demo_permission))
                        Button(onPermission,modifier=Modifier.testTag("demo_permission")){Text(stringResource(R.string.demo_allow_calendar))}
                        TextButton(onSettings){Text(stringResource(R.string.demo_settings))}
                    }
                }
                is DemoResult.NoMeeting -> Text(stringResource(R.string.demo_no_meeting,result.date.toString(),result.zone.id))
                DemoResult.CalendarUnavailable -> Text(stringResource(R.string.demo_calendar_error))
                is DemoResult.Ready -> {
                    val value=result.value
                    val evidence=value.evidence
                    val format=DateTimeFormatter.ofPattern("dd/MM HH:mm").withZone(evidence.zone)
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                            Text(stringResource(R.string.demo_real_meeting),style=MaterialTheme.typography.titleMedium)
                            Text(value.occurrence.title.ifBlank{stringResource(R.string.demo_untitled)})
                            Text("${format.format(value.occurrence.key.begin)} · ${evidence.zone.id}")
                        }
                    }
                    Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                            Text("${evidence.journey.origin} → ${evidence.journey.destination}",style=MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.demo_departure,format.format(evidence.departure)))
                            Text(stringResource(R.string.demo_arrival,evidence.arrival?.let(format::format) ?: stringResource(R.string.demo_unknown)))
                            val comparison=value.comparison
                            Text(stringResource(when(comparison?.status){ComparisonStatus.LATE->R.string.demo_late;ComparisonStatus.ON_TIME->R.string.demo_on_time;null->R.string.demo_indeterminate}),
                                style=MaterialTheme.typography.titleLarge,modifier=Modifier.testTag("demo_result"))
                        }
                    }
                }
                null -> Unit
            }
            Text(stringResource(R.string.demo_fixture_explanation),style=MaterialTheme.typography.bodySmall)
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.demo_sharing_title),modifier=Modifier.weight(1f),style=MaterialTheme.typography.titleMedium)
                        Switch(checked=state.settings.agentAllowed,onCheckedChange=onSharing,enabled=!state.busy && !state.failed,
                            modifier=Modifier.testTag("demo_sharing"))
                    }
                    Text(stringResource(R.string.demo_sharing_scope),style=MaterialTheme.typography.bodySmall)
                }
            }
            OutlinedButton(onRefresh,enabled=!state.busy,modifier=Modifier.fillMaxWidth().testTag("demo_refresh")){Text(stringResource(R.string.demo_refresh))}
            Text(stringResource(R.string.demo_agent_help),style=MaterialTheme.typography.bodySmall)
            TextButton(onAdvanced,modifier=Modifier.testTag("demo_advanced")){Text(stringResource(R.string.demo_advanced))}
        }
    }
}
