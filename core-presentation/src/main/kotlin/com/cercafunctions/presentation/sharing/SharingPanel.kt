package com.cercafunctions.presentation.sharing
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cercafunctions.presentation.R

@Composable fun SharingRoute(viewModel:SharingViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SharingPanel(state,viewModel::change,viewModel::refresh)
}
@Composable internal fun SharingPanel(state:SharingUiState,onChange:()->Unit={},onRetry:()->Unit={}) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.sharing_title),style=MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.sharing_privacy),modifier=Modifier.testTag("sharing_privacy"))
            Text(stringResource(when {
                state.loading -> R.string.sharing_loading
                state.allowed -> R.string.sharing_allowed
                !state.available -> R.string.sharing_unavailable
                else -> R.string.sharing_denied
            }),modifier=Modifier.testTag("sharing_status"))
            Button(onChange,enabled=!state.loading && !state.busy && (state.available || state.allowed),
                modifier=Modifier.testTag("sharing_change")) {
                Text(stringResource(if(state.busy)R.string.sharing_saving else if(state.allowed)R.string.sharing_revoke else R.string.sharing_authorize))
            }
            if(state.error) {
                Text(stringResource(R.string.sharing_error),color=MaterialTheme.colorScheme.error,modifier=Modifier.testTag("sharing_error"))
                TextButton(onRetry,enabled=!state.busy,modifier=Modifier.testTag("sharing_retry")){Text(stringResource(R.string.sharing_retry))}
            }
        }
    }
}
