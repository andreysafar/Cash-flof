package ru.cashflow.statement.ads

/**
 * Точка конфигурации рекламы для публикации на RuStore.
 *
 * Пока подключён плейсхолдер (видимый фрейм, без сети) — приложение собирается
 * и работает офлайн. Чтобы включить реальную рекламу RuStore Ads:
 *   1. Выполните шаги из docs/RUSTORE_ADS.md (репозиторий, зависимость,
 *      разрешение INTERNET, класс RustoreAdController).
 *   2. Подставьте сюда реальные ID рекламных блоков из кабинета RuStore Ads.
 *   3. В [controller] верните RustoreAdController() для релиза.
 */
object AdConfig {

    // TODO: заменить на реальные ID блоков из кабинета RuStore Ads.
    const val BANNER_AD_UNIT_ID = "YOUR_RUSTORE_BANNER_ID"
    const val INTERSTITIAL_AD_UNIT_ID = "YOUR_RUSTORE_INTERSTITIAL_ID"

    /**
     * Активный контроллер рекламы. Сейчас — плейсхолдер (фрейм виден, сети нет).
     * После интеграции SDK замените на `RustoreAdController(...)`.
     */
    fun controller(): AdController = PlaceholderAdController
}
