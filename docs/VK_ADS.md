# Реклама VK Рекламы (VK Ad SDK)

## MVP: только баннер

Для MVP включён **один формат** — баннер 320×50 внизу экрана. Это осознанное
решение по юзабилити: нативный оверлей всплывал после **каждого** хода (а за
партию их десятки) и сильно мешал игре, а interstitial требует отдельного блока.
Баннер приносит доход постоянно и почти не мешает. Реализация —
`VkAdController.kt` (минимум API: `MyTargetView.setSlotId/load/destroy`, без
слушателей с версионно-зависимыми сигнатурами).

| Формат | Статус | Конфиг |
|--------|--------|--------|
| **Баннер 320×50** | ✅ включён | `BANNER_SLOT_ID` = 2021509 |
| **Нативный** | ⏸ отключён в MVP | `NATIVE_SLOT_ID` = 2021512 |
| **Межстраничная (Interstitial)** | ⏸ отключён | `INTERSTITIAL_SLOT_ID` (0) |

### Как вернуть нативную / interstitial рекламу

Точки расширения сохранены в интерфейсе `AdController`
(`onMoveCompleted`, `NativeAdOverlay`, `onInterstitialMoment`) — сейчас в
`VkAdController` это пустые реализации. Чтобы вернуть форматы:

1. Реализуйте слушатели myTarget **точно по сигнатурам вашей версии SDK**
   (5.45.3). Прошлая версия падала из-за неверных сигнатур: у
   `InterstitialAd.InterstitialAdListener` появился `onFailedToShow`, а у
   нативного слушателя — другой набор методов. Сверяйтесь с автодополнением IDE
   после Gradle Sync.
2. Для нативного оверлея делайте локальную копию из `MutableState`
   (`val ad = activeNativeAd.value ?: return`) — иначе smart-cast по
   delegated-property не работает.
3. Дёргайте `onMoveCompleted` из `CashflowApp` (уже подключено) и `NativeAdOverlay`
   из `MainActivity` (уже подключено).

## Что уже сделано в коде

- Зависимость `com.my.target:mytarget-sdk` в `app/build.gradle.kts`
- Разрешения `INTERNET`, `ACCESS_NETWORK_STATE` в манифесте
- `MyTargetActivity` в манифесте
- ProGuard-правила для SDK
- Баннер: slot **2021509**
- Нативный: slot **2021512**, задержка **7 с**, показ **5 с**
- Interstitial: **отключён** (`INTERSTITIAL_SLOT_ID = 0`) — нужен отдельный блок в кабинете

## Что сделать вам

### 1. Кабинет VK Рекламы

1. **Приложение** добавлено с ссылкой на RuStore и `applicationId` = `ru.cashflow.statement`.
2. **Баннер 320×50, SDK** — блок `2021509` ✓
3. **Нативный, SDK** — блок `2021512` ✓
4. **(Опционально) Interstitial, SDK** — создайте блок и подставьте ID в `AdConfig.kt`:

```kotlin
const val INTERSTITIAL_SLOT_ID = ВАШ_ID_БЛОКА
```

5. Дождитесь модерации приложения (для начисления дохода) и заполните платёжные реквизиты.

> Тип интеграции блока и SDK в коде должны совпадать: блок SDK не работает с in-app bidding и наоборот.

### 2. Сборка в Android Studio

1. `File → Sync Project with Gradle Files`
2. `Build → Generate Signed Bundle / APK` → **Android App Bundle** (для RuStore)
3. Версия релиза: `versionCode = 2`, `versionName = "1.1.0"` (уже в `app/build.gradle.kts`)

### 3. Тестирование рекламы

- Запускайте на **реальном устройстве** с интернетом.
- В debug-сборке включён `MyTargetManager.setDebugMode(true)` — смотрите Logcat с тегами myTarget.
- Тестовый режим блока показывает тестовые креативы.
- Баннер — внизу экрана сразу после загрузки.
- Нативный — сделайте любой ход (кнопка на главном экране), через **7 секунд** всплывёт карточка на ~5 секунд.
- Interstitial — после **двух** событий (смена профессии или сброс отчёта), если `INTERSTITIAL_SLOT_ID` задан.

### Настройка таймингов нативной рекламы

В `AdConfig.kt`:

```kotlin
const val NATIVE_AD_DELAY_MS = 7_000L   // задержка после хода
const val NATIVE_AD_DISPLAY_MS = 5_000L // длительность показа
```

Если игрок делает новый ход до истечения 7 с, таймер сбрасывается (показ только после последнего хода).

### 4. Политика конфиденциальности

Обновите `docs/PRIVACY.md` и опубликуйте по URL в карточке RuStore — приложение теперь использует сеть для рекламы.

### 5. Публикация в RuStore

См. `docs/RUSTORE_PUBLISH.md`. Загрузите новый AAB с версией 2.

## Настройка частоты interstitial

В `AdConfig.kt`:

```kotlin
const val MIN_MOVES_BETWEEN_INTERSTITIALS = 2
```

Счётчик увеличивается при каждом вызове `onInterstitialMoment` (смена профессии, сброс отчёта). Показ — только когда счётчик достиг порога и объявление предзагружено.

## Если реклама не грузится

1. Проверьте интернет на устройстве.
2. Убедитесь, что `applicationId` в кабинете = `ru.cashflow.statement`.
3. Статус приложения в кабинете (модерация).
4. Logcat: ошибки `onNoAd` / `IAdLoadingError`.
5. Тип блока = SDK, формат совпадает (320×50 / Native / Interstitial).

## Ссылки

- [Интеграция Android](https://ads.vk.com/help/partner/partner_integration/partner_android/partner_android_integration)
- [Баннер Android](https://ads.vk.ru/help/partner/partner_integration/partner_android/partner_android_banner)
- [Нативный блок Android](https://ads.vk.ru/help/partner/partner_integration/partner_android/partner_android_native)
- [Создание площадки для приложений](https://ads.vk.ru/help/articles/partner_create_app)
