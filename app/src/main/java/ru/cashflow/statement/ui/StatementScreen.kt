package ru.cashflow.statement.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.app.Activity
import ru.cashflow.statement.ads.LocalAdController
import ru.cashflow.statement.logic.Calculator
import ru.cashflow.statement.model.Property
import ru.cashflow.statement.model.StockHolding
import ru.cashflow.statement.model.StockType
import ru.cashflow.statement.ui.theme.Brand

private enum class Dlg {
    BUY_STOCK, CLOSE_STOCK, SPLIT_STOCK, BUY_PROPERTY, SELL_PROPERTY,
    BANK_LOAN, REPAY_DEBT, DOODAD, SET_CASH, CHARITY, DOWNSIZED, RESET
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatementScreen(
    vm: StatementViewModel,
    onOpenProfile: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpen202: () -> Unit,
    onOpenFastTrack: () -> Unit,
    onOpenHelp: () -> Unit,
) {
    val s = vm.state
    val ad = LocalAdController.current
    val activity = LocalContext.current as? Activity
    var dialog by remember { mutableStateOf<Dlg?>(null) }
    var editProp by remember { mutableStateOf<Pair<Boolean, Property>?>(null) }
    var editStock by remember { mutableStateOf<StockHolding?>(null) }
    var menu by remember { mutableStateOf(false) }

    val passive = Calculator.passiveIncome(s)
    val totalExpenses = Calculator.totalExpenses(s)
    val cashFlow = Calculator.monthlyCashFlow(s)
    val canExit = Calculator.canExitRatRace(s)
    val longStocks = s.stocks.filter { it.type == StockType.LONG }
    val has202 = s.stocks.any { it.type != StockType.LONG }
    val optionsActive = s.stocks.any { it.turnsLeft > 0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            s.profession.ifBlank { "Профессия не задана" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        val sub = listOf(s.playerName, s.dream).filter { it.isNotBlank() }
                            .joinToString(" • ").ifBlank { Brand.TAGLINE }
                        Text(sub, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Меню")
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Профессия / профиль") }, onClick = { menu = false; onOpenProfile() })
                        DropdownMenuItem(text = { Text("Подсказки правил") }, onClick = { menu = false; onOpenHelp() })
                        DropdownMenuItem(text = { Text("Журнал операций") }, onClick = { menu = false; onOpenHistory() })
                        DropdownMenuItem(text = { Text("Расширение 202 (шорт/опционы)") }, onClick = { menu = false; onOpen202() })
                        DropdownMenuItem(text = { Text("Скоростная дорожка") }, onClick = { menu = false; onOpenFastTrack() })
                        DropdownMenuItem(text = { Text("Сбросить отчёт") }, onClick = { menu = false; dialog = Dlg.RESET })
                    }
                },
            )
        },
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                // --- Сводка: денежный поток + наличные + ПОЛУЧКА (главная кнопка) ---
                Card(
                    Modifier.fillMaxWidth().padding(12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Ежемесячный денежный поток (получка)", color = MaterialTheme.colorScheme.onPrimary)
                        AnimatedMoney(
                            cashFlow,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text("Наличные", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyMedium)
                                AnimatedMoney(
                                    s.cash,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Пассивный доход", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyMedium)
                                AnimatedMoney(
                                    passive,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                        Button(
                            onClick = { vm.payday() },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).heightIn(min = 60.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary,
                            ),
                        ) {
                            Text(
                                "ПОЛУЧКА   +${money(cashFlow)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                // --- Активные эффекты: благотворительность / опционы ---
                AnimatedVisibility(
                    visible = s.charityTurnsLeft > 0 || optionsActive,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Card(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            if (s.charityTurnsLeft > 0) {
                                Text(
                                    "🎲 Благотворительность активна: бросайте 2 кубика — " +
                                        "ходов осталось ${s.charityTurnsLeft}",
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            if (optionsActive) {
                                Text("⏳ Есть опционы со сроком действия — отметьте прошедший ход.")
                            }
                            OutlinedButton(
                                onClick = { vm.nextTurn() },
                                modifier = Modifier.padding(top = 8.dp).heightIn(min = 48.dp),
                            ) { Text("Следующий ход (−1)") }
                        }
                    }
                }

                // --- Индикатор выхода из крысиных бегов ---
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (canExit) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            if (canExit) "Пассивный доход превысил расходы — можно на скоростную дорожку!"
                            else "Цель: пассивный доход > общих расходов",
                            fontWeight = FontWeight.Bold,
                        )
                        Text("Пассивный доход ${money(passive)}  /  Общий расход ${money(totalExpenses)}")
                        if (canExit && !s.onFastTrack) {
                            Button(
                                onClick = { vm.exitToFastTrack(); onOpenFastTrack() },
                                modifier = Modifier.padding(top = 8.dp).heightIn(min = 52.dp),
                                shape = RoundedCornerShape(14.dp),
                            ) { Text("Выйти на скоростную дорожку") }
                        }
                        if (s.onFastTrack) {
                            OutlinedButton(
                                onClick = onOpenFastTrack,
                                modifier = Modifier.padding(top = 8.dp).heightIn(min = 48.dp),
                            ) { Text("Открыть скоростную дорожку") }
                        }
                    }
                }

                // --- Быстрые действия (выше отчёта: их жмут чаще всего) ---
                SectionCard("Быстрые действия") {
                    ActionGrid(
                        listOf(
                            QuickAction("Купить актив", emphasized = true) { dialog = Dlg.BUY_PROPERTY },
                            QuickAction("Купить акции", emphasized = true) { dialog = Dlg.BUY_STOCK },
                            QuickAction("Расход", emphasized = true) { dialog = Dlg.DOODAD },
                            QuickAction("Продать актив", emphasized = true) { dialog = Dlg.SELL_PROPERTY },
                        ),
                    )
                    Spacer(Modifier.height(6.dp))
                    ActionGrid(
                        listOf(
                            QuickAction("Кредит банка") { dialog = Dlg.BANK_LOAN },
                            QuickAction("Погасить долг") { dialog = Dlg.REPAY_DEBT },
                            QuickAction("Продать акции") { dialog = Dlg.CLOSE_STOCK },
                            QuickAction("Сплит акций") { dialog = Dlg.SPLIT_STOCK },
                            QuickAction("Благотвор.") { dialog = Dlg.CHARITY },
                            QuickAction("Ребёнок") { vm.addChild() },
                            QuickAction("Увольнение") { dialog = Dlg.DOWNSIZED },
                            QuickAction("Наличные") { dialog = Dlg.SET_CASH },
                        ),
                    )
                    OutlinedButton(
                        onClick = { vm.undo() },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp).heightIn(min = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) { Text("Отменить последнее действие") }
                }

                // --- Доходы ---
                SectionCard("Доходы") {
                    StatRow("Заработок", s.salary)
                    StatRow("Капиталовложения / проценты", s.interest)
                    StatRow("Дивиденды (акции)", Calculator.dividends(s))
                    s.realEstate.forEach { p -> StatRow("Недвиж.: ${p.name}", p.cashFlow, onClick = { editProp = false to p }) }
                    s.businesses.forEach { p -> StatRow("Бизнес: ${p.name}", p.cashFlow, onClick = { editProp = true to p }) }
                    StatRow("Пассивный доход", passive, strong = true, highlight = true)
                    StatRow("Общий доход", Calculator.totalIncome(s), strong = true, highlight = true)
                }

                // --- Расходы ---
                SectionCard("Расходы") {
                    StatRow("Налоги", s.taxes)
                    StatRow("Оплата заклада/аренды", s.homePayment)
                    StatRow("Опл. кредита на обучение", s.eduPayment)
                    StatRow("Опл. кредита на автомобиль", s.carPayment)
                    StatRow("Оплата кредитной карточки", s.creditCardPayment)
                    StatRow("Розничные расходы", s.retailPayment)
                    StatRow("Другие расходы", s.otherExpenses)
                    StatRow("Детские расходы (${s.childrenCount})", Calculator.childrenExpense(s))
                    StatRow("Платёж по кредиту банка", Calculator.bankLoanPayment(s))
                    StatRow("Общий расход", totalExpenses, strong = true)
                }

                // --- Активы ---
                SectionCard("Активы") {
                    StatRow("Сбережения (наличные)", s.cash, onClick = { dialog = Dlg.SET_CASH })
                    longStocks.forEach { h ->
                        StatRow("Акции ${h.symbol} ×${h.shares}", h.shares * h.pricePerShare, onClick = { editStock = h })
                    }
                    s.realEstate.forEach { p ->
                        StatRow("Недвиж.: ${p.name} (взнос ${money(p.downPayment)})", p.price, onClick = { editProp = false to p })
                    }
                    s.businesses.forEach { p ->
                        StatRow("Бизнес: ${p.name} (взнос ${money(p.downPayment)})", p.price, onClick = { editProp = true to p })
                    }
                    Text(
                        "Нажмите на строку актива, чтобы изменить или удалить.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    if (has202) {
                        Text(
                            "Позиции 202 (шорт/опционы) — в меню «Расширение 202».",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // --- Пассивы ---
                SectionCard("Пассивы") {
                    StatRow("Закладная на дом", s.homeMortgage)
                    StatRow("Кредит на образование", s.eduLoan)
                    StatRow("Кредит на автомобиль", s.carLoan)
                    StatRow("По кредитным картам", s.creditCardDebt)
                    StatRow("Розничный долг", s.retailDebt)
                    s.realEstate.filter { it.mortgage > 0 }.forEach { StatRow("Ипотека: ${it.name}", it.mortgage) }
                    s.businesses.filter { it.mortgage > 0 }.forEach { StatRow("Пассив бизнеса: ${it.name}", it.mortgage) }
                    StatRow("Кредит банка", s.bankLoan)
                }

                // --- Расширение 202 ---
                SectionCard("Расширение 202") {
                    Text(
                        "Короткие продажи и опционы CALL/PUT — отдельный раздел, " +
                            "чтобы не путать с обычной игрой 101.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    ActionButton("Открыть раздел 202") { onOpen202() }
                }

                Box(Modifier.padding(10.dp))
            }

            // Крупное читаемое уведомление о действии — поверх контента, сверху.
            ActionToast(
                event = vm.lastEvent,
                onShown = { vm.consumeEvent() },
                modifier = Modifier.align(Alignment.TopCenter).padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }

    when (dialog) {
        Dlg.BUY_STOCK -> BuyStockDialog(vm) { dialog = null }
        Dlg.CLOSE_STOCK -> CloseStockDialog(vm, s) { dialog = null }
        Dlg.SPLIT_STOCK -> SplitStockDialog(vm, s) { dialog = null }
        Dlg.BUY_PROPERTY -> PropertyDialog(vm) { dialog = null }
        Dlg.SELL_PROPERTY -> SellPropertyDialog(vm, s) { dialog = null }
        Dlg.BANK_LOAN -> BankLoanDialog(vm) { dialog = null }
        Dlg.REPAY_DEBT -> RepayDebtDialog(vm, s) { dialog = null }
        Dlg.DOODAD -> AmountDialog(
            "Всякая всячина / расход",
            "Сумма списывается с наличных.",
            { vm.doodad(it) }, { dialog = null },
        )
        Dlg.SET_CASH -> SetCashDialog(vm, s) { dialog = null }
        Dlg.CHARITY -> ConfirmDialog(
            "Благотворительность",
            "Пожертвовать 10% общего дохода (${money(Calculator.charityDonation(s))})? " +
                "Взамен 3 следующих хода бросаете по 2 кубика.",
            { vm.charity() }, { dialog = null },
        )
        Dlg.DOWNSIZED -> ConfirmDialog(
            "Увольнение",
            "Списать сумму общих расходов (${money(totalExpenses)}) и пропустить 2 хода?",
            { vm.downsized() }, { dialog = null },
        )
        Dlg.RESET -> ConfirmDialog(
            "Сбросить отчёт",
            "Все данные финансового отчёта будут удалены. Продолжить?",
            { vm.resetAll(); ad.onInterstitialMoment(activity) }, { dialog = null },
        )
        null -> Unit
    }

    editProp?.let { (isBiz, p) ->
        // Берём свежую версию объекта из состояния (на случай отмены/правок).
        val current = (if (isBiz) s.businesses else s.realEstate).firstOrNull { it.id == p.id }
        if (current == null) editProp = null
        else EditPropertyDialog(vm, isBiz, current) { editProp = null }
    }

    editStock?.let { h ->
        val current = s.stocks.firstOrNull { it.id == h.id }
        if (current == null) editStock = null
        else EditStockDialog(vm, current) { editStock = null }
    }
}
