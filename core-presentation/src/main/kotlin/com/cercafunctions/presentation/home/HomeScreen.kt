package com.cercafunctions.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cercafunctions.presentation.R
import com.cercafunctions.presentation.theme.CopilotTheme

@Composable
fun HomeRoute(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(state = state, onRetry = viewModel::refresh)
}

@Composable
internal fun HomeScreen(state: HomeUiState, onRetry: () -> Unit) {
    Scaffold { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets)
                .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(stringResource(R.string.home_brand), style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.home_subtitle), style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.journey_heading), style = MaterialTheme.typography.titleMedium)
                    when (state) {
                        HomeUiState.Loading -> {
                            CircularProgressIndicator()
                            Text(stringResource(R.string.journey_loading))
                        }
                        HomeUiState.NotConfigured -> {
                            Text(stringResource(R.string.journey_not_configured), style = MaterialTheme.typography.titleLarge)
                            Text(stringResource(R.string.journey_empty_description))
                        }
                        is HomeUiState.Ready -> {
                            Text("${state.journey.origin} → ${state.journey.destination}", style = MaterialTheme.typography.titleLarge)
                            Text(stringResource(R.string.journey_duration, state.journey.estimatedDurationMinutes))
                        }
                        HomeUiState.Error -> {
                            Text(stringResource(R.string.journey_error))
                            Button(onClick = onRetry) { Text(stringResource(R.string.journey_retry)) }
                        }
                    }
                }
            }
            Text(stringResource(R.string.home_upcoming_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.home_upcoming_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.home_privacy), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    CopilotTheme { HomeScreen(HomeUiState.NotConfigured, onRetry = {}) }
}
