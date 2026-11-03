package com.cercafunctions.presentation.authorization
import android.content.ClipData
import android.os.PersistableBundle
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.cercafunctions.presentation.R

@Composable fun AssessmentSharingRoute(viewModel:AssessmentSharingViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard=LocalClipboard.current
    val scope=rememberCoroutineScope()
    AssessmentSharingPanel(state,viewModel::authorize,viewModel::revoke,
        onCopy={state.reference?.let{reference->scope.launch{
            val clip=ClipData.newPlainText("Temporary assessment reference",reference).apply {
                // Rendering hint to hide sensitive previews; the grant policy enforces access.
                description.extras=PersistableBundle().apply{putBoolean("android.content.extra.IS_SENSITIVE",true)}
            }
            clipboard.setClipEntry(ClipEntry(clip))
        }}},
        onRefresh=viewModel::refresh)
}
@Composable internal fun AssessmentSharingPanel(state:AuthorizationUiState,onAuthorize:()->Unit={},onRevoke:()->Unit={},onCopy:()->Unit={},onRefresh:()->Unit={}) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.authorization_title),style=MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.authorization_privacy),modifier=Modifier.testTag("authorization_privacy"))
            if(!state.available)Text(stringResource(R.string.authorization_unavailable))
            if(state.target==null)Text(stringResource(R.string.authorization_select))
            state.active?.let { Text(stringResource(R.string.authorization_active,it.meetingStart.toString(),it.expiresAt.toString()),modifier=Modifier.testTag("authorization_active")) }
                ?: Text(stringResource(R.string.authorization_none),modifier=Modifier.testTag("authorization_none"))
            Button(onAuthorize,enabled=state.available && state.target!=null && !state.busy,modifier=Modifier.testTag("authorization_issue")) {
                Text(stringResource(R.string.authorization_issue))
            }
            state.reference?.let{reference->
                Text(reference,modifier=Modifier.testTag("authorization_reference"))
                Text(stringResource(R.string.authorization_secret),style=MaterialTheme.typography.bodySmall)
                OutlinedButton(onCopy,enabled=!state.busy,modifier=Modifier.testTag("authorization_copy")){Text(stringResource(R.string.authorization_copy))}
            }
            OutlinedButton(onRevoke,enabled=!state.busy && (state.active!=null || state.error),modifier=Modifier.testTag("authorization_revoke")) {Text(stringResource(R.string.authorization_revoke))}
            TextButton(onRefresh,enabled=!state.busy,modifier=Modifier.testTag("authorization_refresh")){Text(stringResource(R.string.authorization_refresh))}
            if(state.error)Text(stringResource(R.string.authorization_error),color=MaterialTheme.colorScheme.error,modifier=Modifier.testTag("authorization_error"))
        }
    }
}
