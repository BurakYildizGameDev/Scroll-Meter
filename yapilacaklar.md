# Scroll-Meter: Gelecek Geliştirmeler ve Yol Haritası (yapilacaklar.md)

**Tarih:** 13 Ekim 2026  
**Durum:** Planlama & Tasarım Aşaması (Feature Backlog)  
**Proje:** Scroll-Meter (Android Physical Telemetry & Digital Well-being)  
**Tasarım Dili:** Cyber Luxury & Mineral Glow (Material Design 3 Compose)  

---

## 🎯 Vizyon ve Öncelik Sıralaması

Scroll-Meter'ın sağlam Clean Architecture temeli, 120Hz/144Hz LTPO motoru ve kupa odası üzerine inşa edilecek, hem dijital farkındalığı zirveye taşıyacak hem de sosyal medyada viral etkileşim yaratacak özellik havuzudur.

---

## 🚨 1. [KULLANICI ÖZEL İSTEĞİ] Aşırı Kaydırma Uyarısı / Doomscroll Tavan Alarmı
> *"Çok fazla kaydırma mevcut, günde hani yeter artık doomscroll yapma gibi..."*

### Özellik Açıklaması
Kullanıcı farkında olmadan saatlerce sosyal medyada kaydırma yaptığında veya günlük kritik eşiğe ulaştığında, dikkat çekici, esprili ve sert bir farkındalık uyarısı tetiklenecektir.

### Teknik ve Fonksiyonel Detaylar
* **Kritik Eşik Tespiti (Doomscroll Ceiling):**
  * Kullanıcı Ayarlar ekranından kendi "Tavan Mesafesini" belirleyebilir (Varsayılan: `500 metre` veya `1.000 metre`).
  * Alternatif olarak **Aralıksız Kaydırma Tespiti (Binge Scrolling Detector):** Kullanıcı aralıksız 15 dakika boyunca uygulamadan çıkmadan kaydırma yaptığında tetiklenir.
* **Uyarı Bildirimi ve Diyalog:**
  * **Yüksek Öncelikli Bildirim (Heads-up Notification):**
    > 🚨 **ÇOK FAZLA KAYDIRMA TESPİT EDİLDİ!**  
    > *"Bugün başparmağın 1.200 metre yol katetti! Yeter artık doomscroll yapma, telefonunu masaya bırak ve parmaklarını dinlendir."*
  * **Uygulama İçi Tam Ekran Uyarı Kartı:** Kullanıcı uygulamayı açtığında neon kırmızı/amber renkli farkındalık uyarısı.
* **Özelleştirilebilir Seviyeler:**
  * `Hafif Uyarı (Dürtme):` Hedef %100 aşıldığında.
  * `Sert Alarm (Doomscroll Molası):` Hedef %200 aşıldığında telefonun titreşim motoruyla belirgin haptic titreşim gönderme.

---

## 📸 2. Sosyal Viralite & Hikaye Kartı (Instagram / X Story Share Card)

### Özellik Açıklaması
Kullanıcının günün sonunda katettiği mesafeyi veya kazandığı yeni bir kupa başarısını tek tıkla Instagram Story, X veya WhatsApp durumunda paylaşabileceği, estetik, yüksek çözünürlüklü neon görsel oluşturucu.

### Teknik ve Fonksiyonel Detaylar
* **Saf Compose Canvas Bitmap Render:**
  * Hiçbir harici bulut API'si olmadan doğrudan cihaz üzerinde `Picture` ve `Canvas.drawBitmap` kullanılarak 1080x1920 (9:16 Story boyutu) PNG görseli render edilir.
* **Kart İçeriği:**
  * AMOLED siyah zemin, neon mineral glow çerçeve.
  * Büyük dopamin rakamlar: *"Bugün 1.4 km kaydırdım!"*
  * Metafor kıyası: *"Eyfel Kulesi'nin zirvesine 4 kez tırmandım 🗼"*
  * Lider uygulama rozeti: *"#1 Instagram (850m)"*
  * Altta şık "Scroll-Meter ⚡" logosu ve Burak Yıldız imzası.
* **Paylaşım Entegrasyonu:**
  * Android Sharesheet (`Intent.ACTION_SEND` + `image/png`) ile doğrudan Instagram Story, WhatsApp veya Galeriye Kaydetme.
* **Haftalık Özet ("Scroll Wrapped"):**
  * Her Pazar akşamı haftalık toplam mesafe, en aktif gün ve haftanın toplam fethini özetleyen Spotify Wrapped tarzı karne kartı.

---

## 📲 3. Ana Ekran & Kilit Ekranı Entegrasyonu (Jetpack Glance Widgets)

