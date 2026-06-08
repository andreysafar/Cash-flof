package ru.cashflow.statement.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.my.target.ads.InterstitialAd
import com.my.target.ads.MyTargetView
import com.my.target.common.MyTargetManager
import com.my.target.common.models.IAdLoadingError
import com.my.target.nativeads.NativeAd
import com.my.target.nativeads.banners.NativePromoBanner
import ru.cashflow.statement.BuildConfig
import java.lang.ref.WeakReference

/**
 * VK Ad SDK (myTarget): banner, native overlay after moves, interstitial on key moments.
 */
class VkAdController(@Suppress("UNUSED_PARAMETER") appContext: Context) : AdController {

    private val mainHandler = Handler(Looper.getMainLooper())

    internal val nativeOverlayVisible: MutableState<Boolean> = mutableStateOf(false)
    internal val activeNativeAd: MutableState<NativeAd?> = mutableStateOf(null)

    private var pendingActivity = WeakReference<Activity>(null)
    private var nativePreload: NativeAd? = null
    private var nativeReady = false
    private var nativeScheduleToken = 0

    private var interstitial: InterstitialAd? = null
    private var interstitialReady = false
    private var movesSinceLastAd = 0

    private val showNativeRunnable = Runnable { tryShowNativeOverlay() }
    private val hideNativeRunnable = Runnable { hideNativeOverlay() }

    init {
        if (BuildConfig.DEBUG) {
            MyTargetManager.setDebugMode(true)
        }
    }

    override fun preload(activity: Activity?) {
        activity ?: return
        pendingActivity = WeakReference(activity)
        loadNativeAd(activity)
        loadInterstitial(activity)
    }

    override fun onMoveCompleted(activity: Activity?) {
        activity ?: return
        if (AdConfig.NATIVE_SLOT_ID <= 0) return
        pendingActivity = WeakReference(activity)
        if (nativeOverlayVisible.value) {
            mainHandler.removeCallbacks(hideNativeRunnable)
            hideNativeOverlay()
        }
        nativeScheduleToken++
        mainHandler.removeCallbacks(showNativeRunnable)
        mainHandler.postDelayed(showNativeRunnable, AdConfig.NATIVE_AD_DELAY_MS)
    }

    @Composable
    override fun NativeAdOverlay(modifier: Modifier) {
        NativeAdOverlayContent(this, modifier)
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
                    setAdSize(MyTargetView.AdSize.ADSIZE_320x50)
                    setRefreshAd(false)
                    setListener(object : MyTargetView.MyTargetViewListener {
                        override fun onLoad(view: MyTargetView) = Unit
                        override fun onNoAd(reason: IAdLoadingError, view: MyTargetView) = Unit
                        override fun onShow(view: MyTargetView) = Unit
                        override fun onClick(view: MyTargetView) = Unit
                    })
                    load()
                }
            },
            onRelease = { it.destroy() },
        )
    }

    override fun onInterstitialMoment(activity: Activity?) {
        activity ?: return
        val slotId = AdConfig.INTERSTITIAL_SLOT_ID
        if (slotId <= 0) return

        movesSinceLastAd++
        if (movesSinceLastAd < AdConfig.MIN_MOVES_BETWEEN_INTERSTITIALS) return

        val ad = interstitial
        if (ad != null && interstitialReady) {
            movesSinceLastAd = 0
            interstitialReady = false
            ad.show()
        } else {
            loadInterstitial(activity)
        }
    }

    override fun dispose() {
        mainHandler.removeCallbacksAndMessages(null)
        hideNativeOverlay()
        interstitial?.destroy()
        interstitial = null
    }

    private fun tryShowNativeOverlay() {
        val activity = pendingActivity.get() ?: return
        if (!nativeReady) {
            loadNativeAd(activity)
            return
        }
        val ad = nativePreload ?: return
        nativePreload = null
        nativeReady = false
        activeNativeAd.value = ad
        nativeOverlayVisible.value = true
        mainHandler.postDelayed(hideNativeRunnable, AdConfig.NATIVE_AD_DISPLAY_MS)
    }

    private fun hideNativeOverlay() {
        nativeOverlayVisible.value = false
        activeNativeAd.value?.let { ad ->
            runCatching { ad.unregisterView() }
            ad.destroy()
        }
        activeNativeAd.value = null
        pendingActivity.get()?.let { loadNativeAd(it) }
    }

    private fun loadNativeAd(activity: Activity) {
        val slotId = AdConfig.NATIVE_SLOT_ID
        if (slotId <= 0) return

        nativePreload?.destroy()
        nativePreload = null
        nativeReady = false

        nativePreload = NativeAd(slotId, activity).apply {
            setListener(object : NativeAd.NativeAdListener {
                override fun onLoad(banner: NativePromoBanner, ad: NativeAd) {
                    nativeReady = true
                }

                override fun onNoAd(reason: IAdLoadingError, ad: NativeAd) {
                    nativeReady = false
                }

                override fun onClick(ad: NativeAd) = Unit
                override fun onShow(ad: NativeAd) = Unit
                override fun onVideoPlay(ad: NativeAd) = Unit
                override fun onVideoPause(ad: NativeAd) = Unit
                override fun onVideoComplete(ad: NativeAd) = Unit
            })
            load()
        }
    }

    private fun loadInterstitial(activity: Activity) {
        val slotId = AdConfig.INTERSTITIAL_SLOT_ID
        if (slotId <= 0) return

        interstitial?.destroy()
        interstitialReady = false
        interstitial = InterstitialAd(slotId, activity).apply {
            setListener(object : InterstitialAd.InterstitialAdListener {
                override fun onLoad(ad: InterstitialAd) {
                    interstitialReady = true
                }

                override fun onNoAd(reason: IAdLoadingError, ad: InterstitialAd) {
                    interstitialReady = false
                }

                override fun onClick(ad: InterstitialAd) = Unit
                override fun onDisplay(ad: InterstitialAd) = Unit

                override fun onDismiss(ad: InterstitialAd) {
                    interstitialReady = false
                    loadInterstitial(activity)
                }

                override fun onVideoCompleted(ad: InterstitialAd) = Unit
            })
            load()
        }
    }
}
