# Реклама VK Рекламы (VK Ad SDK)

## Что включено

| Формат | Статус | Конфиг |
|--------|--------|--------|
| **Постоянный баннер 320×50** | ✅ внизу всех экранов | `BANNER_SLOT_ID` = 2021509 |
| **Короткий баннер после хода** | ✅ всплывает через ~6 с, висит ~7 с | `POPUP_BANNER_SLOT_ID` (= баннер) |
| **Нативный (native-формат)** | ⏸ отключён | `NATIVE_SLOT_ID` = 2021512 |
| **Межстраничная (Interstitial)** | ⏸ отключён | `INTERSTITIAL_SLOT_ID` (0) |

Оба активных формата используют один виджет `MyTargetView` (минимальный
стабильный API `setSlotId/load/destroy`, без слушателей с версионно-зависимыми
сигнатурами — именно на них раньше падала сборка).

### Короткий баннер после хода

- После любого хода (`CashflowApp` → `onMoveCompleted`) через
  `POPUP_DELAY_MS` (6 с) всплывает карточка-баннер над постоянным баннером;
  скрывается через `POPUP_DISPLAY_MS` (7 с) или по «×».
- Если игрок делает новый ход в течение задержки — таймер сбрасывается
  (показ только после паузы). Тайминги — в `AdConfig.kt`.
- Слот `POPUP_BANNER_SLOT_ID` по умолчанию равен основному баннеру. Если
  хотите отдельную статистику/частоту — создайте отдельный **банерный** блок
  (формат 320×50, тип SDK) в кабинете VK и подставьте его ID.

### Как вернуть настоящий native-формат / interstitial

Точки расширения в `AdController` сохранены. Чтобы добавить именно native-формат
(слот `NATIVE_SLOT_ID`) или interstitial:

1. Реализуйте слушатели myTarget **точно по сигнатурам вашей версии SDK**
   (5.45.3): у `InterstitialAd.InterstitialAdListener` есть `onFailedToShow`,
   у `NativeAd.NativeAdListener` — свой набор. Сверяйтесь с автодополнением IDE
   после Gradle Sync.
2. Для нативного оверлея делайте локальную копию из `MutableState`
   (`val ad = activeNativeAd.value ?: return`) — иначе smart-cast по
   delegated-property не компилируется.

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
- Тестовый режим блока показывает тестовые креативы.
- Постоянный баннер — внизу экрана сразу после загрузки.
- Короткий баннер после хода — сделайте любой ход (например, ПОЛУЧКА), через
  **~6 секунд** над постоянным баннером всплывёт карточка на **~7 секунд**
  (закрывается сама или по «×»).

### Настройка таймингов всплывающего баннера

В `AdConfig.kt`:

```kotlin
const val POPUP_DELAY_MS = 6_000L   // через сколько после хода показать
const val POPUP_DISPLAY_MS = 7_000L // сколько висит до авто-скрытия
```

Если игрок делает новый ход до истечения задержки, таймер сбрасывается (показ только после последней паузы).

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
