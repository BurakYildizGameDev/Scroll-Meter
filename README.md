<div align="center">

# 📱 Scroll-Meter

### **Physical Distance Telemetry & Digital Well-being Tracker for Android**
*How many meters and kilometers does your thumb travel every single day?*

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-brightgreen.svg?style=for-the-badge&logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.10.00-blue.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Tests](https://img.shields.io/badge/Tests-77%2F77%20Passed-success.svg?style=for-the-badge&logo=checkmarx)](https://github.com)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline-emerald.svg?style=for-the-badge&logo=shield)](https://github.com)
[![Release APK](https://img.shields.io/badge/Release%20APK-3.3%20MB-orange.svg?style=for-the-badge&logo=android)](https://github.com)

<br/>

> **Scroll-Meter** measures your cumulative physical finger scroll distance across all installed Android applications (Instagram, TikTok, X, Reddit, etc.) with sub-millimeter precision, converting mindless scrolling into tangible real-world physical metaphors.

</div>

---

## ✨ Key Features

* **🛡️ 100% Privacy Guarantee (Zero Internet Access)**:
  * Zero `INTERNET` permission in `AndroidManifest.xml` — the app cannot communicate with any external servers.
  * `canRetrieveWindowContent="false"` — screen text, chats, passwords, and sensitive inputs can never be inspected.
  * Completely offline, local hardware-encrypted SQLite storage.
* **🎯 Hardware-Calibrated Precision**:
  * Calculates real-world millimeters from screen pixel density (`DisplayMetrics` PPI / DPI) and event deltas.
  * **Interactive On-Screen Calibration Ruler**: Place a physical ruler directly onto your smartphone screen to fine-tune scaling multipliers with micrometer precision.
* **🏆 Gamified Real-World Metaphors**:
  * Unlocks motivating landmarks as your daily distance increases:
    * 🦒 **Giraffe** (5.5 meters)
    * 🗽 **Statue of Liberty** (93 meters)
    * 🗼 **Eiffel Tower** (330 meters)
    * 🏙️ **Burj Khalifa** (828 meters)
    * 🏔️ **Mount Everest** (8,848 meters)
* **⚡ Cyber Luxury & Mineral Glow Design System**:
  * Dark obsidian void aesthetics designed for high-refresh AMOLED screens.
  * High-voltage dopamine typography and Bento Grid 2.0 telemetry cards.
  * Custom Canvas circular distance gauge with 240° open arc and neon sweep gradients.
  * Custom hardware-accelerated bar charts with zero external charting dependencies.
* **🔋 Battery-Efficient "Never-Crash" Architecture**:
  * 5-second debounced batch collector flushes scroll events in atomic SQLite transactions.
  * Screen-off broadcast receivers automatically sleep background processing.
  * WorkManager data retention worker prunes telemetry older than 30 days only while charging and idle.
* **🪶 Ultra-Lightweight 3.3 MB Binary**:
  * Hardened ProGuard / R8 code and resource shrinking.
  * Pre-compiled ART Baseline Profiles for 30% faster cold starts and 0ms frame drops.

---

## 🏛️ Architecture & Tech Stack

Scroll-Meter is built strictly adhering to **Enterprise Clean Architecture** and reactive **MVI (Model-View-Intent)** patterns.

```mermaid
flowchart TD
    subgraph Presentation ["Presentation Layer (Jetpack Compose + MVI)"]
        UI["Compose Screens (Dashboard, Analytics, Settings, Permission)"]
        VM["ViewModels (StateFlow & SharedFlow Effects)"]
        Nav["Type-Safe Navigation Compose (Kotlinx Serialization)"]
    end

    subgraph Domain ["Domain Layer (Pure Kotlin - Zero Android Dependencies)"]
        UC["UseCases (GetDailyDistance, CalculateMetaphors, Leaderboards)"]
        Calc["DistanceCalculator & ScrollAnomalyFilter"]
        Model["Pure Domain Models (PhysicalDistance, AppScrollSession)"]
    end

    subgraph Service ["Service Layer (Accessibility & Lifecycle)"]
        AS["ScrollAccessibilityService (Never-Crash SupervisorJob)"]
        Batch["ScrollBatchCollector (5s In-Memory Debouncing)"]
        Filter["PackageBlacklist & GestureFilters"]
        Receiver["ScreenStateReceiver (Power-Aware Sleep)"]
    end

    subgraph Data ["Data Layer (Room + DataStore + WorkManager)"]
        DB["Room SQLite (ScrollDao Atomic Upsert)"]
        DS["SettingsDataStore (Preferences DataStore)"]
        Worker["DataCleanupWorker (30-Day Retention Worker)"]
    end

    UI --> VM
    VM --> UC
    AS --> Filter --> Batch --> DB
    Receiver --> AS
    UC --> DB
    UC --> DS
    Worker --> DB
```

### Technology Highlights
* **Language:** Kotlin 2.0.21
* **UI:** Jetpack Compose BOM 2024.10.00 (Material Design 3)
* **Navigation:** Navigation Compose 2.8.3 with Kotlinx Serialization type-safe routes
* **DI:** Dagger-Hilt 2.52 with `@HiltWorker` and `@HiltViewModel`
* **Local Database:** Room 2.6.1 SQLite with atomic `ON CONFLICT DO UPDATE` queries
* **Key-Value Store:** Jetpack Preferences DataStore
* **Background Processing:** AndroidX WorkManager 2.9.1
* **Testing:** JUnit 5 (Jupiter), MockK, Turbine, Robolectric 4.13, Coroutines Test Dispatchers

---

## 📸 Screen Tour & Design System

| Dashboard Screen | Scroll Analytics | Screen Calibration |
| :---: | :---: | :---: |
| Dairesel açık yay mesafe göstergesi, anlık hedef yüzdesi, bento metrik kartları ve kilit açılan anıtlar. | İlk 5 uygulamanın Compose Canvas çubuk grafiği ve LazyColumn liderlik sıralaması. | Fiziksel cetvel ile ekran milimetresini eşitleyen sanal donanım cetveli ve hedef ayarları. |

---

## 🧪 Test Coverage & Quality Verification

All 60 production phases were implemented following strict Test-Driven Development (TDD).

```
================================================================================
Test Class                                                     Tests    Result
================================================================================
com.scrollmeter.app.core.result.ResultTest                       6      PASSED
com.scrollmeter.app.data.local.dao.ScrollDaoTest                 3      PASSED
com.scrollmeter.app.data.repository.ScrollRepositoryImplTest     3      PASSED
com.scrollmeter.app.data.worker.DataCleanupWorkerTest            2      PASSED
com.scrollmeter.app.domain.calculator.DistanceCalculatorTest    10      PASSED
com.scrollmeter.app.domain.filter.ScrollAnomalyFilterTest       13      PASSED
com.scrollmeter.app.domain.usecase.CalculateMetaphorUseCaseTest  3      PASSED
com.scrollmeter.app.domain.usecase.GetAppLeaderboardUseCaseTest  1      PASSED
com.scrollmeter.app.domain.usecase.GetDailyTotalDistance...      1      PASSED
com.scrollmeter.app.presentation.analytics.AnalyticsViewModelTest 2     PASSED
com.scrollmeter.app.presentation.dashboard.DashboardViewModelTest 2     PASSED
com.scrollmeter.app.presentation.navigation.RoutesTest           1      PASSED
com.scrollmeter.app.presentation.settings.SettingsViewModelTest  3      PASSED
com.scrollmeter.app.presentation.theme.ThemeTest                 2      PASSED
com.scrollmeter.app.service.batch.ScrollBatchCollectorTest       6      PASSED
com.scrollmeter.app.service.filter.PackageBlacklistTest          3      PASSED
com.scrollmeter.app.service.filter.ScrollGestureFilterTest       5      PASSED
com.scrollmeter.app.service.notification.ScrollNotification...   2      PASSED
com.scrollmeter.app.service.receiver.ScreenStateReceiverTest     4      PASSED
com.scrollmeter.app.service.ScrollAccessibilityServiceTest       5      PASSED
================================================================================
TOTAL: 77 TESTS | 77 PASSED (100% GREEN) | 0 FAILURES | 0 ERRORS
================================================================================
```

---

## 🚀 Building & Running Locally

### Prerequisites
* Android Studio Ladybug (2024.2+) or higher
* JDK 17 (Temurin / Zulu recommended)
* Android SDK 34 (Android 14) installed
* Min SDK: Android 9.0 (API 28)

### Clone & Build
```bash
# Clone the repository
git clone https://github.com/BurakYildizGameDev/Scroll-Meter.git
cd Scroll-Meter

# Run all 77 unit & integration tests
./gradlew testDebugUnitTest

# Build Debug APK
./gradlew assembleDebug

# Build Optimized Release APK (3.3 MB)
./gradlew assembleRelease
```

Generated APKs will be located at:
* **Debug APK:** `app/build/outputs/apk/debug/app-debug.apk`
* **Release APK:** `app/build/outputs/apk/release/app-release.apk`

---

## 🔒 Privacy & Permissions Deep Dive

Scroll-Meter adheres to the highest Android privacy standards:

1. **Zero Internet (`android.permission.INTERNET`)**:
   The application cannot transmit data over Wi-Fi, cellular, Bluetooth, or local networks.
2. **Accessibility Service Constraints (`accessibility_service_config.xml`)**:
   ```xml
   <accessibility-service
       android:accessibilityEventTypes="typeViewScrolled"
       android:accessibilityFeedbackType="feedbackGeneric"
       android:canRetrieveWindowContent="false"
       android:canRequestFilterKeyEvents="false"
       android:notificationTimeout="100" />
   ```
   * Setting `canRetrieveWindowContent="false"` makes it architecturally impossible for the service to inspect text fields, UI views, passwords, or personal messages.
3. **Blacklisted System Apps**:
   System keyboards, credential managers, lock screens, and launcher dialogues are blocked immediately at the service filter level.

---

## 📁 Repository Structure

```
Scroll-Meter/
├── .github/
│   └── workflows/
│       └── android_ci.yml           # Automated CI workflow
├── app/
│   ├── proguard-rules.pro           # R8 dead code stripping & obfuscation
│   └── src/
│       ├── main/
│       │   ├── baseline-profiles.txt # Ahead-of-Time ART compilation rules
│       │   ├── AndroidManifest.xml
│       │   └── java/com/scrollmeter/app/
│       │       ├── core/            # Dispatchers, Result wrappers
│       │       ├── data/            # Room SQLite, DataStore, Workers
│       │       ├── domain/          # Pure math models, calculators, use cases
│       │       ├── service/         # Accessibility service, debounced batcher
│       │       └── presentation/    # Compose UI, MVI, Canvas components, Theme
│       ├── test/                    # 77 unit & integration tests
│       └── androidTest/             # Compose E2E UI automation
└── docs/                            # 60-Phase Enterprise Architecture Guide
```

---

## 📄 License

Distributed under the Apache License 2.0. See `LICENSE` for more information.

---

<div align="center">
Crafted with ❤️ by <b>Burak Yıldız</b> (<a href="https://github.com/BurakYildizGameDev">@BurakYildizGameDev</a>)
</div>
