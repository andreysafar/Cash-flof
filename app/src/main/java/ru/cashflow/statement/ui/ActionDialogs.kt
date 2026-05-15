package ru.cashflow.statement.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.cashflow.statement.model.FinancialStatement
import ru.cashflow.statement.model.StockType

@Composable
private fun DialogShell(
    title: String,
    confirmEnabled: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    body: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) { body() }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = confirmEnabled) { Text("Готово") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
fun BuyStockDialog(vm: StatementViewModel, onClose: () -> Unit) {
    var symbol by remember { mutableStateOf("") }
    var shares by remember { mutableStateOf(0L) }
    var price by remember { mutableStateOf(0L) }
    var dividend by remember { mutableStateOf(0L) }
    var strike by remember { mutableStateOf(0L) }
    var type by remember { mutableStateOf(StockType.LONG) }

    DialogShell(
        title = "Покупка ценных бумаг",
        confirmEnabled = symbol.isNotBlank() && shares > 0 && price > 0,
        onConfirm = { vm.buyStock(symbol.trim(), shares, price, type, dividend, strike); onClose() },
        onDismiss = onClose,
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StockType.entries.forEach { t ->
                FilterChip(selected = type == t, onClick = { type = t }, label = { Text(stockTypeLabel(t)) })
            }
        }
        TextInputField("Символ акции", symbol, { symbol = it })
        NumberField("Количество", shares, { shares = it })
        NumberField(
            if (type == StockType.CALL_OPTION || type == StockType.PUT_OPTION)
                "Стоимость опциона за акцию" else "Цена за акцию",
            price, { price = it },
        )
        if (type == StockType.LONG) {
            NumberField("Дивиденд на акцию (мес., если есть)", dividend, { dividend = it })
        }
        if (type == StockType.CALL_OPTION || type == StockType.PUT_OPTION) {
            NumberField("Объявленная цена (strike)", strike, { strike = it })
            Text(
                "Опцион действует 3 хода (счётчик кругов).",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
fun CloseStockDialog(vm: StatementViewModel, state: FinancialStatement, onClose: () -> Unit) {
    if (state.stocks.isEmpty()) {
        DialogShell("Закрыть позицию", false, onClose, onClose) { Text("Нет открытых позиций.") }
        return
    }
    var selected by remember { mutableStateOf(state.stocks.first().id) }
    var currentPrice by remember { mutableStateOf(0L) }
    val h = state.stocks.firstOrNull { it.id == selected } ?: state.stocks.first()

    DialogShell(
        title = "Закрытие позиции",
        confirmEnabled = currentPrice >= 0,
        onConfirm = { vm.closeStock(selected, currentPrice); onClose() },
        onDismiss = onClose,
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.stocks.forEach { s ->
                FilterChip(
                    selected = selected == s.id,
                    onClick = { selected = s.id },
                    label = { Text("${s.symbol} ×${s.shares} (${stockTypeLabel(s.type)})") },
                )
            }
        }
        val label = when (h.type) {
            StockType.LONG -> "Цена продажи за акцию"
            StockType.SHORT -> "Цена выкупа за акцию"
            else -> "Текущая цена акции"
        }
        NumberField(label, currentPrice, { currentPrice = it })
    }
}

@Composable
fun SplitStockDialog(vm: StatementViewModel, state: FinancialStatement, onClose: () -> Unit) {
    if (state.stocks.isEmpty()) {
        DialogShell("Сплит акций", false, onClose, onClose) { Text("Нет открытых позиций.") }
        return
    }
    var selected by remember { mutableStateOf(state.stocks.first().id) }
    DialogShell(
        title = "Сплит / обратный сплит",
        onConfirm = onClose,
        onDismiss = onClose,
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.stocks.forEach { s ->
                FilterChip(
                    selected = selected == s.id,
                    onClick = { selected = s.id },
                    label = { Text("${s.symbol} ×${s.shares}") },
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { vm.splitStock(selected, true); onClose() }) { Text("×2 (сплит)") }
            TextButton(onClick = { vm.splitStock(selected, false); onClose() }) { Text("÷2 (обратный)") }
        }
    }
}

@Composable
fun PropertyDialog(vm: StatementViewModel, onClose: () -> Unit) {
    var isBusiness by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var down by remember { mutableStateOf(0L) }
    var price by remember { mutableStateOf(0L) }
    var flow by remember { mutableStateOf(0L) }
    var mortgage by remember { mutableStateOf(0L) }

    DialogShell(
        title = "Покупка актива",
        confirmEnabled = name.isNotBlank(),
        onConfirm = { vm.buyProperty(isBusiness, name.trim(), down, price, flow, mortgage); onClose() },
        onDismiss = onClose,
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !isBusiness, onClick = { isBusiness = false }, label = { Text("Недвижимость") })
            FilterChip(selected = isBusiness, onClick = { isBusiness = true }, label = { Text("Бизнес") })
        }
        TextInputField("Название", name, { name = it })
        NumberField("Первый взнос", down, { down = it })
        NumberField("Цена", price, { price = it })
        NumberField("Денежный поток (в месяц)", flow, { flow = it })
        NumberField("Ипотека / пассив по объекту", mortgage, { mortgage = it })
    }
}

@Composable
fun SellPropertyDialog(vm: StatementViewModel, state: FinancialStatement, onClose: () -> Unit) {
    val all = state.realEstate.map { it to false } + state.businesses.map { it to true }
    if (all.isEmpty()) {
        DialogShell("Продажа актива", false, onClose, onClose) { Text("Нет недвижимости или бизнеса.") }
        return
    }
    var idx by remember { mutableStateOf(0) }
    var salePrice by remember { mutableStateOf(0L) }
    val (p, isBiz) = all[idx]

    DialogShell(
        title = "Продажа актива",
        onConfirm = { vm.sellProperty(isBiz, p.id, salePrice); onClose() },
        onDismiss = onClose,
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            all.forEachIndexed { i, (item, _) ->
                FilterChip(selected = idx == i, onClick = { idx = i }, label = { Text(item.name) })
            }
        }
        Text(
            "Ипотека по объекту: ${money(p.mortgage)}. Прирост капитала = Цена продажи − Ипотека.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 6.dp),
        )
        NumberField("Цена продажи", salePrice, { salePrice = it })
    }
}

