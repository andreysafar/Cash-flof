package ru.cashflow.statement.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.my.target.nativeads.factories.NativeViewsFactory
import com.my.target.nativeads.views.NativeAdContainer

@Composable
internal fun NativeAdOverlayContent(controller: VkAdController, modifier: Modifier = Modifier) {
    val visible by controller.nativeOverlayVisible
    val nativeAd by controller.activeNativeAd
    if (!visible || nativeAd == null) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f)),
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth()
                .navigationBarsPadding(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            key(nativeAd) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        val adView = NativeViewsFactory.getNativeAdView(ctx)
                        adView.setupView(nativeAd.getBanner())
                        val container = NativeAdContainer(ctx)
                        container.addView(adView)
                        nativeAd.registerView(container)
                        container
                    },
                    onRelease = {
                        runCatching { nativeAd.unregisterView() }
                    },
                )
            }
        }
    }
}
