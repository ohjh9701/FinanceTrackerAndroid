package com.finance.localtracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colors = lightColorScheme(
    primary = Color(0xFF172033),
    onPrimary = Color.White,
    secondary = Color(0xFF5C667A),
    background = Color(0xFFF5F7FA),
    surface = Color.White,
    onSurface = Color(0xFF141821),
    surfaceVariant = Color(0xFFEEF1F5)
)

@Composable
fun FinanceTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
