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
        if (file.exists()) json.decodeFromString<FinancialStatement>(file.readText())
        else FinancialStatement()
    }.getOrDefault(FinancialStatement())

    fun save(state: FinancialStatement) {
        runCatching { file.writeText(json.encodeToString(state)) }
    }

    fun encode(state: FinancialStatement): String = json.encodeToString(state)

    fun decode(text: String): FinancialStatement = json.decodeFromString<FinancialStatement>(text)

    companion object {
        const val FILE_NAME = "cashflow_statement.json"
    }
}
