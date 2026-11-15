package com.cercafunctions.app

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cercafunctions.presentation.demo.DemoRoute
import com.cercafunctions.presentation.demo.DemoViewModel
import com.cercafunctions.presentation.R
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.cercafunctions.presentation.authorization.AssessmentSharingViewModel
import com.cercafunctions.presentation.sharing.SharingViewModel
import com.cercafunctions.presentation.calendar.CalendarViewModel
import com.cercafunctions.presentation.rail.RailContextViewModel
import com.cercafunctions.presentation.estimate.ArrivalViewModel
import com.cercafunctions.presentation.home.HomeRoute
import com.cercafunctions.presentation.home.HomeViewModel
import com.cercafunctions.presentation.theme.CopilotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val demoViewModel:DemoViewModel by viewModels()
    private val authorizationViewModel:AssessmentSharingViewModel by viewModels()
    private val sharingViewModel:SharingViewModel by viewModels()
    private val calendarViewModel: CalendarViewModel by viewModels()
    private val railViewModel: RailContextViewModel by viewModels()
    private val arrivalViewModel: ArrivalViewModel by viewModels()
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CopilotTheme {
                var advanced by rememberSaveable { mutableStateOf(false) }
                BackHandler(enabled=advanced){advanced=false}
                if(!advanced)DemoRoute(demoViewModel,onAdvanced={advanced=true})
                else Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                    TextButton(onClick={advanced=false}){Text(stringResource(R.string.demo_back))}
                    Box(Modifier.weight(1f)) {
                        HomeRoute(viewModel, arrivalViewModel, railViewModel, calendarViewModel, sharingViewModel, authorizationViewModel)
                    }
                }
            }
        }
    }
}
