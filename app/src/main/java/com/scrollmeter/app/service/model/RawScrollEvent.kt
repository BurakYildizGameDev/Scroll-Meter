package com.scrollmeter.app.service.model

/**
 * Represents an in-memory raw scroll motion detected by the accessibility service.
 *
 * @param packageName The application package where scrolling occurred.
 * @param deltaX Horizontal displacement in pixels.
 * @param deltaY Vertical displacement in pixels.
 * @param timestamp Event epoch timestamp in milliseconds.
 */
data class RawScrollEvent(
    val packageName: String,
    val deltaX: Float,
    val deltaY: Float,
    val timestamp: Long = System.currentTimeMillis()
)
