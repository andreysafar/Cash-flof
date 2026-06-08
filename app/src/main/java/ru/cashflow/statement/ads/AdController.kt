package ru.cashflow.statement.ads

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Абстракция рекламы. UI не знает о конкретной сети (RuStore Ads и т.п.) —
 * подключение SDK затрагивает только реализацию этого интерфейса.
 * Шаги включения реальной рекламы — в docs/RUSTORE_ADS.md.
 */
interface AdController {
    /** Рекламный фрейм-баннер внизу экрана: рисует баннер либо ничего. */
    @Composable
    fun BannerFrame(modifier: Modifier)

    /** Подходящий момент для межстраничной (interstitial) рекламы. */
    fun onInterstitialMoment(activity: Activity?)

    /** Предзагрузка объявлений (вызывается из MainActivity). */
    fun preload(activity: Activity?)
}

/** Заглушка: рекламы нет (по умолчанию, пока не подключён SDK). */
object NoOpAdController : AdController {
    @Composable override fun BannerFrame(modifier: Modifier) {}
    override fun onInterstitialMoment(activity: Activity?) {}
    override fun preload(activity: Activity?) {}
}

/**
 * Видимый плейсхолдер баннера: показывает, как и где будет реклама, и
 * фиксирует вёрстку до подключения SDK. Сеть не используется.
 */
object PlaceholderAdController : AdController {
    @Composable
    override fun BannerFrame(modifier: Modifier) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 3.dp,
        ) {
            Box(
                Modifier.fillMaxWidth().navigationBarsPadding().height(56.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Место для рекламного баннера (RuStore Ads)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    override fun onInterstitialMoment(activity: Activity?) {}
    override fun preload(activity: Activity?) {}
}

/** Доступ к рекламному контроллеру из любого экрана. */
val LocalAdController = staticCompositionLocalOf<AdController> { NoOpAdController }
