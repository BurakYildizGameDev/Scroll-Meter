package com.scrollmeter.app.domain.calculator

/**
 * Intelligent Doomscroll Cadence Engine.
 * Converts active foreground screen minutes into real physical distance (meters)
 * based on empirical interaction cadences per application archetype.
 */
object ScrollCadenceEngine {

    // Base scroll speed in meters per active foreground minute (physical thumb travel on glass)
    const val CADENCE_SHORT_VIDEO_M_PER_MIN = 0.18  // TikTok, Instagram Reels, Shorts (~3 swipes/min * 6cm thumb travel)
    const val CADENCE_SOCIAL_FEED_M_PER_MIN = 0.22  // Twitter/X, Reddit, Threads (~3.5 swipes/min * 6cm thumb travel)
    const val CADENCE_WEB_COMMERCE_M_PER_MIN = 0.14 // Chrome, Firefox, Amazon, Trendyol (~2.2 swipes/min)
    const val CADENCE_MESSAGING_M_PER_MIN = 0.04    // WhatsApp, Telegram, Messenger (mostly typing/reading)
    const val CADENCE_VIDEO_STREAM_M_PER_MIN = 0.01 // YouTube, Netflix (Stationary viewing)
    const val CADENCE_DEFAULT_M_PER_MIN = 0.06      // Generic apps (~1 swipe/min)

    private val SHORT_VIDEO_PACKAGES = setOf(
        "com.zhiliaoapp.musically", // TikTok
        "com.ss.android.ugc.trill",
        "com.instagram.android",    // Instagram Reels & Stories
        "com.snapchat.android"
    )

    private val SOCIAL_FEED_PACKAGES = setOf(
        "com.twitter.android",      // X / Twitter
        "com.reddit.frontpage",     // Reddit
        "com.instagram.barcelona",  // Threads
        "com.facebook.katana",      // Facebook
        "com.linkedin.android",     // LinkedIn
        "com.pinterest"
    )

    private val WEB_COMMERCE_PACKAGES = setOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.microsoft.emmx",
        "com.amazon.mShop.android.shopping",
        "trendyol.com"
    )

    private val MESSAGING_PACKAGES = setOf(
        "com.whatsapp",
        "org.telegram.messenger",
        "com.facebook.orca",
        "com.discord"
    )

    private val VIDEO_STREAM_PACKAGES = setOf(
        "com.google.android.youtube",
        "com.netflix.mediaclient",
        "com.spotify.music"
    )

    /**
     * Resolves the expected scroll velocity (meters / minute) for a specific package.
     */
    fun getCadenceMetersPerMinute(packageName: String): Double {
        val lower = packageName.lowercase()
        return when {
            SHORT_VIDEO_PACKAGES.contains(lower) -> CADENCE_SHORT_VIDEO_M_PER_MIN
            SOCIAL_FEED_PACKAGES.contains(lower) -> CADENCE_SOCIAL_FEED_M_PER_MIN
            WEB_COMMERCE_PACKAGES.contains(lower) -> CADENCE_WEB_COMMERCE_M_PER_MIN
            MESSAGING_PACKAGES.contains(lower) -> CADENCE_MESSAGING_M_PER_MIN
            VIDEO_STREAM_PACKAGES.contains(lower) -> CADENCE_VIDEO_STREAM_M_PER_MIN
            lower.contains("tiktok") || lower.contains("instagram") -> CADENCE_SHORT_VIDEO_M_PER_MIN
            lower.contains("twitter") || lower.contains("reddit") -> CADENCE_SOCIAL_FEED_M_PER_MIN
            lower.contains("browser") || lower.contains("chrome") -> CADENCE_WEB_COMMERCE_M_PER_MIN
            else -> CADENCE_DEFAULT_M_PER_MIN
        }
    }

    /**
     * Calculates physical distance in meters given active foreground duration in milliseconds.
     *
     * @param packageName Application identifier
     * @param foregroundMillis Milliseconds spent in foreground today
     * @param speedMultiplier User speed profile multiplier (e.g. 0.8x slow, 1.0x balanced, 1.25x rapid)
     * @param calibrationMultiplier Screen physical calibration factor
     * @return Calculated physical distance in meters
     */
    fun calculateMeters(
        packageName: String,
        foregroundMillis: Long,
        speedMultiplier: Float = 1.0f,
        calibrationMultiplier: Float = 1.0f
    ): Double {
        if (foregroundMillis <= 0L) return 0.0
        val activeMinutes = foregroundMillis / 60_000.0
        val baseSpeed = getCadenceMetersPerMinute(packageName)
        val calculated = activeMinutes * baseSpeed * speedMultiplier.toDouble() * calibrationMultiplier.toDouble()
        return calculated.coerceAtLeast(0.0)
    }

    /**
     * Converts meters to pixels based on device hardware PPI.
     */
    fun metersToPixels(meters: Double, devicePpi: Float): Double {
        val millimeters = meters * 1000.0
        val inches = millimeters / 25.4
        return (inches * devicePpi).coerceAtLeast(0.0)
    }
}
