package com.scrollmeter.app.domain.filter

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ScrollAnomalyFilterTest {

    @ParameterizedTest
    @CsvSource(
        "0.0, 0.0",
        "1.0, 1.0",
        "2.0, 0.0",
        "-1.5, 1.5"
    )
    fun `isValidScroll rejects touch-screen micro jitter below threshold`(dx: Float, dy: Float) {
        assertFalse(ScrollAnomalyFilter.isValidScroll(dx, dy))
    }

    @ParameterizedTest
    @CsvSource(
        "10.0, 20.0",
        "0.0, 150.0",
        "-200.0, 300.0",
        "1500.0, 2000.0"
    )
    fun `isValidScroll approves standard plausible scroll gestures`(dx: Float, dy: Float) {
        assertTrue(ScrollAnomalyFilter.isValidScroll(dx, dy))
    }

    @ParameterizedTest
    @CsvSource(
        "5001.0, 0.0",
        "4000.0, 4000.0",
        "0.0, 10000.0",
        "-6000.0, -100.0"
    )
    fun `isValidScroll rejects multi-touch glitches and massive coordinate spikes`(dx: Float, dy: Float) {
        assertFalse(ScrollAnomalyFilter.isValidScroll(dx, dy))
    }

    @Test
    fun `isValidScroll safely rejects NaN and Infinite values`() {
        assertFalse(ScrollAnomalyFilter.isValidScroll(Float.NaN, 50f))
        assertFalse(ScrollAnomalyFilter.isValidScroll(50f, Float.POSITIVE_INFINITY))
    }
}
