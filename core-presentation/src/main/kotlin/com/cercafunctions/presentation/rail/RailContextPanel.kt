package com.cercafunctions.presentation.rail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cercafunctions.domain.rail.*
import com.cercafunctions.domain.estimate.ArrivalSource
import com.cercafunctions.presentation.R

internal data class RailActions(val onOrigin:(String)->Unit={},val onDestination:(String)->Unit={},
    val onSelectOrigin:(String)->Unit={},val onSelectDestination:(String)->Unit={},val onConsult:()->Unit={})

@Composable fun RailContextRoute(viewModel:RailContextViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RailContextPanel(state,RailActions(viewModel::updateOrigin,viewModel::updateDestination,
        viewModel::selectOrigin,viewModel::selectDestination,viewModel::consult),viewModel::suggestions)
}

@Composable
internal fun RailContextPanel(state:RailUiState,actions:RailActions=RailActions(),suggestions:(String)->List<RailStation> ={emptyList()}) {
    val arrival=state.arrival ?: return
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        HorizontalDivider()
        Text(stringResource(R.string.rail_heading),style=MaterialTheme.typography.titleLarge)
        if(arrival.source==ArrivalSource.FIXTURE) {
            Text(stringResource(R.string.rail_demo),modifier=Modifier.testTag("rail_demo"))
        } else {
            Text(stringResource(R.string.rail_associate,arrival.journey.origin,arrival.journey.destination))
            Text(stringResource(R.string.rail_coverage),modifier=Modifier.testTag("rail_coverage"))
            Text(stringResource(R.string.rail_catalog),style=MaterialTheme.typography.bodySmall)
            StationInput(state.originText,state.origin,actions.onOrigin,actions.onSelectOrigin,
                suggestions,R.string.rail_origin,"origin")
            StationInput(state.destinationText,state.destination,actions.onDestination,actions.onSelectDestination,
                suggestions,R.string.rail_destination,"destination")
            Button(actions.onConsult,enabled=!state.busy,modifier=Modifier.testTag("rail_consult")) {
                Text(stringResource(if(state.busy)R.string.rail_loading else R.string.rail_consult))
            }
            if(state.failed) Text(stringResource(R.string.rail_unavailable),color=MaterialTheme.colorScheme.error)
            state.result?.let { result ->
                Text(stringResource(result.status.label()),style=MaterialTheme.typography.titleMedium,
                    modifier=Modifier.testTag("rail_status"))
                Text(stringResource(R.string.rail_source,result.sourceUrl),modifier=Modifier.testTag("rail_source"))
                Text(stringResource(R.string.rail_times,result.consultedAt.toString(),result.publishedAt?.toString()
                    ?: stringResource(R.string.rail_unknown_time)),modifier=Modifier.testTag("rail_times"))
                val failure=result.failure
                if(failure!=null) Text(stringResource(when(failure) {
                    RailFailure.NETWORK -> R.string.rail_failure_network
                    RailFailure.HTTP -> R.string.rail_failure_http
                    RailFailure.INVALID_FORMAT -> R.string.rail_failure_format
                    RailFailure.TOO_LARGE -> R.string.rail_failure_size
                }))
                if(result.status==RailStatus.AVAILABLE) {
                    if(result.alerts.isEmpty()) Text(stringResource(R.string.rail_empty),modifier=Modifier.testTag("rail_empty"))
                    if(result.unresolvedCount>0)Text(stringResource(R.string.rail_unresolved_count,result.unresolvedCount))
                    val unknown=stringResource(R.string.rail_unknown_time)
                    for(item in result.alerts) {
                        Text(stringResource(if(item.impactConfirmed)R.string.rail_impact_confirmed else R.string.rail_impact_unknown),
                            style=MaterialTheme.typography.labelLarge,modifier=Modifier.testTag("rail_alert_${item.alert.id}"))
                        Text(item.alert.text)
                        fun ranges(periods:List<AlertPeriod>) = if(periods.isEmpty())unknown else
                            periods.joinToString("; ") { "[${it.start ?: unknown}, ${it.end ?: unknown})" }
                        Text(stringResource(R.string.rail_periods,ranges(item.alert.publication),ranges(item.alert.impact)),style=MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Text(stringResource(R.string.rail_unchanged),style=MaterialTheme.typography.bodySmall,modifier=Modifier.testTag("rail_unchanged"))
        }
    }
}

@Composable
private fun StationInput(value:String,selected:RailStation?,onChange:(String)->Unit,onSelect:(String)->Unit,
    suggestions:(String)->List<RailStation>,label:Int,tag:String) {
    val keyboard=LocalSoftwareKeyboardController.current
    OutlinedTextField(value,onChange,label={Text(stringResource(label))},singleLine=true,
        modifier=Modifier.fillMaxWidth().testTag("rail_$tag"))
    if(selected==null) for(station in suggestions(value)) TextButton(
        onClick={onSelect(station.id);keyboard?.hide()},modifier=Modifier.testTag("rail_${tag}_${station.id}")) {
        Text("${station.name} · ${station.id}")
    }
}
private fun RailStatus.label():Int=when(this) {
    RailStatus.AVAILABLE -> R.string.rail_available
    RailStatus.UNRESOLVED -> R.string.rail_unresolved
    RailStatus.UNAVAILABLE -> R.string.rail_unavailable
    RailStatus.STALE -> R.string.rail_stale
    RailStatus.FRESHNESS_UNKNOWN -> R.string.rail_freshness_unknown
    RailStatus.FUTURE_TIMESTAMP -> R.string.rail_future
    RailStatus.DEMO_SKIPPED -> R.string.rail_demo
}
