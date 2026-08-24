package com.example.medicationreminder.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand palette (medical teal/green).
private val PrimaryLight = Color(0xFF00695C)
private val PrimaryDark = Color(0xFF4DB6AC)
private val SecondaryLight = Color(0xFF00897B)
private val SecondaryDark = Color(0xFF80CBC4)

private val LightColors = lightColorScheme(
    primary = PrimaryLight,
    secondary = SecondaryLight,
    tertiary = Color(0xFF5E35B1)
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = Color(0xFFB39DDB),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

@Composable
fun MediRemindTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
