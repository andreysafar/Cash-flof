# Денежный поток — электронный финансовый отчёт (Android)

Приложение для настольной игры Роберта Кийосаки **«Денежный поток 101 + 202»**.
Каждый игрок может вести **электронный финансовый отчёт** вместо бумажного бланка:
крупные кнопки, типовые ходы, автоматический пересчёт всех итогов, журнал
операций с отменой. Приложение полностью **офлайн** (нет доступа в интернет,
данные хранятся только на устройстве).

Бумажный и электронный бланки полностью совместимы — за столом одни игроки могут
играть на бумаге, другие в приложении.

---

## Возможности

- Полный финансовый отчёт по официальному бланку **101** (`docs/rules/БЛАНК-101.pdf`):
  доходы, расходы, активы, пассивы, пассивный доход, общий доход/расход,
  месячный денежный поток.
- Автоматический расчёт по правилам игры (единый модуль `logic/Calculator.kt`).
- Быстрые кнопки типовых ходов: **Получка**, покупка/закрытие акций, сплит,
  покупка/продажа недвижимости и бизнеса, кредит банка и погашение, всякая
  всячина, ребёнок, благотворительность, увольнение, корректировка наличных.
- Индикатор и переход на **скоростную дорожку** (вторая сторона бланка 101):
  доход в «ДЕНЬ CASHFLOW», цель победы (+$50 000), бизнесы скоростной дорожки.
- Расширения **202** (`docs/rules/blank202.pdf`): опционы **CALL/PUT**
  (счётчик 3 ходов) и **короткие позиции (Short Sales)** с расчётом по формулам
  официального бланка.
- Журнал операций и **отмена** последнего действия.
- Автосохранение в локальный JSON-файл (восстанавливается при перезапуске).

## Источники правил

Файлы правил и бланков лежат в `docs/rules/` (присланы заказчиком):

| Файл | Назначение |
|---|---|
| `БЛАНК-101.pdf` | Финансовый отчёт 101 (крысиные бега + скоростная дорожка) |
| `blank202.pdf` | Таблицы 202: опционы CALL/PUT и короткие позиции |
| `Правила игры Денежный поток.pdf` | Правила 101 |
| `denejniy_potok.pdf` | Книга правил 101 (та же редакция) |
| `cashflow202rules.pdf` | Правила 202 |

Соответствие правил и реализации описано в `docs/RULES_MAPPING.md`.

---

## Сборка APK

> APK собирается на вашей машине. В этом проекте только исходный код и
> Gradle-обёртка; Android SDK не входит в репозиторий.

### Требования

- **JDK 17** или новее (проверено на JDK 17 и 21).
- **Android SDK** (Platform 35, Build-Tools 35.x). Проще всего поставить
  **Android Studio** (Koala / Ladybug или новее) — он сам ставит SDK.
- Доступ в интернет к репозиториям **`dl.google.com`**, **`maven.google.com`**
  (AndroidX, плагин Android Gradle) и **`repo.maven.apache.org`** при первой
  сборке. Дистрибутив Gradle 8.14.3 скачивается обёрткой с
  `services.gradle.org`.

> Примечание: эти исходники подготовлены в окружении, где `maven.google.com`
> и Android SDK недоступны, поэтому здесь проект **не компилировался**.
> Все версии зафиксированы в `gradle/libs.versions.toml` и совместимы между
> собой; сборку и проверку на устройстве выполняете вы.

### Вариант 1. Android Studio (рекомендуется)

1. `File → Open` → выберите корень репозитория.
2. Дождитесь Gradle Sync (скачает зависимости).
3. `Build → Build App Bundle(s) / APK(s) → Build APK(s)`.
4. APK будет здесь: `app/build/outputs/apk/debug/app-debug.apk`.

### Вариант 2. Командная строка

**Шаг 1. Java.** Для `./gradlew` нужен JDK. Если в терминале видите
`Unable to locate a Java Runtime` — JDK не установлен/не виден. Варианты:

- Использовать JDK, встроенный в Android Studio (ничего ставить не надо), macOS:
  ```bash
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  ```
  (Linux: `.../android-studio/jbr`; Windows: `...\Android Studio\jbr`)
- Либо поставить JDK 21 отдельно — macOS:
  ```bash
  brew install --cask temurin@21
  ```

Проверка: `java -version` должно показать версию 17 или 21.

**Шаг 2. Android SDK.** Укажите путь одним из способов:

- переменная окружения `ANDROID_HOME=/путь/к/Android/Sdk`, или
- файл `local.properties` в корне:
  ```properties
  sdk.dir=/путь/к/Android/Sdk
  ```

(если установлена Android Studio, SDK обычно в
`~/Library/Android/sdk` на macOS, `~/Android/Sdk` на Linux.)

**Шаг 3.** Сборка debug-APK:

```bash
./gradlew assembleDebug
# Windows: gradlew.bat assembleDebug
```

Готовый файл: `app/build/outputs/apk/debug/app-debug.apk`.

