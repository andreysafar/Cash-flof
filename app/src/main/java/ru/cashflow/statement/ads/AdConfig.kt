package ru.cashflow.statement.ads

import android.content.Context

/**
 * Конфигурация рекламы VK Рекламы (VK Ad SDK / myTarget).
 *
 * Три формата:
 *  - баннер 320×50 — постоянно внизу экрана ([BANNER_SLOT_ID]);
 *  - нативный — всплывает через [NATIVE_AD_DELAY_MS] после хода ([NATIVE_SLOT_ID]);
 *  - межстраничная (interstitial) — на ключевых действиях ([INTERSTITIAL_SLOT_ID]).
 *
 * ID блоков берутся из кабинета VK Рекламы → Приложения → Рекламные блоки.
 * Тип интеграции блока должен быть «SDK» (прямая интеграция).
 */
object AdConfig {

    /** Баннер 320×50, тестовый блок от 2026-06-08. */
    const val BANNER_SLOT_ID = 2021509

    /** Нативный блок, тестовый режим от 2026-06-08. */
    const val NATIVE_SLOT_ID = 2021512

    /** Задержка перед показом нативной рекламы после хода (мс). */
    const val NATIVE_AD_DELAY_MS = 7_000L

    /** Длительность показа нативной рекламы (мс). */
    const val NATIVE_AD_DISPLAY_MS = 5_000L

    /**
     * Межстраничная реклама. Создайте блок формата Interstitial в кабинете VK
     * (тип интеграции SDK) и подставьте ID сюда. Пока 0 — interstitial отключён.
     */
    const val INTERSTITIAL_SLOT_ID = 0

    /** Минимум «моментов» между показами interstitial (смена профессии, сброс). */
    const val MIN_MOVES_BETWEEN_INTERSTITIALS = 2

    fun controller(context: Context): AdController = VkAdController(context.applicationContext)
}
