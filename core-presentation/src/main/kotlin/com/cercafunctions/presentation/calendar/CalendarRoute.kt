package com.cercafunctions.presentation.calendar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** User-driven permission boundary; domain/repository never open permission dialogs. */
@Composable fun CalendarRoute(viewModel:CalendarViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context=LocalContext.current
    val keyboard=LocalSoftwareKeyboardController.current
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(),viewModel::onPermissionResult)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME){viewModel.onResume()}
    CalendarPanel(state,CalendarActions(viewModel::updateDate,viewModel::updateZone,viewModel::tomorrow,
        onConsult={
            keyboard?.hide()
            if(viewModel.prepareConsult()) {
                if(context.checkSelfPermission(Manifest.permission.READ_CALENDAR)==PackageManager.PERMISSION_GRANTED)viewModel.consult()
                else permission.launch(Manifest.permission.READ_CALENDAR)
            }
        },onSettings={context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))},onSelect=viewModel::select,onRevalidate=viewModel::revalidate))
}
