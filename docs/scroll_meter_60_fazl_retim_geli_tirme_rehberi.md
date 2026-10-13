# Scroll-Meter: Üretim Sürümü İçin 60 Fazlı Kurumsal Geliştirme Rehberi

**Sürüm:** 3.1 (Enterprise Production Architecture)  
**Hedef Kitle:** Otonom Kodlama Ajanları (DeepSeek, Claude, Gemini, Antigravity) ve Kıdemli Android Mühendisleri  
**Temel Metodoloji:** Test-Driven Development (TDD: Red ➔ Green ➔ Refactor), Clean Architecture, Reaktif MVI, Sıfır Batarya Sızıntısı (Zero-Battery Drain)  

Bu rehber, Scroll-Meter projesini sıfırdan markete çıkışa (Production Release) kadar adım adım inşa etmek için hazırlanmış 60 ayrık ve birbirini tamamlayan geliştirme fazından oluşur. 

---

### Geliştirme ve TDD Uygulama Protokolü
1. **Sırayı Bozma:** Fazlar kesin bağımlılık zincirine göre sıralanmıştır. Bir faz tamamlanmadan ve testleri yeşile dönmeden bir sonraki faza geçilemez.
2. **Önce Test (TDD):** İş mantığı, veri işleme ve sunum durumları içeren tüm fazlarda önce test sınıfı yazılmalı (RED), testin derlendiği ancak başarısız olduğu görülmeli, ardından asıl kod yazılarak test geçirilmelidir (GREEN).
3. **Sıfır Hata / Sıfır Uyarı:** Her faz sonunda Detekt ve derleme kontrolleri çalıştırılmalı; hiçbir linter uyarısı veya bellek sızıntısı kalmamalıdır.

---

## BÖLÜM 1: Kurumsal Proje Temelleri ve Yapılandırma (Faz 1 - 8)

### Faz 1: Android Proje İskeleti ve Hedef SDK Ayarları
*   **Amaç:** Modern Android geliştirme standartlarına (MAD) uygun, temiz, minimum SDK 28 (Android 9 Pie) ve hedef SDK 34/35 (Android 14/15) uyumlu kök proje iskeletini oluşturmak.
*   **Dosyalar:**
    *   `build.gradle.kts` (Root Project)
    *   `app/build.gradle.kts` (App Module)
    *   `settings.gradle.kts`
    *   `gradle.properties`
*   **TDD / Geliştirme Adımları:**
    1.  `gradle.properties` içine AndroidX, R8, Jetifier olmayan modern bayrakları (`android.useAndroidX=true`, `android.nonTransitiveRClass=true`, `org.gradle.jvmargs=-Xmx4096m`) ekle.
    2.  `app/build.gradle.kts` içinde `compileSdk = 34`, `minSdk = 28`, `targetSdk = 34`, `namespace = "com.scrollmeter.app"` olarak tanımla.
    3.  Java ve Kotlin toolchain ayarlarını Java 17 (LTS) olarak sabitle.
*   **Kod Şablonu:**
    ```kotlin
    // app/build.gradle.kts
    plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.kotlin.android)
        alias(libs.plugins.kotlin.compose)
    }

    android {
        namespace = "com.scrollmeter.app"
        compileSdk = 34

        defaultConfig {
            applicationId = "com.scrollmeter.app"
            minSdk = 28
            targetSdk = 34
            versionCode = 1
            versionName = "1.0.0"
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += listOf("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
        }
    }
    ```
*   **Kritik Edge Case:** Eski Android Studio şablonlarındaki gereksiz AppCompat ve XML bağımlılıklarını kaldır; saf Jetpack Compose projesi başlat.
*   **Kabul Kriteri:** `./gradlew assembleDebug` komutunun 0 hata ile başarıyla sonlanması.

---

### Faz 2: Version Catalogs (libs.versions.toml) ile Merkezi Bağımlılık Yönetimi
*   **Amaç:** Kütüphane bağımlılıklarını merkezi, tip-güvenli ve sürüm çakışması yaratmayacak şekilde `libs.versions.toml` dosyasında yapılandırmak.
*   **Dosyalar:**
    *   `gradle/libs.versions.toml`
    *   `app/build.gradle.kts`
*   **TDD / Geliştirme Adımları:**
    1.  Compose BOM (Bill of Materials), Room (KSP ile), Dagger-Hilt, Coroutines, Navigation Compose, DataStore Preferences, Detekt, JUnit 5, MockK, Turbine kütüphanelerini tanımla.
    2.  `app/build.gradle.kts` içindeki tüm string bağımlılıkları `libs.*` sözdizimine dönüştür.
*   **Kod Şablonu:**
    ```toml
    # gradle/libs.versions.toml
    [versions]
    agp = "8.3.2"
    kotlin = "2.0.0"
    ksp = "2.0.0-1.0.21"
    composeBom = "2024.06.00"
    hilt = "2.51.1"
    room = "2.6.1"
    coroutines = "1.8.1"
    datastore = "1.1.1"
    navigation = "2.8.0-beta02"
    junit5 = "5.10.2"
    mockk = "1.13.11"
    turbine = "1.1.0"
    timber = "5.0.1"

    [libraries]
    androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
    androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
    androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
    androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
    androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
    androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
    hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
    hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
    test-junit5-api = { group = "org.junit.jupiter", name = "junit-jupiter-api", version.ref = "junit5" }
    test-mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
    test-turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
    ```
*   **Kabul Kriteri:** Gradle sync hatasız tamamlanmalı ve tüm kütüphaneler catalog üzerinden çözümlenmelidir.

---

### Faz 3: Clean Architecture ve Katmanlı Klasör Hiyerarşisi
*   **Amaç:** Projenin katmanlar arası sınırlarını netleştirmek ve modüler klasör hiyerarşisini kurmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/core/`
    *   `app/src/main/java/com/scrollmeter/app/di/`
    *   `app/src/main/java/com/scrollmeter/app/domain/model/`
    *   `app/src/main/java/com/scrollmeter/app/domain/repository/`
    *   `app/src/main/java/com/scrollmeter/app/domain/usecase/`
    *   `app/src/main/java/com/scrollmeter/app/data/local/`
    *   `app/src/main/java/com/scrollmeter/app/data/repository/`
    *   `app/src/main/java/com/scrollmeter/app/service/`
    *   `app/src/main/java/com/scrollmeter/app/presentation/`
*   **TDD / Geliştirme Adımları:**
    1.  Her paketin `package-info.java` veya boş bir `.keep` dosyasıyla dizin yapısını oluştur.
    2.  Domain katmanına hiçbir Android framework sınıfının eklenmeyeceğine dair kuralları belirle.
*   **Kabul Kriteri:** Klasör yapısı disk üzerinde eksiksiz oluşmalı, projenin package yapısı kurumsal hiyerarşiyi yansıtmalıdır.

---

### Faz 4: Statik Kod Analizi (Detekt / Ktlint) Entegrasyonu
*   **Amaç:** Kod kalitesini, mimari standartları ve isimlendirme konvansiyonlarını otomatik denetleyecek statik analiz motorunu kurmak.
*   **Dosyalar:**
    *   `config/detekt/detekt.yml`
    *   `app/build.gradle.kts`
*   **TDD / Geliştirme Adımları:**
    1.  Detekt gradle plugin'ini projeye ekle.
    2.  `detekt.yml` dosyasında: `naming`, `complexity` (LongMethod max 60, LongParameterList max 5), `potential-bugs`, `style` (WildcardImport yasak) kurallarını aktif et.
    3.  `./gradlew detekt` task'ını check döngüsüne bağla.
*   **Kabul Kriteri:** `./gradlew detekt` komutu çalıştırıldığında projedeki kodların temiz olduğu raporlanmalıdır.

---

### Faz 5: Dagger-Hilt Kurulumu ve Application Sınıfı
*   **Amaç:** Bağımlılık Enjeksiyonu (DI) konteynerini başlatmak ve uygulama yaşam döngüsüne bağlamak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/ScrollMeterApp.kt`
    *   `app/src/main/AndroidManifest.xml`
