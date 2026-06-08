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
import ru.cashflow.statement.model.Property
import ru.cashflow.statement.model.StockHolding
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
private fun Hint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

/** Покупка обычных акций — игра 101 (только длинная позиция). */
@Composable
fun BuyStockDialog(vm: StatementViewModel, onClose: () -> Unit) {
    var symbol by remember { mutableStateOf("") }
    var shares by remember { mutableStateOf(0L) }
    var price by remember { mutableStateOf(0L) }
    var dividend by remember { mutableStateOf(0L) }

    DialogShell(
        title = "Покупка акций (101)",
        confirmEnabled = symbol.isNotBlank() && shares > 0 && price > 0,
        onConfirm = { vm.buyStock(symbol.trim(), shares, price, StockType.LONG, dividend); onClose() },
        onDismiss = onClose,
    ) {
        Hint("Со счёта спишется количество × цена. Дивиденд/процент идёт в пассивный доход. " +
            "Шорт и опционы — в разделе «Расширение 202».")
        TextInputField("Символ акции", symbol, { symbol = it })
        NumberField("Количество", shares, { shares = it })
        NumberField("Цена за акцию", price, { price = it })
        NumberField("Дивиденд на акцию (мес., если есть)", dividend, { dividend = it })
    }
}

/** Открытие позиции расширения 202: шорт / опцион CALL / опцион PUT. */
@Composable
fun Open202PositionDialog(vm: StatementViewModel, onClose: () -> Unit) {
    var symbol by remember { mutableStateOf("") }
    var shares by remember { mutableStateOf(0L) }
    var price by remember { mutableStateOf(0L) }
    var strike by remember { mutableStateOf(0L) }
    var type by remember { mutableStateOf(StockType.SHORT) }

    val isOption = type == StockType.CALL_OPTION || type == StockType.PUT_OPTION

    DialogShell(
        title = "Позиция 202",
        confirmEnabled = symbol.isNotBlank() && shares > 0 && price > 0 &&
            (!isOption || strike > 0),
        onConfirm = { vm.buyStock(symbol.trim(), shares, price, type, 0, strike); onClose() },
        onDismiss = onClose,
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(StockType.SHORT, StockType.CALL_OPTION, StockType.PUT_OPTION).forEach { t ->
                FilterChip(
                    selected = type == t,
                    onClick = { type = t },
                    label = { Text(stockTypeLabel(t)) },
                )
            }
        }
        when (type) {
            StockType.SHORT -> Hint("Короткая продажа: на счёт придёт количество × цена продажи. " +
                "Позже откупите по новой цене в разделе 202.")
            StockType.CALL_OPTION -> Hint("CALL — ставка на рост. Премия = цена опциона × количество " +
                "спишется со счёта. Действует 3 хода.")
            StockType.PUT_OPTION -> Hint("PUT — ставка на падение. Премия = цена опциона × количество " +
                "спишется со счёта. Действует 3 хода.")
            else -> Unit
        }
        TextInputField("Символ", symbol, { symbol = it })
        NumberField("Количество акций", shares, { shares = it })
        NumberField(
            if (isOption) "Цена опциона за акцию (премия)" else "Цена продажи за акцию",
            price, { price = it },
        )
        if (isOption) {
            NumberField("Объявленная цена (strike)", strike, { strike = it })
        }
    }
}

/** Продажа акций 101 — можно продать часть позиции. */
@Composable
fun CloseStockDialog(vm: StatementViewModel, state: FinancialStatement, onClose: () -> Unit) {
    val longs = state.stocks.filter { it.type == StockType.LONG }
    if (longs.isEmpty()) {
        DialogShell("Продажа акций", false, onClose, onClose) {
            Text("Нет акций. Позиции 202 (шорт/опционы) закрываются в разделе «Расширение 202».")
        }
        return
    }
    var selected by remember { mutableStateOf(longs.first().id) }
    var qty by remember { mutableStateOf(0L) }
    var sellPrice by remember { mutableStateOf(0L) }
    val h = longs.firstOrNull { it.id == selected } ?: longs.first()

    DialogShell(
        title = "Продажа акций",
        confirmEnabled = sellPrice > 0,
        onConfirm = { vm.closeStock(selected, sellPrice, qty); onClose() },
        onDismiss = onClose,
    ) {
        Hint("Можно продать часть. Поле «Сколько продать» = 0 → продать всю позицию.")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            longs.forEach { s ->
                FilterChip(
                    selected = selected == s.id,
                    onClick = { selected = s.id; qty = 0 },
                    label = { Text("${s.symbol} ×${s.shares}") },
                )
            }
        }
        Text(
            "В позиции: ${h.shares} акц. по ${money(h.pricePerShare)}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 4.dp),
        )
        NumberField("Сколько продать (0 = все ${h.shares})", qty, { qty = it })
        NumberField("Цена продажи за акцию", sellPrice, { sellPrice = it })
    }
}

