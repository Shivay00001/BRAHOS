package com.brahos.app.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF006C4C),
    secondary = Color(0xFF4D6357),
    tertiary = Color(0xFF3D6373)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006C4C),
    secondary = Color(0xFF4D6357),
    tertiary = Color(0xFF3D6373),
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFBFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF191C1A),
    onSurface = Color(0xFF191C1A)
)

@Composable
fun BrahosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
