package ru.cashflow.statement.ads

import android.content.Context

/**
 * Конфигурация рекламы VK Рекламы (VK Ad SDK / myTarget).
 *
 * Активные форматы:
 *  - постоянный баннер 320×50 внизу экрана ([BANNER_SLOT_ID]);
 *  - короткий всплывающий баннер через [POPUP_DELAY_MS] после хода
 *    ([POPUP_BANNER_SLOT_ID]).
 * Отключены (заглушки): нативный ([NATIVE_SLOT_ID]) и межстраничная
 * ([INTERSTITIAL_SLOT_ID]).
 *
 * ID блоков берутся из кабинета VK Рекламы → Приложения → Рекламные блоки.
 * Тип интеграции блока должен быть «SDK» (прямая интеграция).
 */
object AdConfig {

    /** Баннер 320×50, тестовый блок от 2026-06-08. */
    const val BANNER_SLOT_ID = 2021509

    /** Нативный блок (зарезервирован для будущей нативной интеграции). */
    const val NATIVE_SLOT_ID = 2021512

    /**
     * Слот для всплывающего «короткого баннера» после хода. Используется тот же
     * виджет MyTargetView (формат баннера), поэтому здесь должен быть
     * банерный блок. По умолчанию — основной баннер; при желании создайте
     * отдельный банерный блок в кабинете VK и подставьте его ID сюда.
     */
    const val POPUP_BANNER_SLOT_ID = BANNER_SLOT_ID

    /** Через сколько после хода показать всплывающий баннер (мс). */
    const val POPUP_DELAY_MS = 6_000L

    /** Сколько секунд висит всплывающий баннер до авто-скрытия (мс). */
    const val POPUP_DISPLAY_MS = 7_000L

    /**
     * Межстраничная реклама. Создайте блок формата Interstitial в кабинете VK
     * (тип интеграции SDK) и подставьте ID сюда. Пока 0 — interstitial отключён.
     */
    const val INTERSTITIAL_SLOT_ID = 0

    /** Минимум «моментов» между показами interstitial (смена профессии, сброс). */
    const val MIN_MOVES_BETWEEN_INTERSTITIALS = 2

    fun controller(context: Context): AdController = VkAdController(context.applicationContext)
}
