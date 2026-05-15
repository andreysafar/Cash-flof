package ru.cashflow.statement.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: StatementViewModel, onBack: () -> Unit) {
    val s = vm.state
    var playerName by remember { mutableStateOf(s.playerName) }
    var profession by remember { mutableStateOf(s.profession) }
    var dream by remember { mutableStateOf(s.dream) }
    var salary by remember { mutableLongStateOf(s.salary) }
    var interest by remember { mutableLongStateOf(s.interest) }
    var taxes by remember { mutableLongStateOf(s.taxes) }
    var homePayment by remember { mutableLongStateOf(s.homePayment) }
    var eduPayment by remember { mutableLongStateOf(s.eduPayment) }
    var carPayment by remember { mutableLongStateOf(s.carPayment) }
    var creditCardPayment by remember { mutableLongStateOf(s.creditCardPayment) }
    var retailPayment by remember { mutableLongStateOf(s.retailPayment) }
    var otherExpenses by remember { mutableLongStateOf(s.otherExpenses) }
    var perChildExpense by remember { mutableLongStateOf(s.perChildExpense) }
    var homeMortgage by remember { mutableLongStateOf(s.homeMortgage) }
    var eduLoan by remember { mutableLongStateOf(s.eduLoan) }
    var carLoan by remember { mutableLongStateOf(s.carLoan) }
    var creditCardDebt by remember { mutableLongStateOf(s.creditCardDebt) }
    var retailDebt by remember { mutableLongStateOf(s.retailDebt) }
    var cash by remember { mutableLongStateOf(s.cash) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профессия / профиль") },
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
            SectionCard("Игрок") {
                TextInputField("Имя игрока", playerName, { playerName = it })
                TextInputField("Профессия", profession, { profession = it })
                TextInputField("Мечта", dream, { dream = it })
            }
            SectionCard("Доходы и расходы из карточки профессии") {
                NumberField("Зарплата", salary, { salary = it })
                NumberField("Проценты (сбережения)", interest, { interest = it })
                NumberField("Налоги", taxes, { taxes = it })
                NumberField("Выплаты по ипотеке и аренда", homePayment, { homePayment = it })
                NumberField("Выплаты по кредиту на образование", eduPayment, { eduPayment = it })
                NumberField("Выплаты по кредиту на автомобиль", carPayment, { carPayment = it })
                NumberField("Выплаты по кредитной карточке", creditCardPayment, { creditCardPayment = it })
                NumberField("Выплаты по мелким кредитам", retailPayment, { retailPayment = it })
                NumberField("Прочие расходы", otherExpenses, { otherExpenses = it })
                NumberField("Расходы на одного ребёнка", perChildExpense, { perChildExpense = it })
            }
            SectionCard("Пассивы из карточки профессии") {
                NumberField("Ипотека", homeMortgage, { homeMortgage = it })
                NumberField("Кредит на образование", eduLoan, { eduLoan = it })
                NumberField("Кредит на автомобиль", carLoan, { carLoan = it })
                NumberField("Долг по кредитной карточке", creditCardDebt, { creditCardDebt = it })
                NumberField("Мелкие кредиты", retailDebt, { retailDebt = it })
            }
            SectionCard("Стартовые наличные") {
                NumberField("Сбережения / наличные", cash, { cash = it })
            }
            Button(
                onClick = {
                    vm.edit("Сохранение профиля") {
                        it.copy(
                            playerName = playerName, profession = profession, dream = dream,
                            salary = salary, interest = interest, taxes = taxes,
                            homePayment = homePayment, eduPayment = eduPayment, carPayment = carPayment,
                            creditCardPayment = creditCardPayment, retailPayment = retailPayment,
                            otherExpenses = otherExpenses, perChildExpense = perChildExpense,
                            homeMortgage = homeMortgage, eduLoan = eduLoan, carLoan = carLoan,
                            creditCardDebt = creditCardDebt, retailDebt = retailDebt, cash = cash,
                        )
                    }
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) { Text("Сохранить профиль") }
        }
    }
}
