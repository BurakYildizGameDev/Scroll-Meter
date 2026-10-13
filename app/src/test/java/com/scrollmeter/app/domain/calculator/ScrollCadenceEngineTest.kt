package com.scrollmeter.app.domain.calculator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ScrollCadenceEngineTest {

    @ParameterizedTest
    @CsvSource(
        "com.zhiliaoapp.musically, 0.040",
        "com.instagram.android, 0.040",
        "com.snapchat.android, 0.040",
        "com.twitter.android, 0.035",
        "com.reddit.frontpage, 0.035",
        "com.android.chrome, 0.025",
        "com.whatsapp, 0.008",
        "com.google.android.youtube, 0.002",
        "com.unknown.calculator, 0.015"
    )
    fun `getCadenceMetersPerMinute returns expected cadence for archetypes`(
        packageName: String,
        expectedCadence: Double
    ) {
        val cadence = ScrollCadenceEngine.getCadenceMetersPerMinute(packageName)
        assertEquals(expectedCadence, cadence, 0.001)
    }

    @Test
    fun `calculateMeters computes correct distance for 10 minutes of social feed`() {
        // 10 minutes of Twitter = 10 * 0.035 = 0.35 meters (35 cm)
        val tenMinutesMillis = 10 * 60_000L
        val meters = ScrollCadenceEngine.calculateMeters(
            packageName = "com.twitter.android",
            foregroundMillis = tenMinutesMillis,
            speedMultiplier = 1.0f,
            calibrationMultiplier = 1.0f
        )
        assertEquals(0.35, meters, 0.001)
    }

    @Test
    fun `calculateMeters applies user speedMultiplier correctly`() {
        // 5 minutes of TikTok at 1.25x rapid speed = 5 * 0.040 * 1.25 = 0.25 meters (25 cm)
        val fiveMinutesMillis = 5 * 60_000L
        val meters = ScrollCadenceEngine.calculateMeters(
            packageName = "com.zhiliaoapp.musically",
            foregroundMillis = fiveMinutesMillis,
            speedMultiplier = 1.25f,
            calibrationMultiplier = 1.0f
        )
        assertEquals(0.25, meters, 0.001)
    }

    @Test
    fun `calculateMeters returns 0 for zero or negative durations`() {
        assertEquals(0.0, ScrollCadenceEngine.calculateMeters("com.instagram.android", 0L), 0.001)
        assertEquals(0.0, ScrollCadenceEngine.calculateMeters("com.instagram.android", -5000L), 0.001)
    }

    @Test
    fun `metersToPixels accurately converts physical meters to screen pixels based on PPI`() {
        // 1 meter = 1000 mm = ~39.37 inches. At 400 PPI: ~15,748 pixels
        val meters = 1.0
        val ppi = 400f
        val pixels = ScrollCadenceEngine.metersToPixels(meters, ppi)
        val expected = (1000.0 / 25.4) * 400.0
        assertEquals(expected, pixels, 0.01)
    }
}
