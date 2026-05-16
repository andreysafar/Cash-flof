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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

/** Фирменные тексты приложения. */
object Brand {
    const val NAME = "Денежный поток"
    const val TAGLINE = "Прокачай своего миллионера за 3 часа"
}

// Палитра: глубокий изумруд + золото — «деньги/успех».
private val Emerald = Color(0xFF0E5C43)
private val EmeraldDeep = Color(0xFF0A4633)
private val EmeraldLight = Color(0xFF7FD6B6)
private val Gold = Color(0xFFC9A227)
private val GoldSoft = Color(0xFFE8C871)
private val Ink = Color(0xFF11221C)

private val LightColors = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC9EFDD),
    onPrimaryContainer = EmeraldDeep,
    secondary = Gold,
    onSecondary = Color(0xFF2A2000),
    secondaryContainer = Color(0xFFF6E6BC),
    onSecondaryContainer = Color(0xFF3A2D00),
    tertiary = Color(0xFF2E6F8E),
    background = Color(0xFFF5F3EC),
    onBackground = Ink,
    surface = Color(0xFFFFFFFF),
    onSurface = Ink,
    surfaceVariant = Color(0xFFDDE9E2),
    onSurfaceVariant = Color(0xFF3C4A43),
    outline = Color(0xFF9BAEA4),
    error = Color(0xFFB3261E),
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color(0xFF00251A),
    primaryContainer = Color(0xFF0C4A37),
    onPrimaryContainer = Color(0xFFB9EFD7),
    secondary = GoldSoft,
    onSecondary = Color(0xFF2A2000),
    secondaryContainer = Color(0xFF4A3A09),
    onSecondaryContainer = Color(0xFFF6E6BC),
    tertiary = Color(0xFF8CCBE6),
    background = Color(0xFF0D1411),
    onBackground = Color(0xFFE3E7E4),
    surface = Color(0xFF15201B),
    onSurface = Color(0xFFE3E7E4),
    surfaceVariant = Color(0xFF26322C),
    onSurfaceVariant = Color(0xFFBFCCC4),
    outline = Color(0xFF5C6B63),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
)

private val base = Typography()
private val AppTypography = Typography(
    displaySmall = base.displaySmall.copy(fontSize = 34.sp, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontSize = 17.sp),
    bodyMedium = base.bodyMedium.copy(fontSize = 15.sp),
    labelLarge = base.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = dark
        }
    }
    MaterialTheme(colorScheme = colors, typography = AppTypography, content = content)
}
