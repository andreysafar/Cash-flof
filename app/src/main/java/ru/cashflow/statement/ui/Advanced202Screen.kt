package ru.cashflow.statement.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
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
import ru.cashflow.statement.model.StockHolding
import ru.cashflow.statement.model.StockType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Advanced202Screen(vm: StatementViewModel, onBack: () -> Unit) {
    val s = vm.state
    val calls = s.stocks.filter { it.type == StockType.CALL_OPTION }
    val puts = s.stocks.filter { it.type == StockType.PUT_OPTION }
    val shorts = s.stocks.filter { it.type == StockType.SHORT }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Опционы и шорт (202)") },
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
            SectionCard("Как добавить позицию") {
                Text(
                    "Позиции добавляются на главном экране кнопкой «Купить акции» " +
                        "(тип Шорт / Опцион CALL / Опцион PUT). Здесь — учёт и закрытие.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            SectionCard("Опционы CALL (ставка на рост)") {
                if (calls.isEmpty()) Text("Нет открытых CALL-опционов.")
                calls.forEach { OptionRow(vm, it, isCall = true) }
            }

            SectionCard("Опционы PUT (ставка на падение)") {
                if (puts.isEmpty()) Text("Нет открытых PUT-опционов.")
                puts.forEach { OptionRow(vm, it, isCall = false) }
            }

            SectionCard("Короткие позиции (Short Sales)") {
                if (shorts.isEmpty()) Text("Нет открытых коротких позиций.")
                shorts.forEach { ShortRow(vm, it) }
            }
        }
    }
}

@Composable
private fun OptionRow(vm: StatementViewModel, h: StockHolding, isCall: Boolean) {
    var newPrice by remember { mutableStateOf(0L) }
    val totalPaid = h.shares * h.pricePerShare
    val payoff = if (isCall) Calculator.callOptionPayoff(h, newPrice)
    else Calculator.putOptionPayoff(h, newPrice)

    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            "${h.symbol} • ${h.shares} акц. • опцион/акц. ${money(h.pricePerShare)} • " +
                "strike ${money(h.strikePrice)} • кругов: ${h.turnsLeft}",
            fontWeight = FontWeight.SemiBold,
        )
        Text("Всего оплачено: ${money(totalPaid)}", style = MaterialTheme.typography.bodyMedium)
        NumberField("Новая цена сегодня", newPrice, { newPrice = it })
        Text(
            "Прибыль в цене: ${money(if (isCall) newPrice - h.strikePrice else h.strikePrice - newPrice)} • " +
                "Сумма к получению: ${money(payoff)}",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedButton(
            onClick = { vm.closeStock(h.id, newPrice) },
            modifier = Modifier.padding(top = 4.dp),
        ) { Text("Исполнить / закрыть") }
        HorizontalDivider(Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun ShortRow(vm: StatementViewModel, h: StockHolding) {
    var buyback by remember { mutableStateOf(0L) }
    val totalSales = h.shares * h.pricePerShare
    val gainLoss = Calculator.shortGainLoss(h, buyback)

    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            "${h.symbol} • ${h.shares} акц. • цена продажи ${money(h.pricePerShare)}",
            fontWeight = FontWeight.SemiBold,
        )
        Text("Всего сумма продажи: ${money(totalSales)}", style = MaterialTheme.typography.bodyMedium)
        NumberField("Цена выкупа за акцию", buyback, { buyback = it })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Всего выкуп: ${money(h.shares * buyback)}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Итог: ${money(gainLoss)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (gainLoss < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
        }
        OutlinedButton(
            onClick = { vm.closeStock(h.id, buyback) },
            modifier = Modifier.padding(top = 4.dp),
        ) { Text("Закрыть короткую позицию") }
        HorizontalDivider(Modifier.padding(top = 8.dp))
    }
}
