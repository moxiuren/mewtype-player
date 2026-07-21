package com.mewtype.player.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF7C4DFF),
    secondary = androidx.compose.ui.graphics.Color(0xFF448AFF),
    surface = androidx.compose.ui.graphics.Color(0xFFFFFBFE),
    background = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFB388FF),
    secondary = androidx.compose.ui.graphics.Color(0xFF82B1FF),
    surface = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
    background = androidx.compose.ui.graphics.Color(0xFF121212),
)

@Composable
fun MewtypeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