*   **TDD / Geliştirme Adımları:**
    1.  `ScrollMeterApp.kt` sınıfını oluştur ve `@HiltAndroidApp` anotasyonu ekle.
    2.  `AndroidManifest.xml` içinde `application` etiketine `android:name=".ScrollMeterApp"` niteliğini ekle.
*   **Kod Şablonu:**
    ```kotlin
    package com.scrollmeter.app

    import android.app.Application
    import dagger.hilt.android.HiltAndroidApp

    @HiltAndroidApp
    class ScrollMeterApp : Application() {
        override fun onCreate() {
            super.onCreate()
        }
    }
    ```
*   **Kabul Kriteri:** Hilt kaynak kod üretimi (code generation) hatasız tamamlanmalı, `Hilt_ScrollMeterApp` sınıfı üretilmelidir.

---

### Faz 6: Hilt Modüllerinin (Modules) İskelet Yapısı
*   **Amaç:** Singleton bileşenleri ve katmanlar arası bağımlılıkları barındıracak modül çatılarını kurmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/di/AppModule.kt`
    *   `app/src/main/java/com/scrollmeter/app/di/DatabaseModule.kt`
    *   `app/src/main/java/com/scrollmeter/app/di/RepositoryModule.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `@Module` ve `@InstallIn(SingletonComponent::class)` anotasyonlarıyla modül nesnelerini oluştur.
    2.  Soyut modüller (`RepositoryModule`) için `interface`, somut modüller (`DatabaseModule`, `AppModule`) için `object` yapısını seç.
*   **Kabul Kriteri:** Hilt dependency tree başarıyla derlenmeli, döngüsel bağımlılık (circular dependency) hatası olmamalıdır.

---

### Faz 7: Temel Coroutine Dispatcher Enjeksiyonu
*   **Amaç:** Asenkron iş parçacıklarını sabit (`Dispatchers.IO`) yazmak yerine test edilebilir biçimde DI üzerinden enjekte edilebilir kılmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/core/dispatchers/CoroutineDispatchers.kt`
    *   `app/src/main/java/com/scrollmeter/app/di/DispatcherModule.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `@IoDispatcher`, `@DefaultDispatcher`, `@MainDispatcher` özel qualifier anotasyonlarını tanımla.
    2.  `DispatcherModule` içinde `Dispatchers.IO`, `Dispatchers.Default`, `Dispatchers.Main` sağlayıcılarını `@Provides` ile sun.
*   **Kod Şablonu:**
    ```kotlin
    package com.scrollmeter.app.core.dispatchers

    import javax.inject.Qualifier

    @Qualifier @Retention(AnnotationRetention.BINARY) annotation class IoDispatcher
    @Qualifier @Retention(AnnotationRetention.BINARY) annotation class DefaultDispatcher
    @Qualifier @Retention(AnnotationRetention.BINARY) annotation class MainDispatcher

    // di/DispatcherModule.kt
    @Module
    @InstallIn(SingletonComponent::class)
    object DispatcherModule {
        @Provides @IoDispatcher fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
        @Provides @DefaultDispatcher fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
        @Provides @MainDispatcher fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    }
    ```
*   **Kabul Kriteri:** Unit testlerde `StandardTestDispatcher` kolayca enjekte edilebilmelidir.

---

### Faz 8: Ortak Sonuç Modeli (Result Wrapper)
*   **Amaç:** Asenkron işlemlerin başarı, hata ve yüklenme durumlarını tip-güvenli temsil eden `Result<T>` sınıfını oluşturmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/core/result/Result.kt`
    *   `app/src/test/java/com/scrollmeter/app/core/result/ResultTest.kt`
*   **TDD / Geliştirme Adımları:**
    1.  *RED:* `ResultTest` sınıfında `Success`, `Error`, `Loading` durumlarını ve `map` dönüştürücüsünü test eden testleri yaz.
    2.  *GREEN:* `sealed interface Result<out T>` yapısını ve yardımcı uzantı fonksiyonlarını (`map`, `onSuccess`, `onError`) yaz.
*   **Kod Şablonu:**
    ```kotlin
    sealed interface Result<out T> {
        data class Success<out T>(val data: T) : Result<T>
        data class Error(val throwable: Throwable) : Result<Nothing>
        data object Loading : Result<Nothing>
    }

    inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(throwable)
        is Result.Loading -> Result.Loading
    }
    ```
*   **Kabul Kriteri:** `ResultTest` tüm durumlar için %100 coverage ile geçmelidir.

---

## BÖLÜM 2: Domain Katmanı ve Saf İş Mantığı (Faz 9 - 18)

### Faz 9: Domain Modellerinin (Data Classes) Oluşturulması
*   **Amaç:** Veritabanından ve Android SDK'dan tamamen bağımsız, saf Kotlin modellerini tanımlamak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/domain/model/PhysicalDistance.kt`
    *   `app/src/main/java/com/scrollmeter/app/domain/model/AppScrollSession.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `PhysicalDistance` sınıfını milimetre tabanlı, metre ve kilometre değerlerini hesaplayan immutable bir model olarak tasarla.
    2.  `AppScrollSession` sınıfını paket adı, uygulama adı ve toplam piksel/milimetre verilerini tutacak şekilde oluştur.
*   **Kod Şablonu:**
    ```kotlin
    data class PhysicalDistance(val totalMilliMeters: Double) {
        val inMeters: Double get() = totalMilliMeters / 1000.0
        val inKilometers: Double get() = totalMilliMeters / 1_000_000.0
        val formattedMeters: String get() = String.format(java.util.Locale.US, "%.2f m", inMeters)
        val formattedKilometers: String get() = String.format(java.util.Locale.US, "%.3f km", inKilometers)
    }

    data class AppScrollSession(
        val packageName: String,
        val appDisplayName: String,
        val totalPixels: Double,
        val physicalDistance: PhysicalDistance
    )
    ```
*   **Kabul Kriteri:** Modeller immutable olmalı ve hiçbir Android SDK bağımlılığı içermemelidir.

---

### Faz 10: Domain Arayüzlerinin (Interfaces) Tanımlanması
*   **Amaç:** Bağımlılık Tersine Çevirme (Dependency Inversion) prensibiyle Data katmanını soyutlayan kontratları yazmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/domain/repository/IScrollRepository.kt`
    *   `app/src/main/java/com/scrollmeter/app/domain/repository/IDeviceMetricsProvider.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `IScrollRepository` interface'ini günlük piksel kaydetme, bugünün toplamını Flow olarak dinleme ve oturumları listeleme fonksiyonlarıyla donat.
    2.  `IDeviceMetricsProvider` interface'ini PPI değerini ve kalibrasyon çarpanını dönecek şekilde tanımla.
*   **Kod Şablonu:**
    ```kotlin
    interface IScrollRepository {
        fun getDailyTotalPixels(epochDays: Long): Flow<Double>
        fun getDailyAppSessions(epochDays: Long): Flow<List<AppScrollSession>>
        suspend fun recordScrollBatch(epochDays: Long, batch: Map<String, Double>)
    }

    interface IDeviceMetricsProvider {
        fun getDevicePpi(): Flow<Float>
        fun getCalibrationMultiplier(): Flow<Float>
    }
    ```
*   **Kabul Kriteri:** Arayüzler reaktif Flow yapılarıyla uyumlu olmalıdır.

---

### Faz 11: Matematik Motoru (Distance Calculator) Algoritması
*   **Amaç:** X ve Y ekseninden gelen piksel hareketlerini Pisagor teoremiyle birleştiren ve ekran PPI'ına göre fiziksel mesafeye (mm, m) dönüştüren saf matematik motorunu yazmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/domain/calculator/DistanceCalculator.kt`
*   **TDD / Geliştirme Adımları:**
    1.  Hipotenüs hesabı: $\sqrt{\Delta x^2 + \Delta y^2}$
    2.  Pikselden milimetreye dönüşüm: $\text{mm} = \frac{\text{piksel} \times 25.4}{\text{PPI} \times \text{çarpan}}$
    3.  `Double` hassasiyeti ve sıfıra bölme (Zero Division) koruması ekle.
