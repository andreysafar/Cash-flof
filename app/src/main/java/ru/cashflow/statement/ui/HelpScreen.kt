package ru.cashflow.statement.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Подсказки правил") },
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
            Rule("Цель крысиных бегов",
                "Сделать так, чтобы пассивный доход стал больше общих расходов. " +
                    "Как только это произошло — нажмите «Выйти на скоростную дорожку».")

            Rule("Получка (день выплат)",
                "На клетке выплат нажмите ПОЛУЧКА: на счёт начисляется ежемесячный " +
                    "денежный поток = общий доход − общий расход.")

            Rule("Малая / большая сделка",
                "Акции — обычно малая сделка ($1–50 за акцию). Недвижимость и бизнес — " +
                    "большая сделка: вносится первый взнос наличными, объект даёт " +
                    "ежемесячный денежный поток (идёт в пассивный доход).")

            Rule("Акции и дивиденды",
                "Дивиденд/процент по акциям идёт в пассивный доход. " +
                    "Сплит 2:1 удваивает число акций, обратный 1:2 — уменьшает вдвое. " +
                    "Можно продавать акции частично — укажите количество.")

            Rule("Кредит банка",
                "Берётся в любой момент, только суммами кратно $1 000. " +
                    "Платёж — 10% в месяц ($100 на каждые $1 000) и входит в общий расход.")

            Rule("Долги из профессии",
                "Закладная на дом, кредиты на образование/авто, карты, розничный долг " +
                    "гасятся ТОЛЬКО полностью (кроме кредита банка). После погашения " +
                    "соответствующий ежемесячный платёж исчезает.")

            Rule("Ребёнок / Благотворительность / Увольнение",
                "Ребёнок (до 3): добавляет «детские расходы». Благотворительность: " +
                    "отдать 10% общего дохода, 3 хода можно бросать 1–2 кубика. " +
                    "Увольнение: оплатить общий расход и пропустить 2 хода.")

            Rule("Скоростная дорожка",
                "Доход в «ДЕНЬ CASHFLOW» = пассивный доход (округлённый до $1 000) × 100. " +
                    "Победа: купить мечту ИЛИ первым увеличить доход CASHFLOW на $50 000.")

            Rule("202 · Короткая продажа (Short Sale)",
                "Продаёте акции, которых нет: на счёт приходит количество × цена продажи. " +
                    "Позже откупаете: со счёта уходит количество × цена выкупа. " +
                    "Прибыль = (цена продажи − цена выкупа) × количество. Убыток не ограничен.")

            Rule("202 · Опцион CALL",
                "Ставка на рост. Платите премию (цена опциона × количество). " +
                    "При исполнении получаете (рыночная цена − strike) × количество, " +
                    "если рынок выше strike. Действует 3 хода, потом сгорает.")

            Rule("202 · Опцион PUT",
                "Ставка на падение. Платите премию. При исполнении получаете " +
                    "(strike − рыночная цена) × количество, если рынок ниже strike. " +
                    "Действует 3 хода (кнопка «Ход» уменьшает счётчик).")

            Rule("Ошиблись?",
                "Любое действие можно отменить кнопкой «Отмена» или в журнале операций. " +
                    "Кнопка «Наличные» — ручная корректировка счёта для сверки с банком за столом.")

            Text(
                "Числовые данные перенесены с официальных бланков 101/202 и карточек " +
                    "профессий. Спорные нюансы — в docs/RULES_MAPPING.md.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun Rule(title: String, body: String) {
    SectionCard(title) {
        Text(body, style = MaterialTheme.typography.bodyLarge)
    }
}
