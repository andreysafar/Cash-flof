package ru.cashflow.statement.data

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.cashflow.statement.model.FinancialStatement
import java.io.File

/** Локальное офлайн-хранилище финансового отчёта в JSON (без сети, без БД). */
class StateRepository(context: Context) {

    private val file = File(context.filesDir, FILE_NAME)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun load(): FinancialStatement = runCatching {
        if (!file.exists()) return@runCatching FinancialStatement()
        // Защита от «раздутого» файла старых версий (баг экспоненциального
        // снимка): не читаем гигантский JSON в память — начинаем заново.
        if (file.length() > MAX_FILE_BYTES) {
            file.delete()
            return@runCatching FinancialStatement()
        }
        val st = json.decodeFromString<FinancialStatement>(file.readText())
        st.copy(history = st.history.takeLast(MAX_HISTORY))
    }.getOrDefault(FinancialStatement())

    fun save(state: FinancialStatement) {
        runCatching { file.writeText(json.encodeToString(state)) }
    }

    fun encode(state: FinancialStatement): String = json.encodeToString(state)

    fun decode(text: String): FinancialStatement = json.decodeFromString<FinancialStatement>(text)

    companion object {
        const val FILE_NAME = "cashflow_statement.json"
        const val MAX_HISTORY = 60
        private const val MAX_FILE_BYTES = 5_000_000L
    }
}