*   **Kod Şablonu:**
    ```kotlin
    object DistanceCalculator {
        private const val MM_PER_INCH = 25.4

        fun calculateHypotenusePixels(deltaX: Float, deltaY: Float): Double {
            val dx = kotlin.math.abs(deltaX.toDouble())
            val dy = kotlin.math.abs(deltaY.toDouble())
            if (dx == 0.0) return dy
            if (dy == 0.0) return dx
            return kotlin.math.sqrt(dx * dx + dy * dy)
        }

        fun pixelsToMillimeters(pixels: Double, ppi: Float, multiplier: Float = 1.0f): Double {
            val effectivePpi = (ppi * multiplier).coerceAtLeast(1.0f)
            return (pixels * MM_PER_INCH) / effectivePpi
        }
    }
    ```
*   **Kabul Kriteri:** Fonksiyonlar yan etkisiz (pure function) olmalıdır.

---

### Faz 12: TDD - Matematik Motoru Birim Testleri
*   **Amaç:** `DistanceCalculator` motorunun matematiksel doğruluğunu ve sınır değerlerini JUnit 5 ile kanıtlamak.
*   **Dosyalar:**
    *   `app/src/test/java/com/scrollmeter/app/domain/calculator/DistanceCalculatorTest.kt`
*   **TDD / Geliştirme Adımları:**
    1.  *RED:* 3-4-5 üçgeni hipotenüs testi (X=300, Y=400 ➔ 500.0).
    2.  *RED:* Sıfır deltalar (X=0, Y=0 ➔ 0.0).
    3.  *RED:* Negatif deltalar (X=-100, Y=-200 ➔ pozitif hipotenüs).
    4.  *RED:* PPI = 0 veya negatif gelmesi durumunda `coerceAtLeast` koruma testi.
    5.  *GREEN:* Testlerin tamamını başarıyla koştur.
*   **Kabul Kriteri:** Tüm testler yeşil yanmalı, coverage %100 olmalıdır.

---

### Faz 13: Günlük Mesafe Hesaplama UseCase'i
*   **Amaç:** Repository'den gelen toplam piksel akışını donanım PPI'ı ile birleştirip ekrana sunulacak `PhysicalDistance` akışına dönüştüren UseCase'i oluşturmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/domain/usecase/GetDailyTotalDistanceUseCase.kt`
*   **TDD / Geliştirme Adımları:**
    1.  UseCase'e `IScrollRepository`, `IDeviceMetricsProvider` ve `@IoDispatcher CoroutineDispatcher` enjekte et.
    2.  `combine` operatörü ile piksel ve PPI akışlarını eşleştirip `DistanceCalculator` üzerinden dönüştür.
*   **Kod Şablonu:**
    ```kotlin
    class GetDailyTotalDistanceUseCase @Inject constructor(
        private val repository: IScrollRepository,
        private val metricsProvider: IDeviceMetricsProvider,
        @IoDispatcher private val dispatcher: CoroutineDispatcher
    ) {
        operator fun invoke(epochDays: Long): Flow<Result<PhysicalDistance>> {
            return combine(
                repository.getDailyTotalPixels(epochDays),
                metricsProvider.getDevicePpi(),
                metricsProvider.getCalibrationMultiplier()
            ) { pixels, ppi, multiplier ->
                val mm = DistanceCalculator.pixelsToMillimeters(pixels, ppi, multiplier)
                PhysicalDistance(mm)
            }
            .map { Result.Success(it) as Result<PhysicalDistance> }
            .catch { emit(Result.Error(it)) }
            .flowOn(dispatcher)
        }
    }
    ```
*   **Kabul Kriteri:** UseCase akışı hata durumunda `Result.Error` fırlatmalı, akış çökmeksizin yönetilmelidir.

---

### Faz 14: Liderlik Tablosu UseCase'i (Uygulama Bazlı)
*   **Amaç:** Gün boyunca en çok kaydırma yapılan uygulamaları mesafeye göre büyükten küçüğe sıralayan iş mantığını kurmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/domain/usecase/GetAppLeaderboardUseCase.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `IScrollRepository.getDailyAppSessions(epochDays)` akışını dinle.
    2.  Gelen listeyi `totalPixels` değerine göre azalan (`sortedByDescending`) düzende filtrele.
*   **Kabul Kriteri:** Sıralama tam ve doğru olmalı, 0 piksel olan boş uygulamalar listelenmemelidir.

---

