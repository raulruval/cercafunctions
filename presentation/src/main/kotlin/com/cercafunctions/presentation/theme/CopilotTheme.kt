package com.cercafunctions.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF17665A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3EBE2),
    onPrimaryContainer = Color(0xFF123E36),
    background = Color(0xFFF7F8F3),
    surface = Color(0xFFF7F8F3),
    surfaceVariant = Color(0xFFE8EDE6),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FD5C4),
    primaryContainer = Color(0xFF174E43),
    background = Color(0xFF111C19),
    surface = Color(0xFF111C19),
)

@Composable
fun CopilotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
