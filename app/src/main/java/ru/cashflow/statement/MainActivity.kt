package ru.cashflow.statement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ru.cashflow.statement.ui.CashflowApp
import ru.cashflow.statement.ui.theme.CashflowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CashflowTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CashflowApp()
                }
            }
        }
    }
}