### Faz 15: Metafor Motoru (Oyunlaştırma) UseCase'i
*   **Amaç:** Ham metre değerini kullanıcı için eğlenceli ve anlaşılır dünyevi metaforlara (Karınca, Zürafa, Eyfel Kulesi) dönüştüren algoritmayı yazmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/domain/model/MetaphorMilestone.kt`
    *   `app/src/main/java/com/scrollmeter/app/domain/usecase/CalculateMetaphorUseCase.kt`
*   **TDD / Geliştirme Adımları:**
    1.  Hedef eşikleri listesini tanımla (5m Zürafa, 50m Otobüs, 300m Eyfel, 8848m Everest).
    2.  Kullanıcının mevcut metresine göre en son aşılan kilometre taşını ve bir sonraki hedefe kalan mesafeyi hesapla.
*   **Kod Şablonu:**
    ```kotlin
    data class MetaphorMilestone(
        val name: String,
        val targetMeters: Double,
        val iconResName: String,
        val description: String
    )

    data class MetaphorProgress(
        val currentMilestone: MetaphorMilestone,
        val nextMilestone: MetaphorMilestone?,
        val remainingMetersToNext: Double,
        val progressPercentage: Float
    )
    ```
*   **Kabul Kriteri:** Tüm mesafe aralıkları (0m dahil) için geçerli ve mantıklı bir metafor üretilmelidir.

---

### Faz 16: TDD - UseCase Birim Testleri (MockK ile)
*   **Amaç:** `GetDailyTotalDistanceUseCase` ve `GetAppLeaderboardUseCase` sınıflarını MockK ve Turbine ile test etmek.
*   **Dosyalar:**
    *   `app/src/test/java/com/scrollmeter/app/domain/usecase/GetDailyTotalDistanceUseCaseTest.kt`
    *   `app/src/test/java/com/scrollmeter/app/domain/usecase/GetAppLeaderboardUseCaseTest.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `IScrollRepository` ve `IDeviceMetricsProvider` sahte davranışlarını `every { ... } returns flowOf(...)` ile tanımla.
    2.  Turbine `test { ... }` bloğu ile `awaitItem()` çağırarak `Result.Success` verisini ve beklenen mesafe değerini onayla.
*   **Kabul Kriteri:** Asenkron akışlar %100 deterministik olarak test edilmeli ve yeşil yanmalıdır.

---

### Faz 17: İş Mantığı Edge Caseleri ve Güvenlik Sınırları
*   **Amaç:** Sistemden gelebilecek anormal veri sıçramalarını (anomalileri) eleyen filtreleme kuralını (`ScrollAnomalyFilter`) yazmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/domain/filter/ScrollAnomalyFilter.kt`
*   **TDD / Geliştirme Adımları:**
    1.  Tekil scroll olayında 5000 pikseli aşan sıçramaları filtrele.
    2.  3 pikselin altındaki mikro titremeleri (jitter) göz ardı et.
*   **Kod Şablonu:**
    ```kotlin
    object ScrollAnomalyFilter {
        private const val MIN_VALID_JITTER_PX = 3.0
        private const val MAX_VALID_SINGLE_DELTA_PX = 5000.0

        fun isValidScroll(deltaX: Float, deltaY: Float): Boolean {
            val hypotenuse = DistanceCalculator.calculateHypotenusePixels(deltaX, deltaY)
            return hypotenuse >= MIN_VALID_JITTER_PX && hypotenuse <= MAX_VALID_SINGLE_DELTA_PX
        }
    }
    ```
*   **Kabul Kriteri:** Aşırı ve parazit hareketler iş katmanına geçirilmemelidir.

---

### Faz 18: TDD - Anomali Tespiti Testleri
*   **Amaç:** `ScrollAnomalyFilter` sınır testlerini yazmak.
*   **Dosyalar:**
    *   `app/src/test/java/com/scrollmeter/app/domain/filter/ScrollAnomalyFilterTest.kt`
*   **TDD / Geliştirme Adımları:**
    1.  *RED:* Delta 1.0px ➔ false (Jitter).
    2.  *RED:* Delta 150.0px ➔ true (Geçerli kaydırma).
    3.  *RED:* Delta 12.000px ➔ false (Anomali sıçrama).
    4.  *GREEN:* Testlerin başarılı olduğunu doğrula.
*   **Kabul Kriteri:** Sınır değerlerinin tümü test edilip onaylanmalıdır.

---

## BÖLÜM 3: Veri Katmanı, Yerel Depolama ve Hilt Entegrasyonu (Faz 19 - 30)

### Faz 19: Room Veritabanı Varlıklarının (Entity) Tasarlanması
*   **Amaç:** Günlük uygulama bazlı kümülatif kaydırma verilerini tutacak normalize edilmiş Room Entity'sini oluşturmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/data/local/entity/DailyAppScrollEntity.kt`
*   **TDD / Geliştirme Adımları:**
    1.  Tablo adı: `daily_app_scroll`.
    2.  Bileşik Birincil Anahtar (Composite Primary Key): `dateEpochDays` ve `packageName`.
    3.  Sütunlar: `totalPixels`, `totalMilliMeters`, `lastUpdatedTimestamp`.
*   **Kod Şablonu:**
    ```kotlin
    @Entity(
        tableName = "daily_app_scroll",
        primaryKeys = ["dateEpochDays", "packageName"],
        indices = [
            Index(value = ["dateEpochDays"]),
            Index(value = ["packageName"])
        ]
    )
    data class DailyAppScrollEntity(
        val dateEpochDays: Long,
        val packageName: String,
        val totalPixels: Double,
        val totalMilliMeters: Double,
        val lastUpdatedTimestamp: Long
    )
    ```
*   **Kabul Kriteri:** Entity şeması milyonlarca satırı destekleyecek şekilde indekslenmiş olmalıdır.

---

### Faz 20: Yüksek Performanslı DAO (Data Access Object) Üretimi
*   **Amaç:** Atomik `ON CONFLICT DO UPDATE` (Upsert) sorgusu içeren reaktif DAO arayüzünü oluşturmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/data/local/dao/ScrollDao.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `upsertBatch` fonksiyonu için saf SQLite atomik toplama sorgusunu yaz.
    2.  Günün toplam pikselini dönen `getDailyTotalPixels(epochDays: Long): Flow<Double?>` sorgusunu oluştur.
*   **Kod Şablonu:**
    ```kotlin
    @Dao
    interface ScrollDao {
        @Query("""
            INSERT INTO daily_app_scroll (dateEpochDays, packageName, totalPixels, totalMilliMeters, lastUpdatedTimestamp)
            VALUES (:dateEpochDays, :packageName, :pixelsToAdd, :mmToAdd, :timestamp)
            ON CONFLICT(dateEpochDays, packageName) DO UPDATE SET
                totalPixels = daily_app_scroll.totalPixels + excluded.totalPixels,
                totalMilliMeters = daily_app_scroll.totalMilliMeters + excluded.totalMilliMeters,
                lastUpdatedTimestamp = excluded.lastUpdatedTimestamp
        """)
        suspend fun upsertScroll(
            dateEpochDays: Long,
            packageName: String,
            pixelsToAdd: Double,
            mmToAdd: Double,
            timestamp: Long
        )

        @Query("SELECT SUM(totalPixels) FROM daily_app_scroll WHERE dateEpochDays = :epochDays")
        fun getDailyTotalPixels(epochDays: Long): Flow<Double?>

        @Query("SELECT * FROM daily_app_scroll WHERE dateEpochDays = :epochDays ORDER BY totalPixels DESC")
        fun getDailySessions(epochDays: Long): Flow<List<DailyAppScrollEntity>>
    }
    ```
