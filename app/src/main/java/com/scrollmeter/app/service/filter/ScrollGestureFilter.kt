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

    /**
     * Minimum movement threshold (3px) to bypass touch sensor electrical jitter.
     */
    const val MIN_JITTER_THRESHOLD_PX = 3.0

    /**
     * Validates and returns the calculated hypotenuse pixel distance if valid.
     * Returns null if the gesture is rejected (blacklisted package, micro-jitter, or anomaly spike).
     */
    fun processScrollGesture(
        packageName: String?,
        deltaX: Float,
        deltaY: Float
    ): Double? {
        if (PackageBlacklist.isBlacklisted(packageName)) {
            return null
        }

        if (!ScrollAnomalyFilter.isValidScroll(deltaX, deltaY)) {
            return null
        }

        val hypotenuse = DistanceCalculator.calculateHypotenusePixels(deltaX, deltaY)
        if (hypotenuse < MIN_JITTER_THRESHOLD_PX || hypotenuse > MAX_EVENT_THRESHOLD_PX) {
            return null
        }

        return hypotenuse
    }
}
