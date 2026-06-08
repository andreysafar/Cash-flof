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
import ru.cashflow.statement.ads.LocalAdController
import ru.cashflow.statement.ui.CashflowApp
import ru.cashflow.statement.ui.theme.CashflowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CashflowTheme {
                val ad = remember { AdConfig.controller() }
                CompositionLocalProvider(LocalAdController provides ad) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxSize()) {
                            Box(Modifier.weight(1f).fillMaxWidth()) {
                                CashflowApp()
                            }
                            // Рекламный фрейм закреплён внизу, под всеми экранами.
                            ad.BannerFrame(Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}
