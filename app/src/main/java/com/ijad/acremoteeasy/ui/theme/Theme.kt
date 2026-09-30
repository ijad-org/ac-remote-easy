package com.ijad.acremoteeasy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    secondary = IndigoSoft,
    onSecondary = Color.White,
    tertiary = TealDeep,
    background = Color(0xFF111827),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF1F2937),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    error = Danger
)

private val LightColors = lightColorScheme(
    primary = TealDeep,
    onPrimary = Color.White,
    secondary = IndigoSoft,
    onSecondary = Color.White,
    tertiary = Teal,
    background = Mist,
    onBackground = Ink,
    surface = MistCard,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Slate,
    outline = Color(0xFFCBD5E1),
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = TealDeep,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF312E81),
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
