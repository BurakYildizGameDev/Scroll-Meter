package com.scrollmeter.app.service.filter

import com.scrollmeter.app.domain.calculator.DistanceCalculator
import com.scrollmeter.app.domain.filter.ScrollAnomalyFilter

/**
 * Service-level gesture filter that evaluates scroll events to eliminate
 * micro-jitters, system packages, pinch-to-zoom spikes, and map anomalies.
 */
object ScrollGestureFilter {

    /**
     * Maximum single-event scroll displacement threshold (5000px)
     * as mandated by the production specification.
     */
    const val MAX_EVENT_THRESHOLD_PX = 5000.0
    const val MIN_JITTER_THRESHOLD_PX = 3.0
    const val HIGH_REFRESH_MIN_JITTER_PX = 1.0

    /**
     * Validates and returns the calculated hypotenuse pixel distance if valid.
     * Supports high-refresh rate (90Hz/120Hz/144Hz) displays.
     */
    fun processScrollGesture(
        packageName: String?,
        deltaX: Float,
        deltaY: Float,
        isHighRefreshRate: Boolean = false
    ): Double? {
        if (PackageBlacklist.isBlacklisted(packageName)) {
            return null
        }

        if (!ScrollAnomalyFilter.isValidScroll(deltaX, deltaY, isHighRefreshRate)) {
            return null
        }

        val hypotenuse = DistanceCalculator.calculateHypotenusePixels(deltaX, deltaY)
        val minThreshold = if (isHighRefreshRate) HIGH_REFRESH_MIN_JITTER_PX else MIN_JITTER_THRESHOLD_PX
        if (hypotenuse < minThreshold || hypotenuse > MAX_EVENT_THRESHOLD_PX) {
            return null
        }

        return hypotenuse
    }
}
