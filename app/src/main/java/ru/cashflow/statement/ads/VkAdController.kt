package ru.cashflow.statement.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.my.target.ads.MyTargetView

/**
 * Реклама через VK Ad SDK (myTarget):
 *  1. **Постоянный баннер** 320×50 внизу всех экранов ([BannerFrame]);
 *  2. **Короткий всплывающий баннер** через несколько секунд после хода
 *     ([onMoveCompleted] → [NativeAdOverlay]) — карточка над постоянным
 *     баннером, авто-скрытие через [AdConfig.POPUP_DISPLAY_MS] или по «×».
 *
 * Оба формата используют один виджет [MyTargetView] (минимальный стабильный
 * API: setSlotId/load/destroy) — без слушателей с версионно-зависимыми
 * сигнатурами, на которых раньше падала сборка.
 *
 * Interstitial оставлен заглушкой (см. docs/VK_ADS.md).
 */
class VkAdController(@Suppress("UNUSED_PARAMETER") appContext: Context) : AdController {

    private val mainHandler = Handler(Looper.getMainLooper())

    /** Видимость всплывающего баннера; читается из Compose, меняется из Handler. */
    internal val popupVisible: MutableState<Boolean> = mutableStateOf(false)

    /** Токен показа — каждый новый ход увеличивает его, чтобы пересоздать баннер. */
    internal val popupToken: MutableState<Int> = mutableStateOf(0)

    private val showRunnable = Runnable {
        popupToken.value = popupToken.value + 1
        popupVisible.value = true
        mainHandler.removeCallbacks(hideRunnable)
        mainHandler.postDelayed(hideRunnable, AdConfig.POPUP_DISPLAY_MS)
    }
    private val hideRunnable = Runnable { popupVisible.value = false }

    override fun preload(activity: Activity?) {
        // Баннеры грузятся самостоятельно в своих AndroidView; предзагрузка не нужна.
    }

    @Composable
    override fun BannerFrame(modifier: Modifier) {
        BannerView(
            slotId = AdConfig.BANNER_SLOT_ID,
            modifier = modifier.fillMaxWidth().navigationBarsPadding().height(50.dp),
        )
    }

    /** Запланировать короткий баннер после хода (с перезапуском таймера). */
    override fun onMoveCompleted(activity: Activity?) {
        if (AdConfig.POPUP_BANNER_SLOT_ID <= 0) return
        mainHandler.removeCallbacks(showRunnable)
        mainHandler.removeCallbacks(hideRunnable)
        popupVisible.value = false
        mainHandler.postDelayed(showRunnable, AdConfig.POPUP_DELAY_MS)
    }

    @Composable
    override fun NativeAdOverlay(modifier: Modifier) {
        val visible by popupVisible
        val token by popupToken
        Box(modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 12.dp, end = 12.dp, bottom = 60.dp),
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                ) {
                    Column(Modifier.fillMaxWidth().padding(8.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Реклама",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            IconButton(
                                onClick = {
                                    mainHandler.removeCallbacks(hideRunnable)
                                    popupVisible.value = false
                                },
                                modifier = Modifier.height(28.dp),
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Закрыть",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        // token входит в key, чтобы каждый показ создавал свежий баннер.
                        androidx.compose.runtime.key(token) {
                            BannerView(
                                slotId = AdConfig.POPUP_BANNER_SLOT_ID,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onInterstitialMoment(activity: Activity?) { /* interstitial отключён в MVP */ }

    override fun dispose() {
        mainHandler.removeCallbacksAndMessages(null)
        popupVisible.value = false
    }
}

/** Простой баннер MyTargetView заданного слота (минимальный API SDK). */
@Composable
private fun BannerView(slotId: Int, modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MyTargetView(ctx).apply {
                setSlotId(slotId)
                load()
            }
        },
        onRelease = { it.destroy() },
    )
}
