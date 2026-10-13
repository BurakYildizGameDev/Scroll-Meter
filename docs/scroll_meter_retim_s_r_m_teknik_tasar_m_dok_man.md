# Teknik Tasarım Dokümanı (TDD / Technical Design Document): Scroll-Meter (Parmak Kilometresi)
**Sürüm:** 3.1 (Enterprise Production Architecture)  
**Kapsam:** Uçtan uca test edilmiş, sıfır batarya sızıntısı hedeflenmiş, Android 14/15 uyumlu, markete çıkmaya hazır reaktif Android uygulaması.  
**Hedef Platform:** Min SDK 28 (Android 9.0 Pie) — Target/Compile SDK 34/35 (Android 14/15)  
**Anahtar Teknolojiler:** Kotlin 2.0+, Jetpack Compose (BOM), Material Design 3, Dagger-Hilt, Room (KSP), Kotlin Coroutines & Flow, Jetpack Preferences DataStore, WorkManager, Jetpack Navigation Compose (Type-Safe), Macrobenchmark, JUnit 5, MockK, Turbine.

---

## 1. Proje Vizyonu ve Mimari Felsefe

### 1.1 Üretim Sürümü Yaklaşımı (Production-Ready) & Sistem KPI'ları
Scroll-Meter, kullanıcının gün boyunca cihaz ekranında parmağıyla kat ettiği fiziksel mesafeyi (milimetre, metre, kilometre) piksel düzeyinde izleyen, bunu arka planda fark edilmeyecek kadar düşük kaynak tüketimiyle hesaplayan ve eğlenceli oyunlaştırma metaforlarıyla sunan kurumsal seviyede bir Android uygulamasıdır.

Üretim sürümünün temel başarı metrikleri (Key Performance Indicators - KPI):
*   **CPU Tüketimi:** Ekran kaydırma esnasında dahi toplam CPU tüketimi %0.5'in altında kalmalıdır. Ekran kapalıyken (Screen OFF) CPU tüketimi kesinlikle %0.00 olmalıdır.
*   **Batarya Tüketimi:** 24 saatlik aralıksız kullanımda toplam pil tüketimi cihaz bataryasının en fazla %1.5'i kadar olmalıdır.
*   **Bellek (RAM) Ayak İzi:** Uygulama servisinin arka plan çalışma anındaki bellek tüketimi 35 MB sınırını asla aşmamalıdır.
*   **ANR (Application Not Responding) & Çökme Oranı:** %0.00 sıfır tolerans. Erişilebilirlik servisinin çökmesi sistem tarafından servisin kapatılmasına yol açacağından, servis kodunda hiçbir koşulda unhandled exception fırlatılamaz.
*   **Soğuk Başlangıç (Cold Start):** Material Design 3 Compose arayüzünün ilk açılış süresi 400 milisaniyenin altında olmalıdır.

### 1.2 Katı Clean Architecture ve Paket Ayrıştırma (Decoupling)
Uygulama, Bağımlılık Kuralı'na (Dependency Rule) tavizsiz bağlı olan 4 ana katmandan oluşur. Dış katmanlar iç katmanları bilebilir, ancak iç katmanlar dış katmanların hiçbir ayrıntısından haberdar olamaz.

```
                    ┌─────────────────────────┐
                    │   Presentation Layer    │
                    │ (Compose, MVI, ViewModel)│
                    └───────────┬─────────────┘
                                │ depends on
                                ▼
┌──────────────────┐    ┌─────────────────────────┐    ┌──────────────────────────┐
│  Service Layer   │───▶│      Domain Layer       │◀───│        Data Layer        │
│(Accessibility,   │    │  (UseCases, Calculator, │    │  (Room, DataStore, Repo, │
│ Screen Receiver) │    │  Models, Pure Business) │    │   PackageManager Cache)  │
└──────────────────┘    └─────────────────────────┘    └──────────────────────────┘
```

