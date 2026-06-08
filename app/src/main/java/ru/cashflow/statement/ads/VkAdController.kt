package ru.cashflow.statement.ads

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.my.target.ads.MyTargetView

/**
 * MVP-реклама через VK Ad SDK (myTarget): **только баннер 320×50** внизу экрана.
 *
 * Нативный оверлей (всплывал после каждого хода) и межстраничная реклама
 * намеренно отключены для MVP — это и есть «слишком много рекламы»: баннер
 * приносит доход постоянно и почти не мешает игре. Точки расширения сохранены
 * в интерфейсе [AdController]; как вернуть нативную/interstitial — docs/VK_ADS.md.
 *
 * Реализация умышленно использует минимум API SDK (MyTargetView.setSlotId/load/
 * destroy) — без слушателей с версионно-зависимыми сигнатурами.
 */
class VkAdController(@Suppress("UNUSED_PARAMETER") appContext: Context) : AdController {

    override fun preload(activity: Activity?) {
        // Баннер загружается самостоятельно в BannerFrame; предзагрузка не нужна.
    }

    @Composable
    override fun BannerFrame(modifier: Modifier) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(50.dp),
            factory = { ctx ->
                MyTargetView(ctx).apply {
                    setSlotId(AdConfig.BANNER_SLOT_ID)
                    load()
                }
            },
            onRelease = { it.destroy() },
        )
    }

    // --- Отключено в MVP (достаточно баннера). Включение — docs/VK_ADS.md ---

    override fun onMoveCompleted(activity: Activity?) { /* нативная реклама отключена */ }

    override fun onInterstitialMoment(activity: Activity?) { /* interstitial отключён */ }

    @Composable
    override fun NativeAdOverlay(modifier: Modifier) { /* нет оверлея в MVP */ }

    override fun dispose() { /* нет ресурсов SDK для освобождения */ }
}
