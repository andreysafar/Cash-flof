package ru.cashflow.statement.logic

import ru.cashflow.statement.model.FinancialStatement
import ru.cashflow.statement.model.StockHolding
import ru.cashflow.statement.model.StockType

/**
 * Единственный источник правил расчёта по бланку «Денежный поток 101/202».
 * Никакая бизнес-логика расчётов не должна дублироваться вне этого объекта.
 */
object Calculator {

    /** Дивиденды: сумма по дивидендным длинным позициям. */
    fun dividends(s: FinancialStatement): Long =
        s.stocks.filter { it.type == StockType.LONG }
            .sumOf { it.shares * it.dividendPerShare }

    fun realEstateIncome(s: FinancialStatement): Long = s.realEstate.sumOf { it.cashFlow }

    fun businessIncome(s: FinancialStatement): Long = s.businesses.sumOf { it.cashFlow }

    /** Пассивный доход = проценты + дивиденды + аренда недвижимости + доход бизнеса. */
    fun passiveIncome(s: FinancialStatement): Long =
        s.interest + dividends(s) + realEstateIncome(s) + businessIncome(s)

    /** Общий доход = зарплата + пассивный доход. */
    fun totalIncome(s: FinancialStatement): Long = s.salary + passiveIncome(s)

    /** Расходы на детей = количество детей × расход на одного ребёнка. */
    fun childrenExpense(s: FinancialStatement): Long = s.childrenCount * s.perChildExpense

    /** Платёж по кредиту банка = 10% в месяц = $100 на каждые $1000. */
    fun bankLoanPayment(s: FinancialStatement): Long = s.bankLoan / 10

    fun totalExpenses(s: FinancialStatement): Long =
        s.taxes + s.homePayment + s.eduPayment + s.carPayment +
            s.creditCardPayment + s.retailPayment + s.otherExpenses +
            childrenExpense(s) + bankLoanPayment(s)

    /** Месячный денежный поток («День выплат» / получка). */
    fun monthlyCashFlow(s: FinancialStatement): Long = totalIncome(s) - totalExpenses(s)

    /**
     * Пожертвование на благотворительность = 10% общего дохода
     * (по правилам РФ-издания, округление до ближайшего доллара).
     * Взамен — 3 следующих хода игрок бросает по две кости.
     */
    fun charityDonation(s: FinancialStatement): Long = (totalIncome(s) + 5) / 10

    /** Условие выхода из крысиных бегов: пассивный доход превышает общий расход. */
    fun canExitRatRace(s: FinancialStatement): Boolean = passiveIncome(s) > totalExpenses(s)

    // --- Скоростная дорожка ---

    fun roundToThousand(v: Long): Long = ((v + 500) / 1000) * 1000

    /** Доход в «ДЕНЬ CASHFLOW» = начальный доход + доход бизнесов скоростной дорожки. */
    fun fastTrackCashflowDay(s: FinancialStatement): Long =
        s.fastTrackStartIncome + s.fastTrackBusinesses.sumOf { it.monthlyIncome }

    /** Цель победы: увеличить доход в «ДЕНЬ CASHFLOW» на $50 000. */
    fun fastTrackWinTarget(s: FinancialStatement): Long = s.fastTrackStartIncome + 50_000

    // --- Расширения 202 ---

    /** Шорт: итог = сумма продажи − сумма выкупа. */
    fun shortGainLoss(h: StockHolding, buybackPricePerShare: Long): Long =
        h.shares * h.pricePerShare - h.shares * buybackPricePerShare

    /** Опцион CALL: к получению = max(0, новая цена − strike) × кол-во. */
    fun callOptionPayoff(h: StockHolding, currentPrice: Long): Long =
        maxOf(0L, currentPrice - h.strikePrice) * h.shares

    /** Опцион PUT: к получению = max(0, strike − новая цена) × кол-во. */
    fun putOptionPayoff(h: StockHolding, currentPrice: Long): Long =
        maxOf(0L, h.strikePrice - currentPrice) * h.shares
}