@Composable
fun SplitStockDialog(vm: StatementViewModel, state: FinancialStatement, onClose: () -> Unit) {
    val longs = state.stocks.filter { it.type == StockType.LONG }
    if (longs.isEmpty()) {
        DialogShell("Сплит акций", false, onClose, onClose) { Text("Нет акций.") }
        return
    }
    var selected by remember { mutableStateOf(longs.first().id) }
    DialogShell(
        title = "Сплит / обратный сплит",
        onConfirm = onClose,
        onDismiss = onClose,
    ) {
        Hint("Сплит 2:1 удваивает число акций, обратный 1:2 — уменьшает вдвое.")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            longs.forEach { s ->
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
        Hint("Первый взнос спишется с наличных. Денежный поток объекта идёт в пассивный доход.")
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
        Hint("Ипотека по объекту: ${money(p.mortgage)}. На счёт придёт: Цена продажи − Ипотека.")
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
        Hint("Кредит кратен \$1000. Платёж = 10% в месяц (\$100 на каждые \$1000) и входит в общий расход.")
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
        Triple("Закладная на дом", DebtKind.HOME, state.homeMortgage),
        Triple("Кредит на образование", DebtKind.EDU, state.eduLoan),
        Triple("Кредит на автомобиль", DebtKind.CAR, state.carLoan),
        Triple("По кредитным картам", DebtKind.CREDIT_CARD, state.creditCardDebt),
        Triple("Розничный долг", DebtKind.RETAIL, state.retailDebt),
    ).filter { it.third > 0 }
    if (items.isEmpty()) {
        DialogShell("Погашение долга", false, onClose, onClose) { Text("Нет долгов из профессии.") }
        return
    }
    DialogShell(title = "Полное погашение долга", onConfirm = onClose, onDismiss = onClose) {
        Hint("Долги из профессии гасятся только полностью. После погашения исчезает и платёж.")
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
        if (hint.isNotBlank()) Hint(hint)
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
        Hint("Текущие наличные: ${money(state.cash)}. Укажите фактическое значение для сверки за столом.")
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

/** Изменение существующего объекта (недвижимость/бизнес) без движения денег. */
@Composable
fun EditPropertyDialog(
    vm: StatementViewModel,
    isBusiness: Boolean,
    property: Property,
    onClose: () -> Unit,
) {
    var name by remember { mutableStateOf(property.name) }
    var down by remember { mutableStateOf(property.downPayment) }
    var price by remember { mutableStateOf(property.price) }
    var flow by remember { mutableStateOf(property.cashFlow) }
    var mortgage by remember { mutableStateOf(property.mortgage) }

    DialogShell(
        title = if (isBusiness) "Изменить бизнес" else "Изменить недвижимость",
        confirmEnabled = name.isNotBlank(),
        onConfirm = { vm.editProperty(isBusiness, property.id, name.trim(), down, price, flow, mortgage); onClose() },
        onDismiss = onClose,
    ) {
        Hint("Правка значений не двигает наличные — это исправление отчёта. " +
            "Чтобы продать с зачислением денег, используйте «Продать актив».")
        TextInputField("Название", name, { name = it })
        NumberField("Первый взнос", down, { down = it })
        NumberField("Цена", price, { price = it })
        NumberField("Денежный поток (в месяц)", flow, { flow = it })
        NumberField("Ипотека / пассив по объекту", mortgage, { mortgage = it })
        TextButton(
            onClick = { vm.removeProperty(isBusiness, property.id); onClose() },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        ) { Text("Удалить из отчёта (без денег)") }
    }
}

/** Изменение существующей позиции по ценным бумагам без движения денег. */
@Composable
fun EditStockDialog(vm: StatementViewModel, holding: StockHolding, onClose: () -> Unit) {
    var symbol by remember { mutableStateOf(holding.symbol) }
    var shares by remember { mutableStateOf(holding.shares) }
    var price by remember { mutableStateOf(holding.pricePerShare) }
    var dividend by remember { mutableStateOf(holding.dividendPerShare) }
    var strike by remember { mutableStateOf(holding.strikePrice) }
    val isOption = holding.type == StockType.CALL_OPTION || holding.type == StockType.PUT_OPTION

    DialogShell(
        title = "Изменить: ${stockTypeLabel(holding.type)}",
        confirmEnabled = symbol.isNotBlank() && shares > 0 && price > 0,
        onConfirm = { vm.editStock(holding.id, symbol.trim(), shares, price, dividend, strike); onClose() },
        onDismiss = onClose,
    ) {
        Hint("Правка значений не двигает наличные — это исправление отчёта.")
        TextInputField("Символ", symbol, { symbol = it })
        NumberField("Количество", shares, { shares = it })
        NumberField(if (isOption) "Премия за акцию" else "Цена за акцию", price, { price = it })
        if (holding.type == StockType.LONG) {
            NumberField("Дивиденд на акцию (мес.)", dividend, { dividend = it })
        }
        if (isOption) {
            NumberField("Объявленная цена (strike)", strike, { strike = it })
        }
        TextButton(
            onClick = { vm.removeStock(holding.id); onClose() },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        ) { Text("Удалить из отчёта (без денег)") }
    }
}

fun stockTypeLabel(t: StockType): String = when (t) {
    StockType.LONG -> "Акции"
    StockType.SHORT -> "Шорт (короткая)"
    StockType.CALL_OPTION -> "Опцион CALL"
    StockType.PUT_OPTION -> "Опцион PUT"
}
