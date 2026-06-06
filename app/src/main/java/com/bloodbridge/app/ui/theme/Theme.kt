package com.bloodbridge.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BloodRed = Color(0xFFD32F2F)
val BloodRedDark = Color(0xFFB71C1C)
val BloodRedLight = Color(0xFFEF5350)
val White = Color(0xFFFFFFFF)
val GrayLight = Color(0xFFF5F5F5)
val Gray = Color(0xFF9E9E9E)
val GrayDark = Color(0xFF616161)
val Green = Color(0xFF4CAF50)
val Orange = Color(0xFFFF9800)
val Background = Color(0xFFFAFAFA)
val Surface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF1C1B1F)
val OnSurfaceVariant = Color(0xFF49454F)
val Outline = Color(0xFF79747E)

private val LightColorScheme = lightColorScheme(
    primary = BloodRed,
    onPrimary = White,
    primaryContainer = BloodRedLight,
    secondary = Green,
    onSecondary = White,
    background = Background,
    surface = Surface,
    onBackground = OnSurface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    error = BloodRedDark,
)

private val DarkColorScheme = darkColorScheme(
    primary = BloodRedLight,
    onPrimary = White,
    primaryContainer = BloodRedDark,
    secondary = Green,
    onSecondary = White,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    error = BloodRedLight,
)

@Composable
fun MZBloodBridgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