1.  **Domain Katmanı (Saf Kotlin):**
    *   Kesinlikle hiçbir Android SDK bağımlılığı (`Context`, `DisplayMetrics`, `Bundle` vb.) barındırmaz.
    *   Saf iş mantığını, matematiksel formülleri (`DistanceCalculator`), modelleri (`PhysicalDistance`, `AppScrollSession`) ve repository arayüzlerini (`IScrollRepository`, `IDeviceMetricsProvider`) içerir.
    *   Bu katmandaki kodlar %100 saf JVM Unit Testleri ile (Robolectric veya Emülatör olmadan) mikrosaniyeler içinde test edilebilir.
2.  **Data Katmanı:**
    *   Veritabanı (`ScrollMeterDatabase`, `DailyAppScrollEntity`, `ScrollDao`), `DataStore`, `AppNameResolver` (PackageManager önbelleği) ve `ScrollRepositoryImpl` sınıflarını barındırır.
    *   Veritabanı modellerini Domain modellerine dönüştüren mapper sınıflarını işletir.
3.  **Service Katmanı:**
    *   Android'in `AccessibilityService`, `BroadcastReceiver` ve `Foreground Service` bileşenlerini yönetir.
    *   Kullanıcı hareketlerini dinler, filtreler, ara belleğe (buffer) alır ve Domain/Data katmanlarına aktarır.
4.  **Presentation Katmanı:**
    *   Jetpack Compose UI bileşenleri, tema tanımları, ekranlar (`DashboardScreen`, `AnalyticsScreen`, `SettingsScreen`, `PermissionScreen`) ve `ViewModel` sınıflarından oluşur.

### 1.3 Reaktif Veri Akışı (MVI & Unidirectional Data Flow)
Arayüz ile veri tabanı arasındaki tüm haberleşme Reaktif (Tepkisel) ve Tek Yönlü Veri Akışı (Unidirectional Data Flow) prensibiyle çalışır:
*   **State (Durum):** Ekranda görünen her durum tek bir immutable data class ile temsil edilir (örn. `DashboardUiState`).
*   **Intent (Kullanıcı Niyeti):** Kullanıcının yaptığı tüm aksiyonlar birer mühürlü sınıf (sealed class) olarak ViewModel'a aktarılır (örn. `DashboardIntent.Refresh`, `DashboardIntent.UpdateDailyGoal`).
*   **Side-Effect (Tek Seferlik Olaylar):** Toast mesajları, navigasyon tetikleyicileri gibi tek seferlik aksiyonlar `Channel` üzerinden `receiveAsFlow` ile aktarılır.
*   **Flow Tabanlı Canlı Akış:** Room DAO'dan fırlatılan `Flow<Long>` doğrudan ViewModel'e akar. Kullanıcı ekranı açıkken arka planda kaydırma yapıldığında, arayüzdeki sayaç anında ve kendiliğinden (recomposition ile) artar.

---

## 2. Çekirdek Motor: Gelişmiş Erişilebilirlik Servisi (Accessibility Service)

### 2.1 Sistem Yükünü Minimize Eden Filtreleme Hattı (Filter Pipeline)
`AccessibilityService`, işletim sisteminin en yoğun olay üreten mekanizmalarından biridir. Kaydırma esnasında saniyede yüzlerce event üretilebilir. Çekirdek motor bu yükü 4 aşamalı bir filtre hattıyla eler:

```
[Android OS Event Stream]
         │
         ▼
[1. Event Type Check] ──(Değilse DROP)──> Yalnızca TYPE_VIEW_SCROLLED
         │
         ▼
[2. Package Blacklist] ──(Sistem arayüzüyse DROP)──> com.android.systemui, Launcher vb.
         │
         ▼
[3. Micro-Jitter Filter] ──(sqrt(dx²+dy²) < 3px ise DROP)──> Dokunmatik ekran paraziti engelleme
         │
         ▼
[4. Anomaly Guard] ──(Tekil delta > 5000px ise DROP)──> Multi-touch / Ekran yönü değişimi sıçraması
         │
         ▼
[In-Memory Buffer (MutableSharedFlow)]
```