Сборка release-APK (без подписи):

```bash
./gradlew assembleRelease
```

### Установка на устройство

Включите на телефоне «Отладку по USB», затем:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Либо просто скопируйте `app-debug.apk` на телефон и откройте его
(разрешите установку из неизвестных источников).

### Запуск тестов логики расчётов

```bash
./gradlew test
```

Юнит-тесты `app/src/test/.../CalculatorTest.kt` проверяют пассивный доход,
итоги, получку, платёж по кредиту банка (10%), выход из крысиных бегов,
скоростную дорожку, опционы и короткие позиции.

---

## Диагностика частых ошибок

**`./gradlew ...` → `Unable to locate a Java Runtime`**
На машине нет JDK в PATH. Это не ошибка кода. Решение — «Шаг 1. Java» выше:
задать `JAVA_HOME` на встроенный в Android Studio JDK или поставить Temurin 21.
Либо вообще не использовать терминал и собирать через меню Android Studio
`Build → Build APK(s)` (там свой JDK).

**Android Studio: `Error running 'app': Run configuration app is not
supported in the current project. Cannot obtain the package.`**
Возникает, если конфигурация запуска создалась до завершения первой
синхронизации Gradle (она может идти несколько минут). По шагам:

1. Дождитесь полного завершения Gradle Sync (статус-бар внизу).
2. `File → Sync Project with Gradle Files`.
3. Сверху рядом с кнопкой ▶ выберите модуль **app** в списке конфигураций.
4. Если не помогло — `Build → Clean Project`, затем `Build → Rebuild Project`.
5. В крайнем случае — `File → Invalidate Caches… → Invalidate and Restart`.

Конфигурационный кэш Gradle намеренно отключён
(`org.gradle.configuration-cache=false` в `gradle.properties`) — он мог
вызывать именно эту ошибку IDE; включать обратно не требуется.

**Долгая первая синхронизация / `Sync is taking a significant amount…`**
Это нормально: при первом запуске скачиваются Android Gradle Plugin,
AndroidX, Compose и при необходимости SDK Platform 35. Нужен интернет к
`dl.google.com` и `maven.google.com`. Дальше всё берётся из кэша.

---

## Параметры проекта

| Параметр | Значение |
|---|---|
| Язык / UI | Kotlin, Jetpack Compose, интерфейс на русском |
| `applicationId` | `ru.cashflow.statement` |
| `minSdk` | 24 (Android 7.0) |
| `compileSdk` / `targetSdk` | 35 |
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.0.21 |
| Gradle | 8.14.3 (через wrapper) |
| Сеть | INTERNET — для рекламы VK Ad SDK; игровые данные офлайн |

## Публикация на RuStore и реклама

- Подготовка релиза (подпись, версия, иконка, сборка AAB, карточка) —
  `docs/RUSTORE_PUBLISH.md`.
- Реклама VK Рекламы (баннер + нативный оверлей + межстраничная) —
  `docs/VK_ADS.md`. ID блоков в `AdConfig.kt`.
- Политика конфиденциальности — `docs/PRIVACY.md`.
- Подпись: положите `keystore.properties` в корень (см.
  `keystore.properties.sample`); release-сборка подхватит его автоматически.

## Структура

```
app/src/main/java/ru/cashflow/statement/
  MainActivity.kt          # тема, рекламный фрейм, провайдер рекламы
  ads/AdController.kt      # абстракция рекламы
  ads/VkAdController.kt    # VK Ad SDK: баннер + interstitial
  ads/AdConfig.kt          # slot ID блоков из кабинета VK
  model/Models.kt          # данные финансового отчёта (kotlinx.serialization)
  logic/Calculator.kt      # ЕДИНЫЙ источник правил расчёта
  data/StateRepository.kt  # офлайн-хранилище (JSON в filesDir)
  ui/StatementViewModel.kt # действия, снимок/отмена, автосохранение
  ui/StatementScreen.kt    # главный экран (крысиные бега)
  ui/FastTrackScreen.kt    # скоростная дорожка
  ui/Advanced202Screen.kt  # опционы / короткие позиции (202)
  ui/ProfileScreen.kt      # ввод карточки «Профессия»
  ui/HistoryScreen.kt      # журнал операций
  ui/ActionDialogs.kt      # диалоги быстрых действий
docs/rules/                # официальные правила и бланки (PDF)
docs/RULES_MAPPING.md      # соответствие правил и кода
```

## Как пользоваться (кратко)

1. Откройте меню (⋮) → **«Профессия / профиль»**, перенесите данные с карточки
   профессии (зарплата, налоги, выплаты, пассивы, стартовые наличные) и
   сохраните.
2. На главном экране ведите игру кнопками: **ПОЛУЧКА** в день выплат, сделки,
   кредиты, расходы и т.д. Итоги пересчитываются автоматически.
3. Когда **пассивный доход превысит общий расход** — нажмите
   **«Выйти на скоростную дорожку»**.
4. Ошиблись — **«Отмена»** (журнал операций в меню).
