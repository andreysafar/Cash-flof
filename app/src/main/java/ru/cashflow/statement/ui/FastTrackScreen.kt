package ru.cashflow.statement.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.cashflow.statement.logic.Calculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastTrackScreen(vm: StatementViewModel, onBack: () -> Unit) {
    val s = vm.state
    var dialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Скоростная дорожка") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()),
        ) {
            if (!s.onFastTrack) {
                SectionCard("Вы ещё в крысиных бегах") {
                    Text(
                        "Скоростная дорожка станет доступна после того, как пассивный " +
                            "доход превысит общий расход. Тогда нажмите «Выйти на скоростную дорожку».",
                    )
                }
                return@Column
            }

            val cashflowDay = Calculator.fastTrackCashflowDay(s)
            val target = Calculator.fastTrackWinTarget(s)

            Card(
                Modifier.fillMaxWidth().padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Доход в ДЕНЬ CASHFLOW", color = MaterialTheme.colorScheme.onPrimary)
                    Text(
                        money(cashflowDay),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text("Наличные: ${money(s.cash)}", color = MaterialTheme.colorScheme.onPrimary)
                    Button(
                        onClick = { vm.fastTrackPayday() },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    ) { Text("ДЕНЬ CASHFLOW  (+${money(cashflowDay)})", fontWeight = FontWeight.Bold) }
                }
            }

            SectionCard("Цель победы") {
                Text("Начальный доход: ${money(s.fastTrackStartIncome)}")
                Text("Целевой доход (победа): ${money(target)}")
                Text("Осталось прибавить: ${money((target - cashflowDay).coerceAtLeast(0))}")
                Text(
                    "Победа: купить мечту ИЛИ первым увеличить доход в ДЕНЬ CASHFLOW на \$50 000.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            SectionCard("Бизнесы скоростной дорожки") {
                if (s.fastTrackBusinesses.isEmpty()) {
                    Text("Пока не куплено ни одного бизнеса.")
                }
                s.fastTrackBusinesses.forEach { b ->
                    StatRow(b.name, b.monthlyIncome)
                    OutlinedButton(
                        onClick = { vm.removeFastTrackBusiness(b.id) },
                        modifier = Modifier.padding(bottom = 6.dp),
                    ) { Text("Удалить «${b.name}»") }
                }
            }

            SectionCard("Действия") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { dialog = true }) { Text("Купить бизнес") }
                    OutlinedButton(onClick = { vm.fastTrackPayHalfCash("Налоговая/развод/иск") }) {
                        Text("Минус ½ наличных")
                    }
                    OutlinedButton(onClick = { vm.fastTrackPayAllCash("Забота о здоровье") }) {
                        Text("Минус все наличные")
                    }
                    OutlinedButton(onClick = { vm.undo() }) { Text("Отмена") }
                }
            }
        }
    }

    if (dialog) FastTrackBusinessDialog(vm) { dialog = false }
}
