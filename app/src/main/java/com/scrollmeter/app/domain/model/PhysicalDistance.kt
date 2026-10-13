package com.scrollmeter.app.domain.model

import java.util.Locale

data class PhysicalDistance(val totalMilliMeters: Double) {
    val inMeters: Double
        get() = totalMilliMeters / 1000.0

    val inKilometers: Double
        get() = totalMilliMeters / 1_000_000.0

    val formattedMeters: String
        get() = String.format(Locale.US, "%.2f m", inMeters)

    val formattedKilometers: String
        get() = String.format(Locale.US, "%.3f km", inKilometers)

    val formattedAdaptive: String
        get() = if (inMeters >= 1000.0) formattedKilometers else formattedMeters

    operator fun plus(other: PhysicalDistance): PhysicalDistance =
        PhysicalDistance(this.totalMilliMeters + other.totalMilliMeters)

    companion object {
        val ZERO = PhysicalDistance(0.0)
    }
}
