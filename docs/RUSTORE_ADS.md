# Подключение рекламы RuStore Ads

Сейчас в приложении работает **плейсхолдер баннера** (`PlaceholderAdController`):
фрейм виден внизу экрана, вёрстка зафиксирована, сеть не используется —
приложение собирается и работает офлайн. Ниже — шаги, чтобы включить
**реальную рекламу RuStore Ads** (баннер + межстраничная).

> RuStore Ads SDK размещён в Maven-репозитории RuStore (vkpartner artifactory),
> недоступном в окружении, где готовился код. Поэтому интеграция оформлена как
> точка расширения: подключение SDK затрагивает один новый класс и конфиг.
> Сверяйтесь с официальной документацией RuStore Ads (актуальная версия SDK и
> сигнатуры методов): https://www.rustore.ru/help/sdk/

## Шаг 1. Репозиторий и зависимость

В `settings.gradle.kts` в блок `dependencyResolutionManagement { repositories { … } }`
добавьте репозиторий RuStore:

```kotlin
maven { url = uri("https://artifactory-external.vkpartner.ru/artifactory/maven") }
```

В `app/build.gradle.kts` добавьте зависимость рекламного SDK (версию возьмите
из документации RuStore Ads):

```kotlin
implementation("ru.rustore.sdk:adsdk:<АКТУАЛЬНАЯ_ВЕРСИЯ>")
```

## Шаг 2. Разрешение на интернет

В `app/src/main/AndroidManifest.xml` перед тегом `<application>` добавьте:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

> До этого шага приложение полностью офлайн. Реклама требует сети — это нужно
> отразить в политике конфиденциальности (см. docs/PRIVACY.md).

## Шаг 3. ID рекламных блоков

В `app/src/main/java/ru/cashflow/statement/ads/AdConfig.kt` подставьте реальные
ID блоков из кабинета RuStore Ads:

```kotlin
const val BANNER_AD_UNIT_ID = "ваш-id-баннера"
const val INTERSTITIAL_AD_UNIT_ID = "ваш-id-межстраничной"
```

## Шаг 4. Реализация контроллера

Создайте `app/src/main/java/ru/cashflow/statement/ads/RustoreAdController.kt`,
реализующий интерфейс `AdController`. Скелет (вызовы SDK помечены TODO —
подставьте по документации RuStore Ads):

```kotlin
package ru.cashflow.statement.ads

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class RustoreAdController(private val appContext: android.content.Context) : AdController {

    // private var interstitial: InterstitialAd? = null   // тип из RuStore Ads SDK
    private var movesSinceLastAd = 0

    override fun preload(activity: Activity?) {
        // TODO: инициализация RuStore Ads SDK и предзагрузка interstitial по
        //       AdConfig.INTERSTITIAL_AD_UNIT_ID.
    }

    @Composable
    override fun BannerFrame(modifier: Modifier) {
        AndroidView(
            modifier = modifier.fillMaxWidth().navigationBarsPadding(),
            factory = { ctx ->
                // TODO: создать BannerAdView из RuStore Ads SDK,
                //       задать AdConfig.BANNER_AD_UNIT_ID и загрузить.
                android.view.View(ctx)
            },
        )
    }

    override fun onInterstitialMoment(activity: Activity?) {
        activity ?: return
        // TODO: показать interstitial, если загружен; затем предзагрузить новый.
        //       Частоту можно ограничивать через movesSinceLastAd.
    }
}
```

## Шаг 5. Включить контроллер

В `AdConfig.controller()` верните реальную реализацию (для релиза):

```kotlin
fun controller(): AdController =
    if (BuildConfig.DEBUG) PlaceholderAdController
    else RustoreAdController(/* applicationContext */)
```

(в `MainActivity` вместо `AdConfig.controller()` можно передать
`AdConfig.controller(applicationContext)` — добавьте параметр в функцию.)

## Точки показа

- **Баннер** — постоянный фрейм внизу всех экранов (`MainActivity` →
  `ad.BannerFrame`).
- **Межстраничная** — на нечастых событиях: новая игра по профессии
  (`ProfileScreen`) и сброс отчёта (`StatementScreen`). Вызов:
  `ad.onInterstitialMoment(activity)`.

После этих шагов реклама заработает; ProGuard-правила SDK (если требуются)
добавьте в `app/proguard-rules.pro` по документации RuStore.
