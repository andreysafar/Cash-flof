package ru.cashflow.statement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.material3.Surface
import ru.cashflow.statement.ads.AdConfig
import ru.cashflow.statement.ads.AdController
import ru.cashflow.statement.ads.LocalAdController
import ru.cashflow.statement.ui.CashflowApp
import ru.cashflow.statement.ui.theme.CashflowTheme

class MainActivity : ComponentActivity() {

    private lateinit var adController: AdController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        adController = AdConfig.controller(applicationContext)
        adController.preload(this)
        setContent {
            CashflowTheme {
                val ad = remember { adController }
                CompositionLocalProvider(LocalAdController provides ad) {
                    Box(Modifier.fillMaxSize()) {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            Column(Modifier.fillMaxSize()) {
                                Box(Modifier.weight(1f).fillMaxWidth()) {
                                    CashflowApp()
                                }
                                ad.BannerFrame(Modifier.fillMaxWidth())
                            }
                        }
                        ad.NativeAdOverlay(Modifier.fillMaxSize())
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        adController.dispose()
        super.onDestroy()
    }
}
