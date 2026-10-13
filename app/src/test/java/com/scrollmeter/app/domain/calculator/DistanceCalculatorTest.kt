package com.scrollmeter.app.domain.calculator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class DistanceCalculatorTest {

    @Test
    fun `calculateHypotenusePixels matches classic 3-4-5 Pythagorean triangle`() {
        val hypotenuse = DistanceCalculator.calculateHypotenusePixels(300f, 400f)
        assertEquals(500.0, hypotenuse, 0.0001)
    }

    @Test
    fun `calculateHypotenusePixels handles pure vertical and horizontal fast paths`() {
        assertEquals(250.0, DistanceCalculator.calculateHypotenusePixels(0f, 250f), 0.0001)
        assertEquals(180.0, DistanceCalculator.calculateHypotenusePixels(180f, 0f), 0.0001)
        assertEquals(0.0, DistanceCalculator.calculateHypotenusePixels(0f, 0f), 0.0001)
    }

    @Test
    fun `calculateHypotenusePixels handles negative deltas properly`() {
        val hypotenuse = DistanceCalculator.calculateHypotenusePixels(-300f, -400f)
        assertEquals(500.0, hypotenuse, 0.0001)
    }

    @Test
    fun `pixelsToMillimeters calculates accurate metric length for standard 254 PPI`() {
        // At 254 PPI, 254 pixels should equal exactly 1 inch = 25.4 mm
        val mm = DistanceCalculator.pixelsToMillimeters(
            pixels = 254.0,
            devicePpi = 254.0f,
            calibrationMultiplier = 1.0f
        )
        assertEquals(25.4, mm, 0.0001)
    }

    @Test
    fun `pixelsToMillimeters handles zero and negative pixel values safely`() {
        assertEquals(0.0, DistanceCalculator.pixelsToMillimeters(0.0, 400f), 0.0001)
        assertEquals(0.0, DistanceCalculator.pixelsToMillimeters(-500.0, 400f), 0.0001)
    }

    @Test
    fun `pixelsToMillimeters protects against zero or negative PPI without crashing`() {
        // PPI <= 0 should be coerced to at least 1.0f
        val mmZeroPpi = DistanceCalculator.pixelsToMillimeters(100.0, 0f)
        assertEquals(2540.0, mmZeroPpi, 0.0001)

        val mmNegativePpi = DistanceCalculator.pixelsToMillimeters(100.0, -100f)
        assertEquals(2540.0, mmNegativePpi, 0.0001)
    }

    @Test
    fun `pixelsToMillimeters applies calibration multiplier accurately`() {
        // With 1.5x multiplier, effective PPI = 254 * 1.5 = 381
        // mm = (381 * 25.4) / 381 = 25.4 mm
        val mm = DistanceCalculator.pixelsToMillimeters(
            pixels = 381.0,
            devicePpi = 254.0f,
            calibrationMultiplier = 1.5f
        )
        assertEquals(25.4, mm, 0.0001)
    }

    @ParameterizedTest
    @CsvSource(
        "1000.0, 1.0",
        "500.0, 0.5",
        "0.0, 0.0"
    )
    fun `conversions to meters work accurately`(mm: Double, expectedMeters: Double) {
        assertEquals(expectedMeters, DistanceCalculator.millimetersToMeters(mm), 0.0001)
    }
}
