package com.cercafunctions.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.cercafunctions.presentation.home.HomeRoute
import com.cercafunctions.presentation.home.HomeViewModel
import com.cercafunctions.presentation.theme.CopilotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CopilotTheme {
                HomeRoute(viewModel)
            }
        }
    }
}