*   **Micro-Jitter Eşiği:** Parmak ekranda hareketsiz dururken bile kapasitif ekranlar ±1-2 piksellik elektriksel mikro titremeler raporlayabilir. Bu titremeler $\Delta x^2 + \Delta y^2 < 9$ ise derhal elenir.
*   **Pinch-to-Zoom & Multi-touch Rejection:** İki parmakla yapılan zoom hareketlerinde oluşan zıt vektörler ve orantısız sıçramalar servisin anomali koruma filtresine takılır.

### 2.2 Bellek İçi Ara Belleğe Alma (In-Memory Buffering) ve Debounced Batching
Gelen her geçerli piksel hareketini doğrudan SQLite'a yazmak, disk I/O darboğazına, flash bellek aşınmasına ve pilin hızla tükenmesine yol açar. Bu nedenle Scroll-Meter çift katmanlı bir tamponlama (buffering) stratejisi kullanır:

1.  **Hızlı Tampon (Fast Buffer):** Servis gelen ham veriyi (`RawScrollEvent(packageName, deltaX, deltaY, timestamp)`) derhal yüksek kapasiteli bir `MutableSharedFlow` içerisine fırlatır (`extraBufferCapacity = 1000, onBufferOverflow = BufferOverflow.DROP_OLDEST`). Bu işlem Nano-saniye seviyesinde tamamlanır ve UI/Main thread asla bloklanmaz.
2.  **Yığın İşleme (Batch Engine):** Arka planda `IO` dispatcher üzerinde koşan özel bir coroutine toplama döngüsü çalışır. Bu döngü verileri iki tetikleyiciden biri gerçekleşene kadar toplar:
    *   **Zaman Tetikleyicisi:** Son veri tabanı yazımının üzerinden 5000 milisaniye (5 saniye) geçmesi.
    *   **Hacim Tetikleyicisi:** Tamponda biriken toplam piksel miktarının 20.000 pikseli aşması.
3.  **Tekil Atomik Yazım:** Eşik aşıldığında, toplanan veriler paket adına göre gruplanır (`Map<String, Long>`) ve Room Database'e tek bir `@Transaction` içerisinde tek seferde kaydedilir.

### 2.3 Arka Plan Yaşam Döngüsü ve Ekran Durumu (Screen ON/OFF) Yönetimi
Ekran kapalıyken kullanıcının ekranda kaydırma yapması fiziksel olarak imkansızdır. Bu durumdan faydalanarak:
*   Servis içinde dinamik olarak kaydedilen bir `BroadcastReceiver`, `Intent.ACTION_SCREEN_OFF` ve `Intent.ACTION_SCREEN_ON` olaylarını dinler.
*   **Screen OFF:** Tetiklendiğinde, mevcut tamponda bekleyen tüm veriler anında veritabanına boşaltılır (flush) ve ardından tamponu dinleyen Coroutine Job'ı duraklatılır/iptal edilir. CPU tüketimi mutlak %0'a indirilir.
*   **Screen ON:** Coroutine döngüsü yeniden başlatılır ve hazır bekler.

### 2.4 Ön Plan Servisi (Foreground Service) ve Bildirim Mimarisi
Android 14 (API 34) ve sonrası sistemlerde arka planda çalışan servislerin işletim sistemi tarafından sonlandırılmasını (Low Memory Killer - LMK) engellemek için:
*   `ScrollAccessibilityService`, başlatıldığında bir Foreground Service bildirimi yayınlar.
*   Bildirim Kanalı: `IMPORTANCE_LOW` (Kullanıcıyı ses veya titreşimle rahatsız etmeyen sessiz kanal).
*   Bildirim Özellikleri: `ONGOING` (Kullanıcı tarafından kaydırılarak silinemez), küçük ikon, dinamik metin ("Bugünkü kaydırma mesafeniz: 42.5 metre").
*   Bildirime tıklandığında uygulamanın ana ekranına yönlendiren `PendingIntent` bağlanır.
*   Manifest'te `FOREGROUND_SERVICE_SPECIAL_USE` veya `dataSync` servis tipi tanımlanır.

