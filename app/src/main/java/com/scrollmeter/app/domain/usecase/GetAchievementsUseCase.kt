package com.scrollmeter.app.domain.usecase

import com.scrollmeter.app.core.dispatchers.IoDispatcher
import com.scrollmeter.app.core.result.Result
import com.scrollmeter.app.data.datastore.SettingsDataStore
import com.scrollmeter.app.domain.model.Achievement
import com.scrollmeter.app.domain.model.AchievementCategory
import com.scrollmeter.app.domain.model.TrackingMode
import com.scrollmeter.app.domain.repository.IScrollRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min

/**
 * Provides 100 structured and engaging quests/achievements spanning:
 * - Metric Milestones (from 5 cm up to 42 km Marathons and Space boundaries)
 * - Platform Archetypes & Specialists (TikTok, Instagram, YouTube, X, Reddit, Chrome, Messaging, Games)
 * - Digital Wellness & Multi-App Habits
 * - Physical Calibration & Precision Tuning
 */
class GetAchievementsUseCase @Inject constructor(
    private val getDailyTotalDistanceUseCase: GetDailyTotalDistanceUseCase,
    private val scrollRepository: IScrollRepository,
    private val settingsDataStore: SettingsDataStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    private data class AchievementSettingsContext(
        val calibration: Float,
        val trackingMode: TrackingMode,
        val speedMultiplier: Float,
        val dailyGoalMeters: Float,
        val isDemo: Boolean
    )

    operator fun invoke(epochDays: Long): Flow<Result<List<Achievement>>> {
        val settingsFlow = combine(
            settingsDataStore.calibrationMultiplier,
            settingsDataStore.trackingMode,
            settingsDataStore.cadenceSpeedMultiplier,
            settingsDataStore.dailyGoalMeters,
            settingsDataStore.isDemoModeActive
        ) { calibration, trackingMode, speed, goal, isDemo ->
            AchievementSettingsContext(calibration, trackingMode, speed, goal, isDemo)
        }

        return combine(
            getDailyTotalDistanceUseCase(epochDays),
            scrollRepository.getDailyAppSessions(epochDays),
            settingsFlow
        ) { distanceResult, sessions, settings ->
            val todayMeters = if (distanceResult is Result.Success) distanceResult.data.inMeters else 0.0
            val activeSessions = sessions.filter { it.totalPixels > 0.0 || it.physicalDistance.inMeters > 0.0 }
            val distinctAppsCount = activeSessions.size
            val maxSingleAppMeters = activeSessions.maxOfOrNull { it.physicalDistance.inMeters } ?: 0.0

            // Specific archetype aggregates
            fun packageMeters(predicate: (String) -> Boolean): Double {
                return activeSessions.filter { predicate(it.packageName.lowercase()) }
                    .sumOf { it.physicalDistance.inMeters }
            }

            val instagramMeters = packageMeters { it.contains("instagram") }
            val tiktokMeters = packageMeters { it.contains("musically") || it.contains("trill") || it.contains("tiktok") }
            val twitterMeters = packageMeters { it.contains("twitter") || it.contains("x.android") }
            val redditMeters = packageMeters { it.contains("reddit") }
            val webMeters = packageMeters { it.contains("chrome") || it.contains("firefox") || it.contains("browser") }
            val messagingMeters = packageMeters { it.contains("whatsapp") || it.contains("telegram") || it.contains("discord") || it.contains("signal") }
            val gameMeters = packageMeters {
                it.contains("game") || it.contains("supercell") || it.contains("subway") ||
                    it.contains("candycrush") || it.contains("roblox") || it.contains("unity") ||
                    it.contains("pubg") || it.contains("genshin") || it.contains("mojang") || it.contains("ea.gp")
            }
            val youtubeMeters = packageMeters { it.contains("youtube") }
            val shoppingMeters = packageMeters { it.contains("amazon") || it.contains("trendyol") || it.contains("shopping") }

            val list = mutableListOf<Achievement>()

            // Helper builders
            fun milestone(id: String, title: String, desc: String, emoji: String, target: Double): Achievement {
                val unlocked = todayMeters >= target
                val progress = (todayMeters / target).toFloat().coerceIn(0f, 1f)
                val text = if (unlocked) "${formatMeters(target)} Tamamlandı" else "${formatMeters(todayMeters)} / ${formatMeters(target)}"
                return Achievement(id, title, desc, emoji, AchievementCategory.MILESTONE, unlocked, progress, text)
            }

            fun specialist(id: String, title: String, desc: String, emoji: String, target: Double, current: Double): Achievement {
                val unlocked = current >= target
                val progress = (current / target).toFloat().coerceIn(0f, 1f)
                val text = if (unlocked) "${formatMeters(target)} Tamamlandı" else "${formatMeters(current)} / ${formatMeters(target)}"
                return Achievement(id, title, desc, emoji, AchievementCategory.SPECIALIST, unlocked, progress, text)
            }

            fun habit(id: String, title: String, desc: String, emoji: String, target: Double, current: Double, unit: String): Achievement {
                val unlocked = current >= target
                val progress = (current / target).toFloat().coerceIn(0f, 1f)
                val text = if (unlocked) "Tamamlandı" else "${current.toInt()} / ${target.toInt()} $unit"
                return Achievement(id, title, desc, emoji, AchievementCategory.HABIT, unlocked, progress, text)
            }

            fun precision(id: String, title: String, desc: String, emoji: String, unlocked: Boolean, text: String): Achievement {
                return Achievement(id, title, desc, emoji, AchievementCategory.PRECISION, unlocked, if (unlocked) 1f else 0f, text)
            }

            // 1-9: Legacy Anchors
            list.add(Achievement("FIRST_STEP", "First Step", "Scroll your first 100 meters across any app.", "🥉", AchievementCategory.MILESTONE, todayMeters >= 100.0, min(1.0f, (todayMeters / 100.0).toFloat()), "${String.format(Locale.US, "%.1f", min(todayMeters, 100.0))}m / 100m"))
            list.add(Achievement("GIRAFFE_CLUB", "Giraffe Club", "Outscroll the tallest living land animal (5.5m).", "🦒", AchievementCategory.MILESTONE, todayMeters >= 5.5, min(1.0f, (todayMeters / 5.5).toFloat()), "${String.format(Locale.US, "%.1f", min(todayMeters, 5.5))}m / 5.5m"))
            list.add(Achievement("STATUE_OF_LIBERTY", "Liberty Torch", "Climb past the Statue of Liberty pedestal (93m).", "🗽", AchievementCategory.MILESTONE, todayMeters >= 93.0, min(1.0f, (todayMeters / 93.0).toFloat()), "${String.format(Locale.US, "%.1f", min(todayMeters, 93.0))}m / 93m"))
            list.add(Achievement("EIFFEL_CLIMBER", "Eiffel Climber", "Scale the tip of the Eiffel Tower (330m) in a single day.", "🗼", AchievementCategory.MILESTONE, todayMeters >= 330.0, min(1.0f, (todayMeters / 330.0).toFloat()), "${String.format(Locale.US, "%.1f", min(todayMeters, 330.0))}m / 330m"))
            list.add(Achievement("BURJ_KHALIFA", "Cloud Piercer", "Reach the observation deck of Burj Khalifa (828m).", "🏙️", AchievementCategory.MILESTONE, todayMeters >= 828.0, min(1.0f, (todayMeters / 828.0).toFloat()), "${String.format(Locale.US, "%.1f", min(todayMeters, 828.0))}m / 828m"))
            list.add(Achievement("MARATHON_RUNNER", "Thumb Marathon", "Reach 1,000 meters in a single high-intensity day.", "🏃‍♂️", AchievementCategory.HABIT, todayMeters >= 1000.0, min(1.0f, (todayMeters / 1000.0).toFloat()), "${String.format(Locale.US, "%.1f", min(todayMeters, 1000.0))}m / 1000m"))
            list.add(Achievement("SPEED_DEMON", "Speed Demon", "Accumulate over 250m inside a single dominant application.", "⚡", AchievementCategory.HABIT, maxSingleAppMeters >= 250.0, min(1.0f, (maxSingleAppMeters / 250.0).toFloat()), "${String.format(Locale.US, "%.1f", min(maxSingleAppMeters, 250.0))}m / 250m"))
            list.add(Achievement("SOCIAL_EXPLORER", "Social Explorer", "Log active scroll sessions across at least 3 distinct apps.", "📱", AchievementCategory.HABIT, distinctAppsCount >= 3, min(1.0f, distinctAppsCount.toFloat() / 3f), "$distinctAppsCount / 3 apps"))
            list.add(Achievement("PRECISION_TUNER", "Precision Tuner", "Calibrate screen physical scale using the on-screen ruler.", "📐", AchievementCategory.PRECISION, settings.calibration != 1.0f, if (settings.calibration != 1.0f) 1.0f else 0.0f, if (settings.calibration != 1.0f) "Calibrated" else "Not calibrated"))

            // 10-45: Micro, Macro & Landmark Milestones
            list.add(milestone("QUEST_010", "İlk Kıvılcım", "Telefon ekranında ilk 5 cm yol katedin.", "✨", 0.05))
            list.add(milestone("QUEST_011", "Parmak Isınması", "10 cm fiziksel kaydırma mesafesine ulaşın.", "🖐️", 0.10))
            list.add(milestone("QUEST_012", "Cetvel Boyu", "Standart bir cetvel boyu (15 cm) kaydırın.", "📏", 0.15))
            list.add(milestone("QUEST_013", "Bir Karış", "20 cm mesafeyi geride bırakın.", "🤏", 0.20))
            list.add(milestone("QUEST_014", "Büyük Adım", "30 cm parmak yolculuğunu tamamlayın.", "👣", 0.30))
            list.add(milestone("QUEST_015", "Yarım Metre", "Ekranda yarım metre (50 cm) yol katedin.", "🎯", 0.50))
            list.add(milestone("QUEST_016", "Bir Metre Kulübü", "Tam 1 metre fiziksel kaydırmaya ulaşın.", "🥇", 1.0))
            list.add(milestone("QUEST_017", "Basketbol Potası", "Bir basketbol potasının yüksekliğini (3m) aşın.", "🏀", 3.0))
            list.add(milestone("QUEST_018", "Şehir Otobüsü", "Bir belediye otobüsü uzunluğunda (12m) kaydırın.", "🚌", 12.0))
            list.add(milestone("QUEST_019", "Basketbol Sahası", "Bir basketbol sahası boyu (15m) kaydırma yapın.", "🏟️", 15.0))
            list.add(milestone("QUEST_020", "Tenis Kortu", "Bir tenis kortu uzunluğunu (24m) geride bırakın.", "🎾", 24.0))
            list.add(milestone("QUEST_021", "Mavi Balina", "Dünyanın en büyük hayvanı boyunda (30m) kaydırın.", "🐋", 30.0))
            list.add(milestone("QUEST_022", "Olimpik Havuz", "Olimpik yüzme havuzunu (50m) baştan başa geçin.", "🏊", 50.0))
            list.add(milestone("QUEST_023", "Galata Kulesi", "Tarihi Galata Kulesi'nin yüksekliğine (63m) tırmanın.", "🏰", 63.0))
            list.add(milestone("QUEST_024", "Boeing 747", "Görkemli bir yolcu uçağının boyunu (70m) aşın.", "✈️", 70.0))
            list.add(milestone("QUEST_025", "Futbol Sahası", "Bir futbol sahasının uzunluğunu (105m) tamamlayın.", "⚽", 105.0))
            list.add(milestone("QUEST_026", "Büyük Piramit", "Giza Büyük Piramidi'nin yüksekliğini (139m) tırmanın.", "🔺", 139.0))
            list.add(milestone("QUEST_027", "Kolezyum Turu", "Roma Kolezyumu'nun çevresini (188m) turlayın.", "🏛️", 188.0))
            list.add(milestone("QUEST_028", "Çeyrek Kilometre", "Günde 250 metre kaydırma mesafesine ulaşın.", "🥈", 250.0))
            list.add(milestone("QUEST_029", "Empire State", "Empire State binasının terasına (381m) çıkın.", "🏢", 381.0))
            list.add(milestone("QUEST_030", "Petronas Kuleleri", "Petronas İkiz Kuleleri'ni (452m) geride bırakın.", "🏙️", 452.0))
            list.add(milestone("QUEST_031", "Yarım Kilometre", "Günde tam 500 metre kaydırma mesafesi kaydedin.", "🎖️", 500.0))
            list.add(milestone("QUEST_032", "Taipei 101", "Taipei 101 gökdeleninin tepesine (508m) ulaşın.", "🗼", 508.0))
            list.add(milestone("QUEST_033", "Şanghay Kulesi", "Şanghay Kulesi'nin yüksekliğini (632m) fethedin.", "🌆", 632.0))
            list.add(milestone("QUEST_034", "Boğaziçi Köprüsü", "15 Temmuz Şehitler Köprüsü'nü (1560m) yürüyün.", "🌉", 1560.0))
            list.add(milestone("QUEST_035", "İki Kilometre", "Günde 2.000 metre (2 km) parmak mesafesi kat edin.", "🚶‍♂️", 2000.0))
            list.add(milestone("QUEST_036", "Golden Gate", "Golden Gate Köprüsü'nü (2737m) baştan başa geçin.", "🌁", 2737.0))
            list.add(milestone("QUEST_037", "Üç Kilometre", "Günde 3.000 metre kaydırma eşiğini devirin.", "🧗", 3000.0))
            list.add(milestone("QUEST_038", "Fuji Dağı", "Japonya'nın simgesi Fuji Dağı'na (3776m) tırmanın.", "🗻", 3776.0))
            list.add(milestone("QUEST_039", "Beş Kilometre Maratonu", "Günde tam 5.000 metre (5 km) kaydırın.", "🏃", 5000.0))
            list.add(milestone("QUEST_040", "Everest Zirvesi", "Dünyanın çatısı Everest'in zirvesine (8848m) ulaşın.", "🏔️", 8848.0))
            list.add(milestone("QUEST_041", "On Kilometre Titanı", "Günde akıl almaz 10.000 metre (10 km) kaydırın.", "👑", 10000.0))
            list.add(milestone("QUEST_042", "Mariana Çukuru", "Okyanusun en derin noktasına (11034m) inin.", "🌊", 11034.0))
            list.add(milestone("QUEST_043", "Stratosfer Sınırı", "Gökyüzünün 15.000 metre sınırına yükselin.", "🪂", 15000.0))
            list.add(milestone("QUEST_044", "Yarı Maraton", "Tam 21.097 metre kaydırma mesafesi katedin.", "🎽", 21097.0))
            list.add(milestone("QUEST_045", "Tam Maraton", "42.195 metre resmi maraton koşusu mesafesi!", "🏅", 42195.0))

            // 46-70: App Specialist Quests
            list.add(specialist("QUEST_046", "Instagram Çırağı", "Instagram'da 5 metre kaydırın.", "📸", 5.0, instagramMeters))
            list.add(specialist("QUEST_047", "Reels Gezgini", "Instagram'da 25 metre reels akışı tarayın.", "🎞️", 25.0, instagramMeters))
            list.add(specialist("QUEST_048", "Instagram Şövalyesi", "Instagram'da 100 metre mesafe kaydedin.", "⚜️", 100.0, instagramMeters))
            list.add(specialist("QUEST_049", "Instagram Efendisi", "Instagram'da 250 metre devasa mesafe!", "👑", 250.0, instagramMeters))
            list.add(specialist("QUEST_050", "TikTok Dalgıcı", "TikTok'ta 5 metre kaydırın.", "🎵", 5.0, tiktokMeters))
            list.add(specialist("QUEST_051", "Sonsuz Akış", "TikTok'ta 25 metre video izleyin.", "🌀", 25.0, tiktokMeters))
            list.add(specialist("QUEST_052", "TikTok Efendisi", "TikTok'ta 100 metre kaydırma yapın.", "⚡", 100.0, tiktokMeters))
            list.add(specialist("QUEST_053", "Algoritma Bükücü", "TikTok'ta 250 metre akış tüketin.", "🔮", 250.0, tiktokMeters))
            list.add(specialist("QUEST_054", "Twitter / X Okuru", "X (Twitter) üzerinde 5 metre akış okuyun.", "🐦", 5.0, twitterMeters))
            list.add(specialist("QUEST_055", "Gündem Avcısı", "X (Twitter) üzerinde 25 metre gündem tarayın.", "📰", 25.0, twitterMeters))
            list.add(specialist("QUEST_056", "Tweet Maratoncusu", "X üzerinde 100 metre tweet geçin.", "💬", 100.0, twitterMeters))
            list.add(specialist("QUEST_057", "Zaman Tüneli Fatihi", "X üzerinde 200 metre kaydırma yapın.", "⏳", 200.0, twitterMeters))
            list.add(specialist("QUEST_058", "Reddit Çaylağı", "Reddit'te 5 metre başlık kaydırın.", "🤖", 5.0, redditMeters))
            list.add(specialist("QUEST_059", "Karma Meraklısı", "Reddit'te 25 metre kaydırma yapın.", "⬆️", 25.0, redditMeters))
            list.add(specialist("QUEST_060", "Derin Subreddit", "Reddit'te 100 metre yorum okuyun.", "🔍", 100.0, redditMeters))
            list.add(specialist("QUEST_061", "Web Gezgini", "Chrome veya tarayıcıda 5 metre kaydırın.", "🌐", 5.0, webMeters))
            list.add(specialist("QUEST_062", "Sekme Avcısı", "Tarayıcıda 25 metre web içeriği okuyun.", "📑", 25.0, webMeters))
            list.add(specialist("QUEST_063", "İnternet Kütüphanecisi", "Tarayıcıda 100 metre makale gezin.", "📚", 100.0, webMeters))
            list.add(specialist("QUEST_064", "Sohbet Başlangıcı", "Mesajlaşma uygulamalarında 2 metre kaydırın.", "🗨️", 2.0, messagingMeters))
            list.add(specialist("QUEST_065", "Mesaj Ustası", "Mesajlaşma uygulamalarında 10 metre kaydırın.", "✉️", 10.0, messagingMeters))
            list.add(specialist("QUEST_066", "Grup Yöneticisi", "Mesajlaşma uygulamalarında 30 metre kaydırın.", "👥", 30.0, messagingMeters))
            list.add(specialist("QUEST_067", "Mobil Oyuncu", "Oyunlarda 5 metre parmak hareketi yapın.", "🎮", 5.0, gameMeters))
            list.add(specialist("QUEST_068", "Refleks Ustası", "Oyunlarda 25 metre kaydırma tamamlayın.", "🕹️", 25.0, gameMeters))
            list.add(specialist("QUEST_069", "Oyun Şampiyonu", "Oyunlarda 100 metre parmak yolu kat edin.", "🏆", 100.0, gameMeters))
            list.add(specialist("QUEST_070", "YouTube Shorts Kaşifi", "YouTube'da 10 metre video kaydırın.", "▶️", 10.0, youtubeMeters))

            // 71-85: Habits & Diversity
            list.add(specialist("QUEST_071", "YouTube Maratoncusu", "YouTube'da 50 metre kaydırma yapın.", "📺", 50.0, youtubeMeters))
            list.add(specialist("QUEST_072", "Alışveriş Kurdu", "E-ticaret uygulamalarında 10 metre gezin.", "🛍️", 10.0, shoppingMeters))
            list.add(specialist("QUEST_073", "İndirim Avcısı", "E-ticaret uygulamalarında 40 metre kaydırın.", "🏷️", 40.0, shoppingMeters))
            list.add(habit("QUEST_074", "İkili Denge", "En az 2 farklı uygulamada kaydırma kaydedin.", "⚖️", 2.0, distinctAppsCount.toDouble(), "uygulama"))
            list.add(habit("QUEST_075", "Dörtlü Odak", "En az 4 farklı uygulamada telemetri toplayın.", "🍀", 4.0, distinctAppsCount.toDouble(), "uygulama"))
            list.add(habit("QUEST_076", "Uygulama Koleksiyoneri", "En az 6 farklı uygulamada kaydırma yapın.", "🗂️", 6.0, distinctAppsCount.toDouble(), "uygulama"))
            list.add(habit("QUEST_077", "Dijital Çokyüzlü", "En az 8 farklı uygulamayı aktif kullanın.", "🧩", 8.0, distinctAppsCount.toDouble(), "uygulama"))
            list.add(habit("QUEST_078", "Ekran Ekosistemi", "En az 10 farklı uygulamada kaydırma kaydedin.", "🌐", 10.0, distinctAppsCount.toDouble(), "uygulama"))

            val under50 = todayMeters in 0.01..50.0
            list.add(Achievement("QUEST_079", "Hafif Gün", "Günlük mesafenizi 50 metrenin altında tutun.", "🍃", AchievementCategory.HABIT, under50, if (under50) 1f else 0.5f, if (under50) "Dengeli" else "50m üzeri"))

            val under150 = todayMeters in 0.01..150.0
            list.add(Achievement("QUEST_080", "Dengeli Kullanıcı", "Günlük mesafenizi 150 metrenin altında tutun.", "🧘", AchievementCategory.HABIT, under150, if (under150) 1f else 0.5f, if (under150) "Dengeli" else "150m üzeri"))

            val goalReached = settings.dailyGoalMeters > 0f && todayMeters >= settings.dailyGoalMeters
            list.add(Achievement("QUEST_081", "Hedef Avcısı", "Günlük kaydırma hedefinize tam olarak ulaşın.", "🎯", AchievementCategory.HABIT, goalReached, if (goalReached) 1f else (todayMeters / settings.dailyGoalMeters).toFloat().coerceIn(0f, 1f), if (goalReached) "Tamamlandı" else "${formatMeters(todayMeters)} / ${settings.dailyGoalMeters.toInt()}m"))

            val doubleGoal = settings.dailyGoalMeters > 0f && todayMeters >= (settings.dailyGoalMeters * 2)
            list.add(Achievement("QUEST_082", "Çift Hedef", "Günlük hedefinizin 2 katını devirin.", "🚀", AchievementCategory.HABIT, doubleGoal, if (doubleGoal) 1f else (todayMeters / (settings.dailyGoalMeters * 2)).toFloat().coerceIn(0f, 1f), if (doubleGoal) "Tamamlandı" else "${formatMeters(todayMeters)} / ${(settings.dailyGoalMeters * 2).toInt()}m"))

            val dominantRatio = if (todayMeters > 0.0) maxSingleAppMeters / todayMeters else 0.0
            val isMonotasker = dominantRatio >= 0.70 && todayMeters >= 10.0
            list.add(Achievement("QUEST_083", "Tek Tabanca", "Günün kaydırmasının %70'ini tek uygulamada yapın.", "🎯", AchievementCategory.HABIT, isMonotasker, if (isMonotasker) 1f else (dominantRatio / 0.7).toFloat().coerceIn(0f, 1f), if (isMonotasker) "%${(dominantRatio * 100).toInt()}" else "%${(dominantRatio * 100).toInt()} / %70"))

            val isDistributed = dominantRatio in 0.01..0.40 && todayMeters >= 10.0
            list.add(Achievement("QUEST_084", "Eşit Dağılım", "Hiçbir uygulama toplamın %40'ını aşmasın.", "⚖️", AchievementCategory.HABIT, isDistributed, if (isDistributed) 1f else 0.5f, if (isDistributed) "Dengeli" else "Tek uygulama ağırlıklı"))

            list.add(Achievement("QUEST_085", "Kaydırma Disiplini", "Aktif telemetriyi 0m'den başlatıp takip edin.", "🛡️", AchievementCategory.HABIT, todayMeters > 0.0, if (todayMeters > 0.0) 1f else 0f, if (todayMeters > 0.0) "Aktif" else "0m"))

            // 86-99: Precision, Calibration & Settings
            list.add(precision("QUEST_086", "Fiziksel Kalibratör", "Ekran cetvelini kullanarak kalibrasyon yapın.", "📏", settings.calibration != 1.0f, if (settings.calibration != 1.0f) "${settings.calibration}x ayarlandı" else "Varsayılan 1.00x"))
            list.add(precision("QUEST_087", "Mikro Hassasiyet", "Kalibrasyon çarpanını 1.00x dışına ayarlayın.", "🔬", abs(settings.calibration - 1.0f) >= 0.02f, "${settings.calibration}x"))
            list.add(precision("QUEST_088", "Büyüteç Modu", "Kalibrasyonu 1.10x veya üzerine getirin.", "🔍", settings.calibration >= 1.10f, "${settings.calibration}x / 1.10x"))
            list.add(precision("QUEST_089", "Kompakt Ölçek", "Kalibrasyonu 0.90x veya altına getirin.", "🔎", settings.calibration <= 0.90f, "${settings.calibration}x / 0.90x"))

            val isRelaxed = abs(settings.speedMultiplier - 0.80f) < 0.05f
            list.add(precision("QUEST_090", "Sakin Tempo", "Kadans hızını Sakin (0.8x) olarak seçin.", "🐢", isRelaxed, if (isRelaxed) "0.8x Aktif" else "Seçilmedi"))

            val isBalanced = abs(settings.speedMultiplier - 1.00f) < 0.05f
            list.add(precision("QUEST_091", "Dengeli Tempo", "Kadans hızını Dengeli (1.0x) olarak kullanın.", "⏱️", isBalanced, if (isBalanced) "1.0x Aktif" else "Seçilmedi"))

            val isRapid = abs(settings.speedMultiplier - 1.25f) < 0.05f
            list.add(precision("QUEST_092", "Seri Başparmak", "Kadans hızını Seri (1.25x) olarak seçin.", "🐇", isRapid, if (isRapid) "1.25x Aktif" else "Seçilmedi"))

            val isCustomGoal = settings.dailyGoalMeters.toInt() != 100
            list.add(precision("QUEST_093", "Hedef Belirleyici", "Günlük hedefinizi ayarlardan özelleştirin.", "📝", isCustomGoal, "${settings.dailyGoalMeters.toInt()}m"))
            list.add(precision("QUEST_094", "Büyük Hedef", "Günlük hedefinizi 250m veya üzerine ayarlayın.", "⛰️", settings.dailyGoalMeters >= 250f, "${settings.dailyGoalMeters.toInt()}m / 250m"))
            list.add(precision("QUEST_095", "Ultra Hedef", "Günlük hedefinizi 500m veya üzerine kurun.", "🗻", settings.dailyGoalMeters >= 500f, "${settings.dailyGoalMeters.toInt()}m / 500m"))

            list.add(precision("QUEST_096", "Güvenli Mod Koruyucusu", "Standart Güvenli Modu aktif olarak kullanın.", "🛡️", settings.trackingMode == TrackingMode.SAFE_USAGE_STATS, if (settings.trackingMode == TrackingMode.SAFE_USAGE_STATS) "Aktif" else "Pasif"))
            list.add(precision("QUEST_097", "Laboratuvar Kaşifi", "Erişilebilirlik Piksel Sensörünü deneyin.", "🧪", settings.trackingMode == TrackingMode.ACCESSIBILITY_LAB, if (settings.trackingMode == TrackingMode.ACCESSIBILITY_LAB) "Aktif" else "Pasif"))
            list.add(precision("QUEST_098", "Portföy İncelemesi", "Demo Modunu etkinleştirip inceleyin.", "💼", settings.isDemo || settings.trackingMode == TrackingMode.DEMO_PORTFOLIO, if (settings.isDemo) "Aktif" else "Pasif"))
            list.add(precision("QUEST_099", "Tertemiz Sayfa", "Ayarlardan bugünkü telemetriyi sıfırlayın.", "🧹", todayMeters < 5.0, "Hazır"))

            // 100: Ultimate Master Quest
            val currentUnlockedBefore100 = list.count { it.isUnlocked }
            val masterTarget = 25
            val masterUnlocked = currentUnlockedBefore100 >= masterTarget
            val masterProgress = (currentUnlockedBefore100.toFloat() / masterTarget.toFloat()).coerceIn(0f, 1f)
            list.add(
                Achievement(
                    id = "QUEST_100",
                    title = "Scroll-Meter Efsanesi",
                    description = "Tüm görevlerden en az 25 tanesini başarıyla tamamlayın!",
                    iconEmoji = "👑",
                    category = AchievementCategory.HABIT,
                    isUnlocked = masterUnlocked,
                    progress = masterProgress,
                    progressText = if (masterUnlocked) "25 Görev Tamamlandı!" else "$currentUnlockedBefore100 / $masterTarget görev"
                )
            )

            Result.Success(list) as Result<List<Achievement>>
        }
            .catch { emit(Result.Error(it)) }
            .flowOn(ioDispatcher)
    }

    private fun formatMeters(meters: Double): String {
        return when {
            meters < 1.0 -> String.format(Locale.US, "%.0fcm", (meters * 100.0).coerceAtLeast(0.0))
            meters < 1000.0 -> String.format(Locale.US, "%.1fm", meters)
            else -> String.format(Locale.US, "%.2fkm", meters / 1000.0)
        }
    }
}