@Composable
fun BankLoanDialog(vm: StatementViewModel, onClose: () -> Unit) {
    var amount by remember { mutableStateOf(0L) }
    val valid = amount > 0 && amount % 1000 == 0L
    DialogShell(
        title = "Кредит банка",
        confirmEnabled = valid,
        onConfirm = { vm.takeBankLoan(amount); onClose() },
        onDismiss = onClose,
    ) {
        Text(
            "Кредит кратен \$1000. Платёж = 10% в месяц (\$100 на каждые \$1000).",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        NumberField("Сумма кредита (кратно 1000)", amount, { amount = it })
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { if (valid) { vm.repayBankLoan(amount); onClose() } }, enabled = valid) {
                Text("Погасить эту сумму")
            }
        }
    }
}

@Composable
fun RepayDebtDialog(vm: StatementViewModel, state: FinancialStatement, onClose: () -> Unit) {
    val items = listOf(
        Triple("Ипотека", DebtKind.HOME, state.homeMortgage),
        Triple("Кредит на образование", DebtKind.EDU, state.eduLoan),
        Triple("Кредит на автомобиль", DebtKind.CAR, state.carLoan),
        Triple("Кредитная карточка", DebtKind.CREDIT_CARD, state.creditCardDebt),
        Triple("Мелкие кредиты", DebtKind.RETAIL, state.retailDebt),
    ).filter { it.third > 0 }
    if (items.isEmpty()) {
        DialogShell("Погашение долга", false, onClose, onClose) { Text("Нет долгов из профессии.") }
        return
    }
    DialogShell(title = "Полное погашение долга", onConfirm = onClose, onDismiss = onClose) {
        Text(
            "Долги из профессии гасятся только полностью.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        items.forEach { (label, kind, bal) ->
            TextButton(onClick = { vm.repayDebt(kind); onClose() }, modifier = Modifier.fillMaxWidth()) {
                Text("$label — ${money(bal)}")
            }
        }
    }
}

@Composable
fun AmountDialog(title: String, hint: String, onConfirm: (Long) -> Unit, onClose: () -> Unit) {
    var amount by remember { mutableStateOf(0L) }
    DialogShell(
        title = title,
        confirmEnabled = amount > 0,
        onConfirm = { onConfirm(amount); onClose() },
        onDismiss = onClose,
    ) {
        if (hint.isNotBlank()) {
            Text(hint, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 6.dp))
        }
        NumberField("Сумма", amount, { amount = it })
    }
}

@Composable
fun SetCashDialog(vm: StatementViewModel, state: FinancialStatement, onClose: () -> Unit) {
    var value by remember { mutableStateOf(state.cash) }
    DialogShell(
        title = "Корректировка наличных",
        onConfirm = { vm.setCash(value); onClose() },
        onDismiss = onClose,
    ) {
        Text(
            "Текущие наличные: ${money(state.cash)}. Укажите фактическое значение.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        NumberField("Наличные", value, { value = it })
    }
}

@Composable
fun ConfirmDialog(title: String, message: String, onConfirm: () -> Unit, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = { onConfirm(); onClose() }) { Text("Да") } },
        dismissButton = { TextButton(onClick = onClose) { Text("Отмена") } },
    )
}

@Composable
fun FastTrackBusinessDialog(vm: StatementViewModel, onClose: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var down by remember { mutableStateOf(0L) }
    var income by remember { mutableStateOf(0L) }
    DialogShell(
        title = "Бизнес на скоростной дорожке",
        confirmEnabled = name.isNotBlank() && income > 0,
        onConfirm = { vm.buyFastTrackBusiness(name.trim(), down, income); onClose() },
        onDismiss = onClose,
    ) {
        TextInputField("Тип бизнеса", name, { name = it })
        NumberField("Первый взнос", down, { down = it })
        NumberField("Месячный денежный поток", income, { income = it })
    }
}

fun stockTypeLabel(t: StockType): String = when (t) {
    StockType.LONG -> "Акции (длинная)"
    StockType.SHORT -> "Шорт (короткая)"
    StockType.CALL_OPTION -> "Опцион CALL"
    StockType.PUT_OPTION -> "Опцион PUT"
}