### 2.5 Hata Yakalama ve Never-Crash (Asla Çökme) Garantisi
Android'de bir Erişilebilirlik Servisi çökerse, işletim sistemi servisi güvenlik gerekçesiyle "Devre Dışı" (Disabled) duruma getirir. Kullanıcının sistem ayarlarına gidip servisi elle tekrar açması gerekir ki bu durum kullanıcı kaybına sebep olur.
*   Servisin tüm metotları (`onAccessibilityEvent`, `onServiceConnected`) kapsamlı `try-catch` blokları ile korunur.
*   Tüm coroutine'ler `SupervisorJob() + CoroutineExceptionHandler` ile başlatılır.
*   Yakalanan hatalar sessizce `Timber.e(throwable)` ile loglanır ve servis çalışmaya devam eder.

---

## 3. Veri Katmanı (Data Layer) ve Yüksek Performanslı Depolama

### 3.1 Room Database Normalizasyonu ve Şema Mimarisi
Veritabanında her kaydırma olayını ayrı bir satır olarak saklamak birkaç ay içinde milyonlarca satıra ve yüzlerce megabaytlık veritabanı boyutuna ulaşır. Scroll-Meter, birleşik anahtarlı (Composite Primary Key) normalize bir şema kullanır.

#### `DailyAppScrollEntity` Tablosu
```sql
CREATE TABLE IF NOT EXISTS daily_app_scroll (
    dateEpochDays INTEGER NOT NULL,
    packageName TEXT NOT NULL,
    totalPixels REAL NOT NULL DEFAULT 0.0,
    totalMilliMeters REAL NOT NULL DEFAULT 0.0,
    lastUpdatedTimestamp INTEGER NOT NULL,
    PRIMARY KEY (dateEpochDays, packageName)
);

CREATE INDEX IF NOT EXISTS idx_scroll_date ON daily_app_scroll (dateEpochDays);
CREATE INDEX IF NOT EXISTS idx_scroll_package ON daily_app_scroll (packageName);
```

