package com.scrollmeter.app.domain.calculator

import kotlin.math.abs
import kotlin.math.sqrt

object DistanceCalculator {
    const val MM_PER_INCH = 25.4
    const val MIN_EFFECTIVE_PPI = 1.0f

    /**
     * Calculates the scalar hypotenuse pixel distance from Cartesian deltas.
     * Uses fast-path optimization when one of the deltas is zero to avoid sqrt.
     */
    fun calculateHypotenusePixels(deltaX: Float, deltaY: Float): Double {
        val dx = abs(deltaX.toDouble())
        val dy = abs(deltaY.toDouble())
        return when {
            dx == 0.0 -> dy
            dy == 0.0 -> dx
            else -> sqrt(dx * dx + dy * dy)
        }
    }

    /**
     * Converts pixel distance into physical millimeters using calibrated device PPI.
     * Guaranteed to never divide by zero or negative PPI.
     */
    fun pixelsToMillimeters(
        pixels: Double,
        devicePpi: Float,
        calibrationMultiplier: Float = 1.0f
    ): Double {
        if (pixels <= 0.0) return 0.0
        val effectivePpi = (devicePpi * calibrationMultiplier).coerceAtLeast(MIN_EFFECTIVE_PPI)
        return (pixels * MM_PER_INCH) / effectivePpi
    }

    fun millimetersToMeters(mm: Double): Double = (mm / 1000.0).coerceAtLeast(0.0)

    fun millimetersToKilometers(mm: Double): Double = (mm / 1_000_000.0).coerceAtLeast(0.0)
}
