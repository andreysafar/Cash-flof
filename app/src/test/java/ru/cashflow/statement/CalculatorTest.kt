package ru.cashflow.statement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.cashflow.statement.logic.Calculator
import ru.cashflow.statement.model.FinancialStatement
import ru.cashflow.statement.model.Property
import ru.cashflow.statement.model.StockHolding
import ru.cashflow.statement.model.StockType

class CalculatorTest {

    /** Профессия «Менеджер» из примера бланка 101. */
    private fun manager() = FinancialStatement(
        profession = "Менеджер",
        salary = 4600,
        taxes = 910,
        homePayment = 700,
        eduPayment = 60,
        carPayment = 120,
        creditCardPayment = 90,
        retailPayment = 50,
        otherExpenses = 690,
        perChildExpense = 140,
        homeMortgage = 75000,
        eduLoan = 6000,
        carLoan = 6000,
        creditCardDebt = 3000,
        retailDebt = 1000,
        cash = 1750,
    )

    @Test
    fun baseStatement_noPassiveIncome() {
        val s = manager()
        assertEquals(0L, Calculator.passiveIncome(s))
        assertEquals(4600L, Calculator.totalIncome(s))
        assertEquals(2620L, Calculator.totalExpenses(s))
        assertEquals(1980L, Calculator.monthlyCashFlow(s))
        assertFalse(Calculator.canExitRatRace(s))
    }

    @Test
    fun children_increaseExpenses() {
        val s = manager().copy(childrenCount = 2)
        assertEquals(280L, Calculator.childrenExpense(s))
        assertEquals(2900L, Calculator.totalExpenses(s))
        assertEquals(1700L, Calculator.monthlyCashFlow(s))
    }

    @Test
    fun bankLoan_payment_is_tenPercent() {
        val s = manager().copy(bankLoan = 5000)
        assertEquals(500L, Calculator.bankLoanPayment(s))
        assertEquals(3120L, Calculator.totalExpenses(s))
    }

    @Test
    fun passiveIncome_fromRealEstateAndBusiness() {
        val s = manager().copy(
            realEstate = listOf(Property(1, "Дом 2/1", 5000, 45000, 300, 40000)),
            businesses = listOf(Property(2, "Автомойка", 10000, 60000, 1500, 50000)),
            interest = 50,
        )
        assertEquals(1850L, Calculator.passiveIncome(s)) // 50 + 300 + 1500
        assertEquals(6450L, Calculator.totalIncome(s))
    }

    @Test
    fun dividends_countTowardPassiveIncome() {
        val s = manager().copy(
            stocks = listOf(
                StockHolding(1, "OK4U", 100, 10, StockType.LONG, dividendPerShare = 1),
            ),
        )
        assertEquals(100L, Calculator.dividends(s))
        assertEquals(100L, Calculator.passiveIncome(s))
    }

    @Test
    fun exitRatRace_whenPassiveExceedsExpenses() {
        val s = manager().copy(
            businesses = listOf(Property(1, "Сеть", 0, 0, 3000, 0)),
        )
        assertTrue(Calculator.passiveIncome(s) > Calculator.totalExpenses(s))
        assertTrue(Calculator.canExitRatRace(s))
    }

    @Test
    fun fastTrack_startIncome_isRoundedPassiveTimes100() {
        assertEquals(2000L, Calculator.roundToThousand(1850L))
        val s = manager().copy(fastTrackStartIncome = 200000)
        assertEquals(200000L, Calculator.fastTrackCashflowDay(s))
        assertEquals(250000L, Calculator.fastTrackWinTarget(s))
    }

    @Test
    fun options_and_short_payoffs() {
        val call = StockHolding(1, "MYT4U", 500, 5, StockType.CALL_OPTION, strikePrice = 20)
        assertEquals(5000L, Calculator.callOptionPayoff(call, 30L)) // (30-20)*500
        assertEquals(0L, Calculator.callOptionPayoff(call, 15L)) // out of the money

        val put = StockHolding(2, "MYT4U", 500, 5, StockType.PUT_OPTION, strikePrice = 20)
        assertEquals(5000L, Calculator.putOptionPayoff(put, 10L)) // (20-10)*500
        assertEquals(0L, Calculator.putOptionPayoff(put, 25L))

        val short = StockHolding(3, "GRO4U", 100, 30, StockType.SHORT)
        assertEquals(2000L, Calculator.shortGainLoss(short, 10L)) // 100*30 - 100*10
        assertEquals(-1000L, Calculator.shortGainLoss(short, 40L))
    }
}