*   `dateEpochDays`: `LocalDate.now().toEpochDay()` değeri (1970'ten bu yana geçen gün sayısı - örn: 20008). Tarih aramaları tamsayı (Integer) karşılaştırması olduğu için anında sonuç verir.
*   `packageName`: Uygulamanın benzersiz paket adı (örn: `com.instagram.android`).
*   Bu tablo yapısı sayesinde 100 farklı uygulama her gün kullanılsa bile yılda en fazla $365 \times 100 = 36.500$ satır oluşur; bu da SQLite için 2-3 megabaytı aşmayan önemsiz bir boyuttur.

### 3.2 Atomik SQLite Upsert Stratejisi
SQLite'ın `ON CONFLICT DO UPDATE` yeteneği kullanılarak, disk kilitlemelerini (lock contention) ortadan kaldıran tekil atomik sorgu işletilir:

```sql
INSERT INTO daily_app_scroll (dateEpochDays, packageName, totalPixels, totalMilliMeters, lastUpdatedTimestamp)
VALUES (:dateEpochDays, :packageName, :pixelsToAdd, :mmToAdd, :timestamp)
ON CONFLICT(dateEpochDays, packageName) DO UPDATE SET
    totalPixels = daily_app_scroll.totalPixels + excluded.totalPixels,
    totalMilliMeters = daily_app_scroll.totalMilliMeters + excluded.totalMilliMeters,
    lastUpdatedTimestamp = excluded.lastUpdatedTimestamp;
```

Bu yöntem sayesinde "Önce veriyi oku, varsa üstüne ekle güncelle, yoksa yeni satır aç" şeklindeki iki aşamalı (SELECT + UPDATE/INSERT) yavaş ve yarış durumuna (race condition) açık mimari tamamen bertaraf edilir.

### 3.3 Veri Saklama (Data Retention) ve Agregasyon Politikası
*   **WorkManager ile Periyodik Temizlik (`DataCleanupWorker`):**
    *   Çalışma Sıklığı: 24 saatte bir (PeriodicWorkRequestBuilder).
    *   Çalışma Kısıtlamaları (Constraints): `RequiresCharging(true)`, `RequiresDeviceIdle(true)`, `BatteryNotLow(true)`. Cihaz yalnızca gece şarja takılı ve boştayken tetiklenir.
    *   Mantık: 30 günden eski satırları `daily_app_scroll` tablosundan siler.
    *   İsteğe bağlı olarak silinen veriler, ay bazında özet tutan `MonthlyArchiveEntity` tablosuna kümülatif olarak aktarılır; böylece kullanıcının geçmiş yıl istatistikleri kaybolmaz.

### 3.4 DataStore Preferences Mimarisi
Hafif ayarlar, reaktif ve tip-güvenli olan Jetpack DataStore Preferences ile yönetilir.
Saklanan Temel Anahtarlar:
*   `KEY_MANUAL_PPI_MULTIPLIER`: Float (Varsayılan: 1.0f - Manuel kalibrasyon katsayısı)
*   `KEY_DAILY_GOAL_METERS`: Float (Varsayılan: 100.0f - Günlük hedef metre)
*   `KEY_THEME_MODE`: Int (0: Sistem, 1: Açık, 2: Koyu)
*   `KEY_BLACKLISTED_PACKAGES`: Set<String> (Takip edilmeyecek uygulamalar)
*   `KEY_IS_ONBOARDING_COMPLETED`: Boolean

### 3.5 Memory Caching (Uygulama İsim ve İkon Önbelleği)
`PackageManager.getApplicationInfo` ve `loadIcon` çağrıları Android işletim sisteminde Binder IPC (Inter-Process Communication) üzerinden çalışır ve oldukça yavaştır. Bir liste içerisinde onlarca kez çağrılması jank'e (arayüz takılmasına) neden olur.
*   `AppNameResolver` sınıfı, bellek içi bir `LruCache<String, AppMetadata>` barındırır.
*   Bir uygulamanın adı ve ikonu sistemden yalnızca bir kez çözülür ve RAM önbelleğinde tutulur. Sonraki tüm çağrılar 0 gecikmeyle bellekten döndürülür.

---

## 4. Matematiksel Motor ve Donanım Kalibrasyonu

### 4.1 Pisagor Teoremi ve Vektörel Piksel Hesaplaması
Kullanıcı parmağını ekranda hareket ettirdiğinde işletim sistemi X ve Y eksenlerindeki yer değiştirmeleri ($\Delta x, \Delta y$) raporlar. Çoğu kaydırma hareketi tam dikey veya tam yatay değil, hafif kavisli veya çaprazdır.

$$\text{Piksel Hipotenüsü } (\Delta d) = \sqrt{\Delta x^2 + \Delta y^2}$$

*   Hesaplamalar `Double` duyarlığında yapılır.
*   Eğer $\Delta x = 0$ ise doğrudan $|\Delta y|$, $\Delta y = 0$ ise doğrudan $|\Delta x|$ alınarak karekök alma işleminin CPU maliyeti optimize edilir.
*   Sıfırın altında negatif delta değerleri gelebileceğinden matematik motoru mutlaka skaler mutlak büyüklüklerle çalışır.

### 4.2 Donanım Çeşitliliği ve Dinamik PPI Kalibrasyonu
Android dünyasında ekran boyutları ve piksel yoğunlukları (PPI - Pixels Per Inch) cihazdan cihaza büyük farklılıklar gösterir. 1000 piksellik bir kaydırma 6.7 inçlik 1440p bir ekranda birkaç santimetreye denk gelirken, 10 inçlik 800p bir tablette ekranın yarısını kaplayabilir.

Fiziksel mesafeyi doğru hesaplamak için formül:

$$\text{Fiziksel Mesafe (inç)} = \frac{\Delta d}{\text{PPI}_{\text{efektif}}}$$
$$\text{Fiziksel Mesafe (mm)} = \text{Fiziksel Mesafe (inç)} \times 25.4 = \frac{\Delta d \times 25.4}{\text{PPI}_{\text{efektif}}}$$

Burada $\text{PPI}_{\text{efektif}}$:
$$\text{PPI}_{\text{donanım}} = \frac{xdpi + ydpi}{2}$$
$$\text{PPI}_{\text{efektif}} = \text{PPI}_{\text{donanım}} \times \text{Kalibrasyon Çarpanı } (k)$$

*   `xdpi` ve `ydpi`, `Resources.getSystem().displayMetrics` üzerinden donanımdan okunur.
*   Bazı üreticiler (özellikle giriş seviyesi cihazlarda) displayMetrics değerlerini hatalı bildirebilir. Bu durum Bölüm 4.4'teki kalibrasyon ile çözülür.

### 4.3 Donanımsal Anomali Dedektörü (Anomaly Detection Filter)
Bazı uygulamalarda harita yakınlaştırma (Google Maps), hızlı sayfa atlama (ViewPager scroll) veya ekran yönü değiştirme (rotasyon) esnasında işletim sistemi tek bir olayda binlerce piksellik sıçramalar üretebilir.
*   **Anomali Kuralı:** Tek bir kaydırma olayındaki piksel deltası, ekranın dikey piksel çözünürlüğünün %70'ini aşıyorsa ($\Delta d > 0.70 \times \text{ScreenHeight}$ veya $\Delta d > 3000\text{ px}$), bu olay bir kullanıcı kaydırması değil, donanımsal/yazılımsal bir atlama kabul edilir ve filtre tarafından yutularak loglanır.

### 4.4 Etkileşimli Manuel Kalibrasyon Algoritması
Kullanıcı Ayarlar ekranında "Cetvel Kalibrasyonu" modunu açtığında:
1.  Ekranda tam 100 mm (10 cm) uzunluğunda olması hedeflenen bir sanal cetvel çizgisi çizilir:
    $$\text{Hedef Çizgi Pikselleri} = \frac{100 \times \text{PPI}_{\text{donanım}}}{25.4}$$
2.  Kullanıcı gerçek fiziksel bir cetveli ekranın üzerine koyar ve ekrandaki slider ile sanal cetvelin iki ucunu fiziksel cetveldeki 10 cm çizgisine eşitler.
3.  Oluşan kullanıcı oranı üzerinden yeni kalibrasyon katsayısı ($k$) hesaplanır:
    $$k = \frac{\text{Kullanıcının Ayarladığı Piksel Genişliği}}{\text{Hedef Piksel Genişliği}}$$
4.  Bu katsayı DataStore'a kaydedilir ve sonraki tüm ölçümlere dinamik çarpan olarak uygulanır.

---

## 5. Kullanıcı Arayüzü (Jetpack Compose) ve Oyunlaştırma (Gamification)

### 5.1 Material Design 3 Tasarım Sistemi ve Temalama
*   **Renk Paleti:** Android 12+ dinamik renk desteği (`dynamicDarkColorScheme`, `dynamicLightColorScheme`), eski sürümler için özel hazırlanmış derin lacivert/mor gece teması ve modern zümrüt yeşili/turkuaz vurgu renkleri.
*   **Tipografi:** Büyük mesafe rakamları için okunabilir, modern ve monospace uyumlu font düzeni. Metrik değerler için `HeadlineLarge` ve `DisplayLarge` stilleri.
*   **Erişilebilirlik:** Minimum 48dp tıklanabilir alanlar, yüksek kontrast standartları (WCAG AA uyumlu) ve TalkBack içerik açıklamaları (`contentDescription`).

### 5.2 Compose Recomposition Optimizasyonu
Arayüzün 60/120 FPS akıcılıkta kalması ve gereksiz yere baştan çizilmemesi (recomposition) için:
*   Tüm UI durum modelleri `@Immutable` veya `@Stable` anotasyonları ile işaretlenir.
*   Sık değişen değerler (örneğin kaydırma animasyonları veya anlık piksel güncellemeleri) için `derivedStateOf` ve lambdalar (`{ state.value }`) kullanılır; böylece üst Composable'lar çizilmeden yalnızca alt Canvas/Text güncellenir.
*   Listelerde (`LazyColumn`) her bir öğeye kararlı ve benzersiz `key` atanır (`key = { it.packageName }`).

### 5.3 Custom Canvas Animasyonları
1.  **Dairesel İlerleme Çubuğu (Circular Progress Indicator):**
    *   Compose `Canvas` API'si ve `drawArc` kullanılarak sıfırdan çizilir.
    *   Arka plan izi (track) ve ön plan ilerleme çizgisi.
    *   Ön plan çizgisi `Brush.sweepGradient` ile renklendirilir.
    *   Hedefe ulaşma yüzdesi `animateFloatAsState` ile yay animasyonu (spring/tween) şeklinde yumuşakça hareket eder.
2.  **Compose Bar Chart (Uygulama Kullanım Çubuk Grafiği):**
    *   Harici ağır kütüphaneler (MPAndroidChart vb.) kullanılmaz; saf Compose Canvas ile çizilir.
    *   En çok kaydırılan ilk 5 uygulamanın mesafelerini oransal yüksekliklerle dinamik barlara dönüştürür.
    *   Her barın altında uygulama ikonu ve üzerinde mesafe metni yer alır. Barlar ekrana ilk girişte aşağıdan yukarıya yumuşakça yükselen animasyona sahiptir.

### 5.4 Metafor ve Oyunlaştırma Motoru (Gamification Engine)
Kullanıcının parmakla kat ettiği mesafeyi içselleştirmesi için soyut rakamlar somut metaforlara dönüştürülür:

| Mesafe Aralığı | Metafor Adı | İkon / Görsel | Motivasyonel Mesaj |
| :--- | :--- | :--- | :--- |
| **0 - 5 Metre** | 🐜 Karınca Parkuru | Karınca | "Güne yeni başladın, parmağın henüz ısınıyor!" |
| **5 - 15 Metre** | 🦒 Zürafa Boyu | Zürafa | "Tebrikler! Bir zürafanın boyu kadar kaydırdın!" |
| **15 - 50 Metre** | 🚌 Belediye Otobüsü | Otobüs | "Parmağın bir otobüs boyu yol kat etti!" |
| **50 - 100 Metre** | ⚽ Futbol Sahası | Futbol Topu | "Neredeyse bir futbol sahasını boydan boya koştun!" |
| **100 - 300 Metre** | 🗽 Özgürlük Heykeli | Heykel | "Özgürlük Heykeli'nin meşalesine ulaştın!" |
| **300 - 828 Metre** | 🗼 Eyfel Kulesi | Eyfel | "Eyfel Kulesi'nin zirvesindesin! Muhteşem manzara!" |
| **828 - 2000 Metre** | 🏙️ Burj Khalifa | Gökdelen | "Dünyanın en yüksek binasını parmağınla fethettin!" |
| **8848+ Metre** | 🏔️ Everest Zirvesi | Dağ | "İnanılmaz! Parmağın Everest Dağı'nın zirvesinde!" |

Presentation katmanındaki `CalculateMetaphorUseCase`, mevcut toplam metre değerini alır ve en yakın metaforu, bir sonraki hedefe kalan mesafeyi ve ilerleme yüzdesini hesaplayarak UI kartına iletir.

### 5.5 Tip-Güvenli Ekran Navigasyonu (Type-Safe Navigation)
AndroidX Navigation 2.8+ ile gelen Kotlinx Serialization tabanlı Type-Safe rotalar kullanılır:
```kotlin
@Serializable object HomeRoute
@Serializable object AnalyticsRoute
@Serializable object SettingsRoute
@Serializable object PermissionRoute
```
String tabanlı URL eşleştirmelerindeki yazım hataları ve runtime çökmeleri tamamen engellenir.

---

## 6. Güvenlik, Gizlilik, Pil ve Test (TDD) Standartları

### 6.1 Sıfır Bilgi (Zero-Knowledge) Gizlilik Mimarisi
Erişilebilirlik servisleri kullanıcılar ve Google Play denetçileri açısından en hassas servis türleridir.
Uygulamanın Play Store onayını sorunsuz alabilmesi için:
*   `res/xml/accessibility_service_config.xml` dosyasında:
    *   `android:canRetrieveWindowContent="false"`: Uygulama ekrandaki hiçbir metni, şifreyi, banka bilgisini veya mesajı OKUYAMAZ.
    *   `android:canRequestTouchExplorationMode="false"`: Ekrana dokunma simülasyonu yapamaz.
    *   `android:accessibilityEventTypes="typeViewScrolled"`: Yalnızca kaydırma olaylarını dinler; tuş basımlarını dinleyemez.
*   **İnternet İzni Yoktur (No Internet Permission):** `AndroidManifest.xml` dosyasında `android.permission.INTERNET` izni bulunmaz! Veriler cihazdan dışarıya teknik olarak SIZDIRILAMAZ. Bu durum en güçlü pazarlama ve güven argümanıdır.

### 6.2 Batarya ve Kaynak Koruma İlkeleri
*   `WakeLock` kullanımı kesinlikle yasaktır. Cihaz uykuya geçtiğinde uygulamanın işlemciyi uyanık tutmasına izin verilmez.
*   Arka plan görevleri için `AlarmManager` yerine sistemle optimize çalışan `WorkManager` kullanılır.
*   Ekran kapalıyken Coroutine döngüleri durdurulur (0 CPU kullanımı).

### 6.3 ProGuard ve R8 Kuralları
Release derlemelerinde kod boyutunu küçültmek ve tersine mühendisliği önlemek için:
*   `minifyEnabled true` ve `shrinkResources true` etkinleştirilir.
*   Room şemaları, Hilt oluşturulan sınıfları ve Serialization veri sınıfları için koruma (`-keep`) kuralları eklenir.

### 6.4 Test Piramidi ve Kalite Kriterleri

```
          / \
         /   \        Uçtan Uca UI & Macrobenchmark (5%)
        /     \       - Compose UI Tests & Baseline Profiles
       /───────\
      /         \     Entegrasyon & Veritabanı Testleri (20%)
     /           \    - Room In-Memory DB, Robolectric Service Testleri
    /─────────────\
   /               \  Birim Testleri (Unit Tests) (75%)
  /                 \ - Pure Kotlin Math Tests, UseCase MockK, Turbine Flow Tests
 /───────────────────\
```

1.  **Birim Testleri (JUnit 5 + MockK + Turbine):**
    *   Matematik motorunun sınır değerleri: negatif piksel, sıfır piksel, devasa deltalar.
    *   UseCase akış testleri: Verilerin sıralanması, filtrelerin doğrulanması.
    *   Hedef Kapsam (Coverage): Domain katmanı için %100, Data katmanı iş mantığı için %85+.
2.  **Veritabanı Entegrasyon Testleri:**
    *   `Room.inMemoryDatabaseBuilder` ile SQLite'ın bellek içi çalıştırılması.
    *   Upsert mantığının ve conflict yönetiminin doğrulanması.
3.  **UI ve Görsel Doğrulama Testleri:**
    *   `createComposeRule` ile ana ekran kartlarının ve dairesel barın çizimlerinin testi.
4.  **Performans & Benchmark Testleri:**
    *   `androidx.benchmark.macro` kütüphanesi ile Cold Start ve Frame Jank testleri.
    *   Uygulama için optimize edilmiş `BaselineProfile` üretilmesi.
