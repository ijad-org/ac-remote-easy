package com.ijad.acremoteeasy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Sky,
    onPrimary = Ink,
    secondary = SkyDeep,
    onSecondary = Color.White,
    background = Ink,
    onBackground = Color(0xFFF8FAFC),
    surface = InkSoft,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF243044),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    error = Danger
)

private val LightColors = lightColorScheme(
    primary = SkyDeep,
    onPrimary = Color.White,
    secondary = Sky,
    onSecondary = Ink,
    background = Mist,
    onBackground = Ink,
    surface = MistCard,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE8EEF6),
    onSurfaceVariant = Slate,
    outline = Color(0xFFCBD5E1),
    error = Danger
)

@Composable
fun AcRemoteEasyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
