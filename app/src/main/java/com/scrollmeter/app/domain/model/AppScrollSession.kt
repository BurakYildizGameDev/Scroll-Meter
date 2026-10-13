package com.scrollmeter.app.domain.model

data class AppScrollSession(
    val packageName: String,
    val appDisplayName: String,
    val totalPixels: Double,
    val physicalDistance: PhysicalDistance
)
