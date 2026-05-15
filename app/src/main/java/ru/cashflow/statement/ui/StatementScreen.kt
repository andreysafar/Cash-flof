package ru.cashflow.statement.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.cashflow.statement.logic.Calculator

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
) {
    val s = vm.state
    var dialog by remember { mutableStateOf<Dlg?>(null) }
    var menu by remember { mutableStateOf(false) }

    val passive = Calculator.passiveIncome(s)
    val totalExpenses = Calculator.totalExpenses(s)
    val cashFlow = Calculator.monthlyCashFlow(s)
    val canExit = Calculator.canExitRatRace(s)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            s.profession.ifBlank { "Профессия не задана" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (s.playerName.isNotBlank() || s.dream.isNotBlank()) {
                            Text(
                                listOf(s.playerName, s.dream).filter { it.isNotBlank() }.joinToString(" • "),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
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
                        DropdownMenuItem(text = { Text("Журнал операций") }, onClick = { menu = false; onOpenHistory() })
                        DropdownMenuItem(text = { Text("Опционы / шорт (202)") }, onClick = { menu = false; onOpen202() })
                        DropdownMenuItem(text = { Text("Скоростная дорожка") }, onClick = { menu = false; onOpenFastTrack() })
                        DropdownMenuItem(text = { Text("Сбросить отчёт") }, onClick = { menu = false; dialog = Dlg.RESET })
                    }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()),
        ) {
            // --- Сводка: денежный поток + наличные ---
            Card(
                Modifier.fillMaxWidth().padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Месячный денежный поток (получка)", color = MaterialTheme.colorScheme.onPrimary)
                    Text(
                        money(cashFlow),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Наличные: ${money(s.cash)}", color = MaterialTheme.colorScheme.onPrimary)
                        Text("Пассивный доход: ${money(passive)}", color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Button(
                        onClick = { vm.payday() },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    ) { Text("ПОЛУЧКА  (+${money(cashFlow)})", fontWeight = FontWeight.Bold) }
                }
            }

            // --- Индикатор выхода из крысиных бегов ---
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (canExit) MaterialTheme.colorScheme.secondary
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
                            modifier = Modifier.padding(top = 8.dp),
                        ) { Text("Выйти на скоростную дорожку") }
                    }
                    if (s.onFastTrack) {
                        OutlinedButton(
                            onClick = onOpenFastTrack,
                            modifier = Modifier.padding(top = 8.dp),
                        ) { Text("Открыть скоростную дорожку") }
                    }
                }
            }

            // --- Доходы ---
            SectionCard("Доходы") {
                StatRow("Зарплата", s.salary)
                StatRow("Проценты", s.interest)
                StatRow("Дивиденды (акции)", Calculator.dividends(s))
                s.realEstate.forEach { StatRow("Недвиж.: ${it.name}", it.cashFlow) }
                s.businesses.forEach { StatRow("Бизнес: ${it.name}", it.cashFlow) }
                StatRow("Пассивный доход", passive, strong = true, highlight = true)
                StatRow("Общий доход", Calculator.totalIncome(s), strong = true, highlight = true)
            }

            // --- Расходы ---
            SectionCard("Расходы") {
                StatRow("Налоги", s.taxes)
                StatRow("Ипотека и арендная плата", s.homePayment)
                StatRow("Кредит на образование", s.eduPayment)
                StatRow("Кредит на автомобиль", s.carPayment)
                StatRow("Кредитная карточка", s.creditCardPayment)
                StatRow("Мелкие кредиты", s.retailPayment)
                StatRow("Прочие расходы", s.otherExpenses)
                StatRow("Расходы на детей (${s.childrenCount})", Calculator.childrenExpense(s))
                StatRow("Оплата кредита банка", Calculator.bankLoanPayment(s))
                StatRow("Общий расход", totalExpenses, strong = true)
            }

            // --- Активы ---
            SectionCard("Активы") {
                StatRow("Сбережения (наличные)", s.cash)
                s.stocks.forEach {
                    StatRow(
                        "${stockTypeLabel(it.type)} ${it.symbol} ×${it.shares}" +
                            if (it.turnsLeft > 0) " (кругов: ${it.turnsLeft})" else "",
                        it.shares * it.pricePerShare,
                    )
                }
                s.realEstate.forEach { StatRow("Недвиж.: ${it.name} (взнос ${money(it.downPayment)})", it.price) }
                s.businesses.forEach { StatRow("Бизнес: ${it.name} (взнос ${money(it.downPayment)})", it.price) }
            }

            // --- Пассивы ---
            SectionCard("Пассивы") {
                StatRow("Ипотека", s.homeMortgage)
                StatRow("Кредит на образование", s.eduLoan)
                StatRow("Кредит на автомобиль", s.carLoan)
                StatRow("Долг по кредитной карточке", s.creditCardDebt)
                StatRow("Мелкие кредиты", s.retailDebt)
                s.realEstate.filter { it.mortgage > 0 }.forEach { StatRow("Ипотека: ${it.name}", it.mortgage) }
                s.businesses.filter { it.mortgage > 0 }.forEach { StatRow("Пассив бизнеса: ${it.name}", it.mortgage) }
                StatRow("Кредит банка", s.bankLoan)
            }

            // --- Быстрые действия ---
            SectionCard("Быстрые действия") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionChip("Купить акции") { dialog = Dlg.BUY_STOCK }
                    ActionChip("Закрыть позицию") { dialog = Dlg.CLOSE_STOCK }
                    ActionChip("Сплит акций") { dialog = Dlg.SPLIT_STOCK }
                    ActionChip("Купить актив") { dialog = Dlg.BUY_PROPERTY }
                    ActionChip("Продать актив") { dialog = Dlg.SELL_PROPERTY }
                    ActionChip("Кредит банка") { dialog = Dlg.BANK_LOAN }
                    ActionChip("Погасить долг") { dialog = Dlg.REPAY_DEBT }
                    ActionChip("Расход") { dialog = Dlg.DOODAD }
                    ActionChip("Ребёнок") { vm.addChild() }
                    ActionChip("Благотвор.") { dialog = Dlg.CHARITY }
                    ActionChip("Увольнение") { dialog = Dlg.DOWNSIZED }
                    ActionChip("Наличные") { dialog = Dlg.SET_CASH }
                    ActionChip("Ход (опционы −1)") { vm.tickOptionTurns() }
                    ActionChip("Отмена") { vm.undo() }
                }
            }

            Box(Modifier.padding(8.dp))
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
            "Списать 10% от общего дохода (${money(Calculator.totalIncome(s) / 10)}) и получить право бросать 1–2 кубика 3 хода?",
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
            { vm.resetAll() }, { dialog = null },
        )
        null -> Unit
    }
}

@Composable
private fun ActionChip(label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.padding(vertical = 2.dp)) { Text(label) }
}