*   **Kabul Kriteri:** Upsert sorguları veri yarışması (race condition) olmadan kümülatif ekleme yapmalıdır.

---

### Faz 21: TDD - Room DAO Testleri (In-Memory DB)
*   **Amaç:** In-Memory SQLite ile DAO'nun kümülatif toplama mantığını test etmek.
*   **Dosyalar:**
    *   `app/src/test/java/com/scrollmeter/app/data/local/dao/ScrollDaoTest.kt` (Robolectric ile)
*   **TDD / Geliştirme Adımları:**
    1.  `Room.inMemoryDatabaseBuilder` ile geçici veritabanı aç.
    2.  Aynı günde "com.instagram" için önce 500px, sonra 300px upsert et.
    3.  `getDailyTotalPixels` ile okunan değerin 800px olduğunu doğrula.
*   **Kabul Kriteri:** Kümülatif toplama hatasız çalışmalı ve dünkü veriler bugüne sızmamalıdır.

---

### Faz 22: Room Database Sınıfı ve TypeConverters
*   **Amaç:** Veritabanı yönetim sınıfını tanımlamak ve şema versiyonlamasını kurmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/data/local/database/ScrollMeterDatabase.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `@Database(entities = [DailyAppScrollEntity::class], version = 1, exportSchema = true)` yapılandır.
    2.  `abstract fun scrollDao(): ScrollDao` metodunu sağla.
*   **Kabul Kriteri:** KSP annotation processing hatasız derlenmeli ve `ScrollMeterDatabase_Impl` oluşturulmalıdır.

---

### Faz 23: Settings DataStore (Kullanıcı Tercihleri ve Kalibrasyon)
*   **Amaç:** Hafif konfigürasyonları asenkron ve tip-güvenli saklamak için Preferences DataStore kurmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/data/datastore/SettingsDataStore.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `KEY_CALIBRATION_MULTIPLIER` (Float, default 1.0f).
    2.  `KEY_DAILY_GOAL_METERS` (Float, default 100.0f).
    3.  Flow tabanlı okuma ve `edit` fonksiyonlarını yaz.
*   **Kabul Kriteri:** DataStore okuma/yazma işlemleri IO thread'inde non-blocking çalışmalıdır.

---

### Faz 24: Donanım Metrikleri Sağlayıcısı (DeviceMetricsProviderImpl)
*   **Amaç:** Cihazın ekran PPI değerini `DisplayMetrics` üzerinden okuyan sağlayıcıyı geliştirmek.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/data/resolver/DeviceMetricsProviderImpl.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `IDeviceMetricsProvider` interface'ini implemente et.
    2.  `Resources.getSystem().displayMetrics` üzerinden `xdpi` ve `ydpi` ortalamasını al. Hatalı cihazlar için minimum 160 PPI tabanı koy.
*   **Kabul Kriteri:** Sağlayıcı negatif veya tanımsız PPI döndürmemelidir.

---

### Faz 25: App Name Resolver (Paket İsimlerini Çözme)
*   **Amaç:** Paket isimlerini (`com.twitter.android`) kullanıcı dostu isimlere (`X / Twitter`) ve ikonlara dönüştüren, bellek önbellekli (LruCache) motoru yazmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/data/resolver/AppNameResolver.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `LruCache<String, String>` ile 200 adet paket adını bellekte tut.
    2.  Önbellekte yoksa `PackageManager.getApplicationLabel` ile çöz ve önbelleğe yaz.
*   **Kabul Kriteri:** Yinelenen çağrılar IPC yapmadan doğrudan RAM'den 0ms ile dönmelidir.

---

### Faz 26: Repository Implementasyonu (ScrollRepositoryImpl)
*   **Amaç:** `IScrollRepository` arayüzünün Data katmanındaki tam implementasyonunu yazmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/data/repository/ScrollRepositoryImpl.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `ScrollDao`, `AppNameResolver` ve `IDeviceMetricsProvider` bileşenlerini inject et.
    2.  `getDailyAppSessions` akışında `DailyAppScrollEntity` nesnelerini `AppScrollSession` domain modellerine dönüştür.
*   **Kabul Kriteri:** Domain modellerine dönüşüm eksiksiz ve saf olmalıdır.

---

### Faz 27: TDD - Repository Mapper Testleri
*   **Amaç:** Repository katmanındaki model dönüşümlerinin ve boş veri kontrollerinin test edilmesi.
*   **Dosyalar:**
    *   `app/src/test/java/com/scrollmeter/app/data/repository/ScrollRepositoryImplTest.kt`
*   **TDD / Geliştirme Adımları:**
    1.  Mock Dao'dan dönen sahte entity listesini ver.
    2.  Dönen domain modellerinin beklenen metrajlara ve isimlere sahip olduğunu doğrula.
*   **Kabul Kriteri:** Tüm mapper testleri %100 doğrulukla geçmelidir.

---

### Faz 28: Hilt Database Module Konfigürasyonu
*   **Amaç:** Room Database ve DAO nesnelerini Hilt DI konteynerine kaydetmek.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/di/DatabaseModule.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `provideDatabase(@ApplicationContext context: Context): ScrollMeterDatabase` metodunu yaz.
    2.  `provideScrollDao(database: ScrollMeterDatabase): ScrollDao` metodunu sağla.
*   **Kabul Kriteri:** Database SingletonComponent içinde tek bir örnek (Singleton) olarak sağlanmalıdır.

---

### Faz 29: Hilt Repository ve Provider Module Bağlantıları
*   **Amaç:** Interface-Implementation bağlantılarını `@Binds` ile performanslı olarak kurmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/di/RepositoryModule.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `@Binds bindScrollRepository(impl: ScrollRepositoryImpl): IScrollRepository`
    2.  `@Binds bindMetricsProvider(impl: DeviceMetricsProviderImpl): IDeviceMetricsProvider`
*   **Kabul Kriteri:** DI grafiği derlenmeli, tüm bağımlılıklar çözümlenmelidir.

---

### Faz 30: Uygulama İçi Loglama (Timber) Entegrasyonu
*   **Amaç:** Geliştirme aşamasında log takibi yapıp, prodüksiyon derlemesinde logları tamamen susturmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/core/logging/TimberInitializer.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `BuildConfig.DEBUG` kontrolü ile `Timber.plant(Timber.DebugTree())` ekle.
    2.  Release modunda boş bir tree (veya Crashlytics entegrasyonu) bırak.
*   **Kabul Kriteri:** Release APK çıktısında Logcat'e hiçbir kişisel veri veya metrik basılmamalıdır.

---

