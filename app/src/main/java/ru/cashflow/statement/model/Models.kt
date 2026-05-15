package ru.cashflow.statement.model

import kotlinx.serialization.Serializable

/**
 * Тип позиции по ценным бумагам.
 * LONG/SHORT — обычные акции (101), CALL_OPTION/PUT_OPTION — опционы (202).
 */
enum class StockType { LONG, SHORT, CALL_OPTION, PUT_OPTION }

/** Строка «Акции/Взаимные фонды/Депозиты» и расширенные позиции 202. */
@Serializable
data class StockHolding(
    val id: Long,
    val symbol: String,
    val shares: Long,
    /** LONG/SHORT — цена за акцию; CALL/PUT — стоимость опциона за акцию. */
    val pricePerShare: Long,
    val type: StockType = StockType.LONG,
    /** Дивиденд на акцию в месяц (для дивидендных акций LONG). */
    val dividendPerShare: Long = 0,
    /** Опционы: объявленная цена (strike). */
    val strikePrice: Long = 0,
    /** Опционы: счётчик кругов, срок действия 3 хода. */
    val turnsLeft: Int = 0,
)

/** Объект «Недвижимость» или «Бизнес» в балансовом отчёте. */
@Serializable
data class Property(
    val id: Long,
    val name: String,
    val downPayment: Long,
    val price: Long,
    val cashFlow: Long,
    /** Ипотека/пассив по объекту — гасится при продаже. */
    val mortgage: Long,
)

/** Бизнес, купленный на скоростной дорожке. */
@Serializable
data class FastTrackBusiness(
    val id: Long,
    val name: String,
    val monthlyIncome: Long,
)

/** Запись журнала операций; хранит снимок состояния ДО действия для отмены. */
@Serializable
data class Transaction(
    val id: Long,
    val timestamp: Long,
    val title: String,
    val amount: Long,
    val snapshotJson: String,
)

/**
 * Полный финансовый отчёт игрока по официальному бланку «Денежный поток 101»
 * (+ скоростная дорожка и расширения 202).
 */
@Serializable
data class FinancialStatement(
    val playerName: String = "",
    val profession: String = "",
    val dream: String = "",

    // Доходы
    val salary: Long = 0,
    val interest: Long = 0,

    // Расходы (постоянные / из карточки профессии)
    val taxes: Long = 0,
    val homePayment: Long = 0,
    val eduPayment: Long = 0,
    val carPayment: Long = 0,
    val creditCardPayment: Long = 0,
    val retailPayment: Long = 0,
    val otherExpenses: Long = 0,
    val perChildExpense: Long = 0,
    val childrenCount: Int = 0,

    // Пассивы (балансовый отчёт)
    val homeMortgage: Long = 0,
    val eduLoan: Long = 0,
    val carLoan: Long = 0,
    val creditCardDebt: Long = 0,
    val retailDebt: Long = 0,
    val bankLoan: Long = 0,

    // Активы
    val cash: Long = 0,
    val stocks: List<StockHolding> = emptyList(),
    val realEstate: List<Property> = emptyList(),
    val businesses: List<Property> = emptyList(),

    // Состояние игры
    val onFastTrack: Boolean = false,
    val fastTrackStartIncome: Long = 0,
    val fastTrackBusinesses: List<FastTrackBusiness> = emptyList(),
    val skipTurns: Int = 0,
    val charityTurnsLeft: Int = 0,

    val history: List<Transaction> = emptyList(),
    val nextId: Long = 1,
)
