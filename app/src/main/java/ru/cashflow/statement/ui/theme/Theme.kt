package ru.cashflow.statement.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val Green = Color(0xFF1F6F54)
private val GreenDark = Color(0xFF8FD8BD)
private val Gold = Color(0xFFB8860B)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    secondary = Gold,
    background = Color(0xFFF7F4EF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE7EFE9),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = GreenDark,
    onPrimary = Color(0xFF00382A),
    secondary = Color(0xFFE8C871),
    background = Color(0xFF101513),
    surface = Color(0xFF18211D),
    surfaceVariant = Color(0xFF26322C),
    error = Color(0xFFF2B8B5),
)

private val AppTypography = Typography(
    titleLarge = Typography().titleLarge.copy(fontSize = 22.sp),
    bodyLarge = Typography().bodyLarge.copy(fontSize = 17.sp),
    labelLarge = Typography().labelLarge.copy(fontSize = 16.sp),
)

@Composable
fun CashflowTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = if (dark) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(colorScheme = colors, typography = AppTypography, content = content)
}