## BÖLÜM 4: Kritik Motor - Erişilebilirlik Servisi (Faz 31 - 42)

### Faz 31: Manifest İzinleri ve Servis Kaydı
*   **Amaç:** Android 14+ gereksinimlerine uygun olarak Erişilebilirlik ve Foreground Service izinlerini manifest'e işlemek.
*   **Dosyalar:**
    *   `app/src/main/AndroidManifest.xml`
*   **TDD / Geliştirme Adımları:**
    1.  `android.permission.BIND_ACCESSIBILITY_SERVICE` izniyle servisi kaydet.
    2.  `FOREGROUND_SERVICE` ve `FOREGROUND_SERVICE_SPECIAL_USE` izinlerini tanımla.
    3.  `android.permission.INTERNET` izninin **OLMADIĞINI** doğrula (Gizlilik kuralı).
*   **Kod Şablonu:**
    ```xml
    <manifest xmlns:android="http://schemas.android.com/apk/res/android">
        <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
        <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
        <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

        <application ...>
            <service
                android:name=".service.ScrollAccessibilityService"
                android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
                android:exported="true"
                android:foregroundServiceType="specialUse">
                <intent-filter>
                    <action android:name="android.accessibilityservice.AccessibilityService" />
                </intent-filter>
                <meta-data
                    android:name="android.accessibilityservice"
                    android:resource="@xml/accessibility_service_config" />
            </service>
        </application>
    </manifest>
    ```
*   **Kabul Kriteri:** Manifest geçerli olmalı, sistem izin yapılandırmaları tam olmalıdır.

---

### Faz 32: Servis Konfigürasyon XML'i ve Gizlilik Bayrakları
*   **Amaç:** Servisin yalnızca kaydırma olaylarını dinlemesini ve ekrandaki hiçbir metni okumamasını garanti eden XML yapılandırmasını oluşturmak.
*   **Dosyalar:**
    *   `app/src/main/res/xml/accessibility_service_config.xml`
*   **TDD / Geliştirme Adımları:**
    1.  `accessibilityEventTypes="typeViewScrolled"` olarak sınırla.
    2.  `canRetrieveWindowContent="false"` değerini sabitle.
*   **Kod Şablonu:**
    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
        android:accessibilityEventTypes="typeViewScrolled"
        android:accessibilityFeedbackType="feedbackGeneric"
        android:accessibilityFlags="flagDefault"
        android:canRetrieveWindowContent="false"
        android:canRequestTouchExplorationMode="false"
        android:notificationTimeout="50"
        android:description="@string/accessibility_service_description" />
    ```
*   **Kabul Kriteri:** Google Play Store güvenlik politikalarına tam uyum sağlanmalıdır.

---

### Faz 33: ScrollAccessibilityService Sınıf İskeleti
*   **Amaç:** `AccessibilityService` taban sınıfını genişleten ve Hilt bağımlılıklarını barındıran servis iskeletini oluşturmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/service/ScrollAccessibilityService.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `@AndroidEntryPoint` anotasyonu ile servisi işaretle.
    2.  `onServiceConnected`, `onAccessibilityEvent`, `onInterrupt`, `onDestroy` yaşam döngüsü metotlarını kur.
*   **Kabul Kriteri:** Servis başlatıldığında çökme olmadan sisteme bağlanmalıdır.

---

### Faz 34: Olay (Event) Verilerinin Güvenli Ayıklanması
*   **Amaç:** Gelen `AccessibilityEvent` nesnesinden güvenle deltaX, deltaY ve paket adını ayıklamak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/service/ScrollAccessibilityService.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED` kontrolü yap.
    2.  `scrollDeltaX` ve `scrollDeltaY` değerlerini oku. Null veya geçersiz paket isimlerini derhal filtrele.
*   **Kabul Kriteri:** NullPointerException riski tamamen sıfırlanmalıdır.

---

### Faz 35: Servis İçi Asenkron Akış (MutableSharedFlow)
*   **Amaç:** Ana thread'i bloke etmeden scroll verilerini mikro saniyeler içinde tampona fırlatmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/service/model/RawScrollEvent.kt`
    *   `app/src/main/java/com/scrollmeter/app/service/ScrollAccessibilityService.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `extraBufferCapacity = 1000` ve `BufferOverflow.DROP_OLDEST` ile `MutableSharedFlow<RawScrollEvent>` tanımla.
    2.  `onAccessibilityEvent` içinde `eventFlow.tryEmit(...)` çağırarak thread kilitlemesini önle.
*   **Kabul Kriteri:** Saniyede 500 event gelse dahi UI thread 0ms beklemelidir.

---

### Faz 36: Batching (Yığın İşleme) ve Debounce Mekanizması
*   **Amaç:** Tampondaki verileri 5 saniyede bir veya 20.000 pikselde bir gruplayıp veritabanına tek transaction ile yazan coroutine döngüsünü kurmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/service/batch/ScrollBatchCollector.kt`
*   **TDD / Geliştirme Adımları:**
    1.  Gelen eventleri paket bazında `Map<String, Double>` olarak topla.
    2.  Zaman veya hacim eşiği aşıldığında `repository.recordScrollBatch(...)` çağır.
*   **Kabul Kriteri:** Disk I/O yazımları saniyede 1 kezden daha aza indirilmelidir.

---

### Faz 37: Ekran Durumuna (Screen ON/OFF) Göre Tasarruf
*   **Amaç:** Ekran kapandığında servisi uyutarak batarya tüketimini %0.00'a indirmek.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/service/receiver/ScreenStateReceiver.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `ACTION_SCREEN_OFF` geldiğinde bekleyen verileri derhal diske yaz (flush) ve toplama Coroutine'ini duraklat.
    2.  `ACTION_SCREEN_ON` geldiğinde toplama döngüsünü yeniden başlat.
*   **Kabul Kriteri:** Ekran kapalıyken uygulamanın CPU kullanımı sıfır olmalıdır.

---

### Faz 38: Ön Plan Servisi (Foreground Service) ve Bildirim
*   **Amaç:** Sistemin servisi arka planda öldürmesini önleyen düşük öncelikli bildirim mekanizmasını oluşturmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/service/notification/ScrollNotificationManager.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `IMPORTANCE_LOW` ile sessiz bildirim kanalı yarat.
    2.  `startForeground(NOTIFICATION_ID, notification)` çağır.
*   **Kabul Kriteri:** Bildirim ses veya titreşim yapmadan kalıcı olarak görünmelidir.

---

### Faz 39: Hızlı Sıçrama ve Harita Bypass Logiği
*   **Amaç:** Harita yakınlaştırma veya fotoğraf galerisi geçişlerindeki aşırı anomalileri elemek.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/service/filter/ScrollGestureFilter.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `ScrollAnomalyFilter` kuralını servis seviyesinde işlet.
    2.  Tekil olayda 5000px üzeri sıçramaları sessizce çöpe at.
*   **Kabul Kriteri:** Harita açıldığında sayaçta gerçek dışı fırlamalar yaşanmamalıdır.

