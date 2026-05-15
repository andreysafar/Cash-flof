package ru.cashflow.statement.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import ru.cashflow.statement.data.StateRepository
import ru.cashflow.statement.logic.Calculator
import ru.cashflow.statement.model.FastTrackBusiness
import ru.cashflow.statement.model.FinancialStatement
import ru.cashflow.statement.model.Property
import ru.cashflow.statement.model.StockHolding
import ru.cashflow.statement.model.StockType
import ru.cashflow.statement.model.Transaction

enum class DebtKind { HOME, EDU, CAR, CREDIT_CARD, RETAIL }

class StatementViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = StateRepository(app)

    var state: FinancialStatement by mutableStateOf(repo.load())
        private set

    private val maxHistory = 100

    /**
     * Центральная точка изменения состояния: снимок для отмены, запись в журнал,
     * автосохранение. Любое действие проходит через неё.
     */
    fun edit(title: String, amount: Long = 0, transform: (FinancialStatement) -> FinancialStatement) {
        val before = state
        val snapshot = repo.encode(before)
        val changed = transform(before)
        val tx = Transaction(
            id = changed.nextId,
            timestamp = System.currentTimeMillis(),
            title = title,
            amount = amount,
            snapshotJson = snapshot,
        )
        val next = changed.copy(
            history = (changed.history + tx).takeLast(maxHistory),
            nextId = changed.nextId + 1,
        )
        state = next
        repo.save(next)
    }

    fun undo() {
        val last = state.history.lastOrNull() ?: return
        val restored = repo.decode(last.snapshotJson)
            .copy(history = state.history.dropLast(1))
        state = restored
        repo.save(restored)
    }

    fun resetAll() {
        val fresh = FinancialStatement()
        state = fresh
        repo.save(fresh)
    }

    // --- Доходы / получка ---

    fun payday() {
        val cf = Calculator.monthlyCashFlow(state)
        edit("День выплат (получка)", cf) { it.copy(cash = it.cash + cf) }
    }

    // --- Акции / ценные бумаги ---

    fun buyStock(
        symbol: String,
        shares: Long,
        pricePerShare: Long,
        type: StockType,
        dividendPerShare: Long = 0,
        strikePrice: Long = 0,
    ) {
        val total = shares * pricePerShare
        val cashDelta = if (type == StockType.SHORT) total else -total
        edit("Покупка: $symbol ×$shares", cashDelta) {
            it.copy(
                cash = it.cash + cashDelta,
                stocks = it.stocks + StockHolding(
                    id = it.nextId,
                    symbol = symbol,
                    shares = shares,
                    pricePerShare = pricePerShare,
                    type = type,
                    dividendPerShare = dividendPerShare,
                    strikePrice = strikePrice,
                    turnsLeft = if (type == StockType.CALL_OPTION || type == StockType.PUT_OPTION) 3 else 0,
                ),
            )
        }
    }

    /** Закрытие позиции по текущей цене (продажа/выкуп/исполнение опциона). */
    fun closeStock(id: Long, currentPrice: Long) {
        val h = state.stocks.firstOrNull { it.id == id } ?: return
        val delta = when (h.type) {
            StockType.LONG -> h.shares * currentPrice
            StockType.SHORT -> -(h.shares * currentPrice)
            StockType.CALL_OPTION -> Calculator.callOptionPayoff(h, currentPrice)
            StockType.PUT_OPTION -> Calculator.putOptionPayoff(h, currentPrice)
        }
        edit("Закрытие: ${h.symbol}", delta) {
            it.copy(cash = it.cash + delta, stocks = it.stocks.filterNot { s -> s.id == id })
        }
    }

    fun splitStock(id: Long, doubleShares: Boolean) {
        val h = state.stocks.firstOrNull { it.id == id } ?: return
        val newShares = if (doubleShares) h.shares * 2 else h.shares / 2
        edit("${if (doubleShares) "Сплит ×2" else "Обратный сплит ÷2"}: ${h.symbol}") {
            it.copy(stocks = it.stocks.map { s -> if (s.id == id) s.copy(shares = newShares) else s })
        }
    }

    fun tickOptionTurns() {
        if (state.stocks.none { it.turnsLeft > 0 }) return
        edit("Ход: счётчик опционов −1") {
            it.copy(stocks = it.stocks.map { s ->
                if (s.turnsLeft > 0) s.copy(turnsLeft = s.turnsLeft - 1) else s
            })
        }
    }

    // --- Недвижимость / бизнес ---

    fun buyProperty(
        isBusiness: Boolean,
        name: String,
        downPayment: Long,
        price: Long,
        cashFlow: Long,
        mortgage: Long,
    ) {
        edit("Покупка: $name", -downPayment) {
            val p = Property(it.nextId, name, downPayment, price, cashFlow, mortgage)
            if (isBusiness) it.copy(cash = it.cash - downPayment, businesses = it.businesses + p)
            else it.copy(cash = it.cash - downPayment, realEstate = it.realEstate + p)
        }
    }

    fun sellProperty(isBusiness: Boolean, id: Long, salePrice: Long) {
        val list = if (isBusiness) state.businesses else state.realEstate
        val p = list.firstOrNull { it.id == id } ?: return
        val delta = salePrice - p.mortgage
        edit("Продажа: ${p.name}", delta) {
            if (isBusiness) it.copy(cash = it.cash + delta, businesses = it.businesses.filterNot { x -> x.id == id })
            else it.copy(cash = it.cash + delta, realEstate = it.realEstate.filterNot { x -> x.id == id })
        }
    }

    // --- Кредиты ---

    fun takeBankLoan(amount: Long) {
        edit("Кредит банка +$amount", amount) {
            it.copy(cash = it.cash + amount, bankLoan = it.bankLoan + amount)
        }
    }

    fun repayBankLoan(amount: Long) {
        val pay = minOf(amount, state.bankLoan)
        edit("Погашение кредита банка −$pay", -pay) {
            it.copy(cash = it.cash - pay, bankLoan = it.bankLoan - pay)
        }
    }

    /** Полное погашение долга из профессии (частично гасить нельзя по правилам). */
    fun repayDebt(kind: DebtKind) {
        edit("Погашение долга") {
            when (kind) {
                DebtKind.HOME -> it.copy(cash = it.cash - it.homeMortgage, homeMortgage = 0, homePayment = 0)
                DebtKind.EDU -> it.copy(cash = it.cash - it.eduLoan, eduLoan = 0, eduPayment = 0)
                DebtKind.CAR -> it.copy(cash = it.cash - it.carLoan, carLoan = 0, carPayment = 0)
                DebtKind.CREDIT_CARD -> it.copy(cash = it.cash - it.creditCardDebt, creditCardDebt = 0, creditCardPayment = 0)
                DebtKind.RETAIL -> it.copy(cash = it.cash - it.retailDebt, retailDebt = 0, retailPayment = 0)
            }
        }
    }

    // --- Всякая всячина / события ---

    fun doodad(amount: Long) {
        edit("Всякая всячина / расход", -amount) { it.copy(cash = it.cash - amount) }
    }

    fun addChild() {
        if (state.childrenCount >= 3) return
        edit("Прибавление в семье") { it.copy(childrenCount = it.childrenCount + 1) }
    }

    fun adjustChildren(delta: Int) {
        edit("Корректировка: дети") {
            it.copy(childrenCount = (it.childrenCount + delta).coerceIn(0, 3))
        }
    }

    fun charity() {
        val pay = Calculator.totalIncome(state) / 10
        edit("Благотворительность (10% дохода)", -pay) {
            it.copy(cash = it.cash - pay, charityTurnsLeft = 3)
        }
    }

    fun downsized() {
        val pay = Calculator.totalExpenses(state)
        edit("Увольнение (общий расход)", -pay) {
            it.copy(cash = it.cash - pay, skipTurns = 2, charityTurnsLeft = 0)
        }
    }

    fun setCash(value: Long) {
        edit("Корректировка наличных", value - state.cash) { it.copy(cash = value) }
    }

    // --- Скоростная дорожка ---

    fun exitToFastTrack() {
        if (!Calculator.canExitRatRace(state)) return
        val start = Calculator.roundToThousand(Calculator.passiveIncome(state)) * 100
        edit("Выход на скоростную дорожку") {
            it.copy(onFastTrack = true, fastTrackStartIncome = start)
        }
    }

    fun fastTrackPayday() {
        val income = Calculator.fastTrackCashflowDay(state)
        edit("ДЕНЬ CASHFLOW", income) { it.copy(cash = it.cash + income) }
    }

    fun buyFastTrackBusiness(name: String, downPayment: Long, monthlyIncome: Long) {
        edit("Бизнес (скор. дорожка): $name", -downPayment) {
            it.copy(
                cash = it.cash - downPayment,
                fastTrackBusinesses = it.fastTrackBusinesses +
                    FastTrackBusiness(it.nextId, name, monthlyIncome),
            )
        }
    }

    fun removeFastTrackBusiness(id: Long) {
        edit("Удаление бизнеса (скор. дорожка)") {
            it.copy(fastTrackBusinesses = it.fastTrackBusinesses.filterNot { b -> b.id == id })
        }
    }

    fun fastTrackPayHalfCash(reason: String) {
        val pay = state.cash / 2
        edit("$reason (½ наличных)", -pay) { it.copy(cash = it.cash - pay) }
    }

    fun fastTrackPayAllCash(reason: String) {
        edit("$reason (все наличные)", -state.cash) { it.copy(cash = 0) }
    }
}
