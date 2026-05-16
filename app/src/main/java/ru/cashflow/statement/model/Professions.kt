package ru.cashflow.statement.model

/**
 * Карточка профессии «Денежный поток 101» (значения перенесены 1-в-1
 * с официальных карточек, см. docs/professions/ и docs/RULES_MAPPING.md).
 * Денежный поток = Заработок − сумма всех расходов (детские расходы
 * добавляются только при появлении детей).
 */
data class Profession(
    val title: String,
    val salary: Long,
    val taxes: Long,
    val homePayment: Long,
    val eduPayment: Long,
    val carPayment: Long,
    val creditCardPayment: Long,
    val retailPayment: Long,
    val otherExpenses: Long,
    val perChildExpense: Long,
    val savings: Long,
    val homeMortgage: Long,
    val eduLoan: Long,
    val carLoan: Long,
    val creditCardDebt: Long,
    val retailDebt: Long,
)

/** Преобразует карточку профессии в чистый стартовый финансовый отчёт. */
fun Profession.toStatement(): FinancialStatement = FinancialStatement(
    profession = title,
    salary = salary,
    taxes = taxes,
    homePayment = homePayment,
    eduPayment = eduPayment,
    carPayment = carPayment,
    creditCardPayment = creditCardPayment,
    retailPayment = retailPayment,
    otherExpenses = otherExpenses,
    perChildExpense = perChildExpense,
    cash = savings,
    homeMortgage = homeMortgage,
    eduLoan = eduLoan,
    carLoan = carLoan,
    creditCardDebt = creditCardDebt,
    retailDebt = retailDebt,
)

/** 8 преднастроенных профессий из коробки игры. */
val PRESET_PROFESSIONS: List<Profession> = listOf(
    Profession(
        title = "Швейцар",
        salary = 1600, taxes = 280, homePayment = 200, eduPayment = 0,
        carPayment = 60, creditCardPayment = 60, retailPayment = 50,
        otherExpenses = 300, perChildExpense = 70, savings = 400,
        homeMortgage = 0, eduLoan = 0, carLoan = 4000,
        creditCardDebt = 2000, retailDebt = 1000,
    ),
    Profession(
        title = "Секретарь",
        salary = 2500, taxes = 460, homePayment = 400, eduPayment = 0,
        carPayment = 80, creditCardPayment = 60, retailPayment = 50,
        otherExpenses = 570, perChildExpense = 140, savings = 710,
        homeMortgage = 38000, eduLoan = 0, carLoan = 4000,
        creditCardDebt = 2000, retailDebt = 1000,
    ),
    Profession(
        title = "Офицер полиции",
        salary = 3000, taxes = 580, homePayment = 400, eduPayment = 0,
        carPayment = 100, creditCardPayment = 60, retailPayment = 50,
        otherExpenses = 690, perChildExpense = 160, savings = 520,
        homeMortgage = 46000, eduLoan = 0, carLoan = 5000,
        creditCardDebt = 2000, retailDebt = 1000,
    ),
    Profession(
        title = "Учитель",
        salary = 3300, taxes = 630, homePayment = 500, eduPayment = 60,
        carPayment = 100, creditCardPayment = 90, retailPayment = 50,
        otherExpenses = 760, perChildExpense = 180, savings = 400,
        homeMortgage = 50000, eduLoan = 12000, carLoan = 5000,
        creditCardDebt = 3000, retailDebt = 1000,
    ),
    Profession(
        title = "Медсестра",
        salary = 3100, taxes = 600, homePayment = 400, eduPayment = 30,
        carPayment = 100, creditCardPayment = 90, retailPayment = 50,
        otherExpenses = 710, perChildExpense = 170, savings = 480,
        homeMortgage = 47000, eduLoan = 6000, carLoan = 5000,
        creditCardDebt = 3000, retailDebt = 1000,
    ),
    Profession(
        title = "Конструктор",
        salary = 4900, taxes = 1050, homePayment = 700, eduPayment = 60,
        carPayment = 140, creditCardPayment = 120, retailPayment = 50,
        otherExpenses = 1090, perChildExpense = 250, savings = 400,
        homeMortgage = 75000, eduLoan = 12000, carLoan = 7000,
        creditCardDebt = 4000, retailDebt = 1000,
    ),
    Profession(
        title = "Адвокат",
        salary = 7500, taxes = 1830, homePayment = 1100, eduPayment = 390,
        carPayment = 220, creditCardPayment = 180, retailPayment = 50,
        otherExpenses = 1650, perChildExpense = 640, savings = 400,
        homeMortgage = 115000, eduLoan = 78000, carLoan = 11000,
        creditCardDebt = 6000, retailDebt = 1000,
    ),
    Profession(
        title = "Врач",
        salary = 13200, taxes = 3420, homePayment = 1900, eduPayment = 750,
        carPayment = 380, creditCardPayment = 270, retailPayment = 50,
        otherExpenses = 2880, perChildExpense = 640, savings = 400,
        homeMortgage = 202000, eduLoan = 150000, carLoan = 19000,
        creditCardDebt = 9000, retailDebt = 1000,
    ),
)