---

### Faz 40: Sistem Arayüzü İstisnaları (SystemUI Blacklisting)
*   **Amaç:** Bildirim çekmecesini indirme gibi sistem hareketlerinin sayaca eklenmesini engellemek.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/service/filter/PackageBlacklist.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `"com.android.systemui"`, varsayılan launcher paketleri gibi sistem paketlerini kara listeye al.
    2.  Bu paketlerden gelen eventleri filtrele.
*   **Kabul Kriteri:** Sistem içi gezinmeler toplam mesafeyi kirletmemelidir.

---

### Faz 41: Servis İçi Hata Yakalama (Crash Prevention)
*   **Amaç:** Servisin hiçbir istisna durumunda çökmemesini garanti eden "Never Crash" mekanizmasını kurmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/service/ScrollAccessibilityService.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `CoroutineExceptionHandler` ile tüm scope'ları koru.
    2.  `onAccessibilityEvent` gövdesini geniş `try-catch` ile sarmala.
*   **Kabul Kriteri:** Servis kodunda fırlatılan bir exception servisi sonlandırmamalıdır.

---

### Faz 42: Servis İşlemleri İçin TDD (Robolectric ile Uçtan Uca)
*   **Amaç:** Servisin event toplama ve batch kaydetme davranışını simüle ederek doğrulamak.
*   **Dosyalar:**
    *   `app/src/test/java/com/scrollmeter/app/service/ScrollBatchCollectorTest.kt`
*   **TDD / Geliştirme Adımları:**
    1.  100 adet sahte scroll event'i fırlat.
    2.  Sanal zamanı 5 saniye ileri sar.
    3.  Repository `recordScrollBatch` metodunun 100 kere değil, yalnızca 1 kere toplu çağrıldığını doğrula.
*   **Kabul Kriteri:** Toplu yazım mantığı başarıyla onaylanmalıdır.

---

## BÖLÜM 5: Sunum Katmanı, MVI ve Jetpack Compose (Faz 43 - 54)

### Faz 43: Compose Tema ve Renk Paleti (Material Design 3)
*   **Amaç:** Modern, dinamik renk destekli Material Design 3 temasını oluşturmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/presentation/theme/Color.kt`
    *   `app/src/main/java/com/scrollmeter/app/presentation/theme/Type.kt`
    *   `app/src/main/java/com/scrollmeter/app/presentation/theme/Theme.kt`
*   **TDD / Geliştirme Adımları:**
    1.  Açık ve koyu tema için zümrüt yeşili/turkuaz vurgulu renk paletlerini tanımla.
    2.  Büyük mesafe metinleri için okunaklı tipografi tanımla.
*   **Kabul Kriteri:** `ScrollMeterTheme` dinamik renkleri ve karanlık modu hatasız desteklemelidir.

---

### Faz 44: Ekran Navigasyonu (Compose Navigation)
*   **Amaç:** Kotlinx Serialization ile tip-güvenli rotalar ve NavHost oluşturmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/presentation/navigation/Routes.kt`
    *   `app/src/main/java/com/scrollmeter/app/presentation/navigation/ScrollNavHost.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `@Serializable object HomeRoute`, `AnalyticsRoute`, `SettingsRoute`, `PermissionRoute` sınıflarını yaz.
    2.  NavHost içinde ekran rotalarını tanımla.
*   **Kabul Kriteri:** Rotalar string URL içermemeli, derleme anında tip-güvenli olmalıdır.

---

### Faz 45: İzin ve Onboarding Ekranı (Permission UI)
*   **Amaç:** Erişilebilirlik izninin neden gerekli olduğunu ve gizlilik garantilerini açıklayan karşılama ekranını çizmek.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/presentation/onboarding/PermissionScreen.kt`
*   **TDD / Geliştirme Adımları:**
    1.  "Şifreleriniz ve mesajlarınız asla okunamaz" güvence kartlarını yerleştir.
    2.  "İzin Ver" butonuna basıldığında `Settings.ACTION_ACCESSIBILITY_SETTINGS` intentini başlat.
*   **Kabul Kriteri:** İzin verilmeden ana ekrana geçiş engellenmelidir.

---

### Faz 46: Dashboard (Ana Ekran) State ve Intent Tanımları (MVI)
*   **Amaç:** Ana ekran durumunu ve kullanıcı niyetlerini mühürlü sınıflarla modellemek.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/presentation/dashboard/DashboardContract.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `@Immutable data class DashboardUiState` (toplamMesafe, metafor, ilerlemeYüzdesi, isLoading).
    2.  `sealed interface DashboardIntent` (Refresh, OnGoalClicked).
*   **Kabul Kriteri:** State tek bir immutable kaynaktan beslenmelidir.

---

### Faz 47: Dashboard ViewModel Entegrasyonu
*   **Amaç:** UseCase'lerden gelen verileri `StateFlow<DashboardUiState>` olarak sunan ViewModel'ı yazmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/presentation/dashboard/DashboardViewModel.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `GetDailyTotalDistanceUseCase` ve `CalculateMetaphorUseCase`'i inject et.
    2.  `stateIn(SharingStarted.WhileSubscribed(5000))` ile akışı UI state'ine bağla.
*   **Kabul Kriteri:** Ekran rotasyonlarında veri kaybı yaşanmamalıdır.

---

### Faz 48: TDD - Dashboard ViewModel Testi (Turbine)
*   **Amaç:** ViewModel'ın state geçişlerini Turbine ile doğrulamak.
*   **Dosyalar:**
    *   `app/src/test/java/com/scrollmeter/app/presentation/dashboard/DashboardViewModelTest.kt`
*   **TDD / Geliştirme Adımları:**
    1.  UseCase'den 100 metrelik sahte akış fırlat.
    2.  `viewModel.uiState.test { ... }` ile durumun `100.00 m` olarak güncellendiğini onayla.
*   **Kabul Kriteri:** Tüm state geçişleri başarıyla test edilmelidir.

---

### Faz 49: Dairesel İlerleme Çubuğu (Custom Compose UI)
*   **Amaç:** Günlük hedefe ulaşma oranını gösteren animasyonlu özel Canvas dairesel barını çizmek.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/presentation/components/CircularDistanceProgress.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `Canvas(modifier = ...)` içinde `drawArc` ile arka plan çemberini ve ilerleme yayını çiz.
    2.  İlerleme yayına `Brush.sweepGradient` ver ve `animateFloatAsState` ile yumuşak yay animasyonu uygula.
*   **Kabul Kriteri:** 60/120 FPS akıcılıkta çizilmeli ve gereksiz recomposition yapmamalıdır.

---

