package com.scrollmeter.app.domain.model

import java.util.Locale

data class PhysicalDistance(val totalMilliMeters: Double) {
    val inMeters: Double
        get() = totalMilliMeters / 1000.0

    val inCentimeters: Double
        get() = totalMilliMeters / 10.0

    val inKilometers: Double
        get() = totalMilliMeters / 1_000_000.0

    val formattedCentimeters: String
        get() = String.format(Locale.US, "%.1f cm", inCentimeters)

    val formattedMeters: String
        get() = String.format(Locale.US, "%.2f m", inMeters)

    val formattedKilometers: String
        get() = String.format(Locale.US, "%.3f km", inKilometers)

    val formattedAdaptive: String
        get() = when {
            inMeters >= 1000.0 -> formattedKilometers
            inMeters >= 1.0 -> formattedMeters
            else -> formattedCentimeters
        }

    operator fun plus(other: PhysicalDistance): PhysicalDistance =
        PhysicalDistance(this.totalMilliMeters + other.totalMilliMeters)

    companion object {
        val ZERO = PhysicalDistance(0.0)
    }
}