### Özellik Açıklaması
Kullanıcının Scroll-Meter'ı açmasına gerek kalmadan, telefonunun ana ekranında anlık mesafesini ve hedefini takip edebilmesini sağlayan modern masaüstü bileşenleri.

### Teknik ve Fonksiyonel Detaylar
* **Jetpack Glance Compose Kütüphanesi:**
  * Saf Kotlin & Compose mimarisiyle yazılmış Android Widget'ları.
* **Widget Varyasyonları:**
  * **2x2 Minimalist Sayaç:** Günlük mesafe (metre/km), dairesel ilerleme halkası ve lider uygulama simgesi.
  * **4x2 Bento Geniş Kart:** Canlı mesafe, Eyfel Kulesi'ne kalan metre barı ve son 3 uygulamanın dikey minyatür çubuk grafiği.
* **Canlı Kilometre Sayacı (Odometer Bildirimi):**
  * Arabalardaki dijital takometre gibi, kilit ekranında ve bildirim çekmecesinde sessizce ilerleyen retro/neon tek satırlık canlı mesafe sayacı.

---

## 📳 4. Dijital Sağlık & Dokunsal Frenleme (Mindful Friction)

### Özellik Açıklaması
Kullanıcıyı telefonundan soğutmadan, otomatik kaydırma refleksini fiziksel olarak hissettiren inovatif engelleme araçları.

### Teknik ve Fonksiyonel Detaylar
* **Haptic Speed Bump (Dokunsal Hız Kasısi):**
  * Kullanıcı örneğin TikTok veya Instagram'da aralıksız her `200 metre` kaydırdığında titreşim motoru hafif bir mikro "tık" titreşimi versin. Kullanıcıya parmağının ne kadar çok hareket ettiğini fiziksel olarak hissettirir.
* **Anlık Kaydırma Hız Göstergesi (Scroll Speedometer):**
  * Gerçek zamanlı parmak hızı ölçümü: `m/s` (metre/saniye) veya `km/s`.
  * Gösterge: *"Şu an saatte 4.5 km hızla doomscrolling yapıyorsunuz! 🏎️"*
* **"Touch Grass" Molası:**
  * Günlük hedef %100 aşıldığında ekranda beliren esprili animasyon: *"Dışarı çık ve biraz çimene dokun 🍃"*.

---

## 📊 5. İleri Düzey Analitikler & Veri Araçları

### Özellik Açıklaması
Kullanıcının dijital alışkanlıklarını derinlemesine anlamasını sağlayan veri görselleştirme araçları.

### Teknik ve Fonksiyonel Detaylar
* **Saatlik Isı Haritası (Hourly 24-Hour Heatmap):**
  * Günün 24 saatini gösteren neon bloklar (GitHub katkı matrisi tarzında).
  * Gece saatlerinde (örneğin 01:00 - 04:00) yapılan kaydırmaları koyu amber/kırmızı ile vurgulayarak uyku öncesi doomscrolling alışkanlığını ortaya çıkarır.
* **Uygulama Düellosu (App Duel):**
  * İki popüler uygulamanın doğrudan karşılaştırması:
  * *"Instagram vs TikTok"* — Hangisinde kaç metre kaydırdın, hangisi daha çok zamanını/mesafeni çaldı?
* **Yerel Veri Dışa Aktarma (CSV / JSON Export):**
  * Kullanıcının geçmiş tüm telemetri kayıtlarını yerel hafızaya `scroll_meter_export_YYYY_MM_DD.csv` olarak dışa aktarması.
  * Gizlilik ilkesini bozmaz (dosya yalnızca cihaz içine yazılır).

---

## 🛡️ Teknik Prensipler (Değişmeyecek Temel Kurallar)

1. **Mutlak Gizlilik:** Yeni eklenecek hiçbir özellik için `INTERNET` izni istenmeyecektir. Tüm görseller ve raporlar %100 cihaz içinde yerel olarak işlenecektir.
2. **Pil Tasarrufu:** Arka plan kontrolleri 5 saniyelik debounced batch motoru ve ekran kapalıyken uyku modu prensibine sadık kalacaktır.
3. **100% Yeşil Test Garantisi:** Her yeni özellik (Widget, Haptic filtre, Alarm mantığı) TDD ile yazılacak ve birim testlerle doğrulanacaktır.
4. **9 Dil Desteği:** Yeni eklenen her metin ve uyarı mesajı tüm 9 dilde (İngilizce, Türkçe, Almanca, İtalyanca, Rusça, Fransızca, Çince, Portekizce, Hintçe) yerelleştirilecektir.

---

*Scroll-Meter Proje Geliştirme Notları — Burak Yıldız (@BurakYildizGameDev)*