### Faz 50: Dinamik Metafor Kartları (Gamification UI)
*   **Amaç:** Kullanıcının mevcut mesafesine karşılık gelen oyunlaştırma kartını çizmek.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/presentation/components/MetaphorCard.kt`
*   **TDD / Geliştirme Adımları:**
    1.  Metafor simgesi, başlık ve "Eyfel Kulesine 45 metre kaldı!" gibi dinamik motivasyon metnini göster.
*   **Kabul Kriteri:** Kart tasarımı Material Design 3 Elevation ve Surface standartlarına uymalıdır.

---

### Faz 51: Analytics State ve ViewModel
*   **Amaç:** Uygulama bazlı detaylı istatistikleri yönetecek `AnalyticsViewModel`'i oluşturmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/presentation/analytics/AnalyticsContract.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `GetAppLeaderboardUseCase`'i bağla.
    2.  En çok kaydırılan uygulamalar listesini StateFlow olarak sun.
*   **Kabul Kriteri:** Veriler bellek dostu biçimde listelenmelidir.

---

### Faz 52: Çubuk Grafik (Bar Chart) Componenti
*   **Amaç:** Harici kütüphane kullanmadan Compose Canvas ile en çok kaydırılan 5 uygulamayı çubuk grafikte görselleştirmek.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/presentation/components/ScrollDistanceBarChart.kt`
*   **TDD / Geliştirme Adımları:**
    1.  Canvas üzerinde dinamik oransal sütun yükseklikleri hesapla.
    2.  Aşağıdan yukarıya yükselme animasyonu (`Animatable`) ekle.
*   **Kabul Kriteri:** Çizim donanım hızlandırmalı (Hardware Accelerated) olmalıdır.

---

### Faz 53: Uygulama Listesi (LazyColumn) ve İkon Renderlama
*   **Amaç:** Uygulama liderlik tablosunu yüksek performansla listeleyen `LazyColumn` bileşenini yazmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/presentation/analytics/components/AppLeaderboardList.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `items(list, key = { it.packageName })` ile kararlı listeleme yap.
    2.  Önbellekten çekilen uygulama ikonunu ve formatlanmış mesafeyi göster.
*   **Kabul Kriteri:** Hızlı kaydırmalarda takılma (jank) olmamalıdır.

---

### Faz 54: Ayarlar (Settings) ve Kalibrasyon UI
*   **Amaç:** Kullanıcının ekranına gerçek fiziksel cetvel koyup yazılımsal cetvelle eşleştirebileceği kalibrasyon ekranını çizmek.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/presentation/settings/SettingsScreen.kt`
*   **TDD / Geliştirme Adımları:**
    1.  Ekranda 100mm uzunluğunda sanal cetvel çiz.
    2.  Kullanıcının slider ile ayarladığı çarpanı DataStore'a kaydet.
*   **Kabul Kriteri:** Yeni kalibrasyon değeri anında tüm ölçümlere yansımalıdır.

---

## BÖLÜM 6: Üretim (Production) Hazırlığı, Optimizasyon ve Dağıtım (Faz 55 - 60)

### Faz 55: WorkManager ile Otomatik Veri Temizliği (Retention)
*   **Amaç:** 30 günden eski detay kayıtları silerek cihaz hafızasının şişmesini önleyen periyodik arka plan işçisini yazmak.
*   **Dosyalar:**
    *   `app/src/main/java/com/scrollmeter/app/data/worker/DataCleanupWorker.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `CoroutineWorker` sınıfından türet ve Hilt ile inject et.
    2.  Kısıtlamalar: `setRequiresCharging(true)`, `setRequiresDeviceIdle(true)`.
    3.  30 günden eski satırları silen SQL sorgusunu işlet.
*   **Kabul Kriteri:** Temizlik yalnızca gece şarjdayken ve cihaz boştayken çalışmalıdır.

---

### Faz 56: Uçtan Uca (E2E) Compose UI Testleri
*   **Amaç:** Ana kullanıcı senaryolarını UI otomasyon testleriyle doğrulamak.
*   **Dosyalar:**
    *   `app/src/androidTest/java/com/scrollmeter/app/presentation/ScrollMeterUiTest.kt`
*   **TDD / Geliştirme Adımları:**
    1.  `createAndroidComposeRule` kullanarak uygulamanın açılışını test et.
    2.  Dashboard'dan Analytics sekmesine geçişi ve grafiklerin belirdiğini `onNodeWithText` ile onayla.
*   **Kabul Kriteri:** UI testleri emülatörde hatasız tamamlanmalıdır.

---

### Faz 57: ProGuard ve R8 Obfuscation Kuralları
*   **Amaç:** APK boyutunu küçültmek ve kod güvenliğini sağlamak için R8 kurallarını yapılandırmak.
*   **Dosyalar:**
    *   `app/proguard-rules.pro`
*   **TDD / Geliştirme Adımları:**
    1.  Room, Hilt ve Kotlinx Serialization için gerekli `-keep` kurallarını ekle.
    2.  Release derlemesinde `minifyEnabled true` ve `shrinkResources true` aktif et.
*   **Kabul Kriteri:** Release APK küçülmeli ve açılışta çökmemelidir.

---

### Faz 58: Macrobenchmark ile Performans ve Jank Analizi
*   **Amaç:** Soğuk başlangıç süresini ve frame kayıplarını ölçen benchmark modülünü kurmak ve Baseline Profiles üretmek.
*   **Dosyalar:**
    *   `benchmark/src/main/java/com/scrollmeter/benchmark/BaselineProfileGenerator.kt`
    *   `benchmark/src/main/java/com/scrollmeter/benchmark/StartupBenchmark.kt`
*   **TDD / Geliştirme Adımları:**
    1.  Baseline Profile kural üreticisini çalıştır.
    2.  Soğuk başlangıç süresinin 400ms altında olduğunu raporla.
*   **Kabul Kriteri:** AOT (Ahead-of-Time) optimizasyonuyla açılış hızı %30 artmalıdır.

---

### Faz 59: Release Build, Keystore ve İmzalama İşlemleri
*   **Amaç:** Google Play Store'a yüklenecek imzalı AAB (Android App Bundle) paketini üretmek.
*   **Dosyalar:**
    *   `app/build.gradle.kts`
*   **TDD / Geliştirme Adımları:**
    1.  `signingConfigs.create("release")` bloğunu ortam değişkenleriyle (Environment Variables) yapılandır.
    2.  `./gradlew bundleRelease` komutunu çalıştır.
*   **Kabul Kriteri:** Geçerli ve imzalı `.aab` dosyası başarıyla üretilmelidir.

---

### Faz 60: Son İnceleme (Final Review) ve Dağıtım
*   **Amaç:** Sürekli Entegrasyon (CI) boru hattını kurmak ve son kalite onayını vermek.
*   **Dosyalar:**
    *   `.github/workflows/android_ci.yml`
*   **TDD / Geliştirme Adımları:**
    1.  Detekt kontrolü, Unit testler (`testDebugUnitTest`), Android lint ve `assembleRelease` adımlarını içeren GitHub Actions workflow'unu yaz.
    2.  Tüm testlerin yeşil olduğunu doğrula.
*   **Kabul Kriteri:** CI pipeline başarıyla tamamlanmalı, uygulama markete sürülmeye %100 hazır olmalıdır.
