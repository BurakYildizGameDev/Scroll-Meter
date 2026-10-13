package com.scrollmeter.app.domain.filter

import com.scrollmeter.app.domain.calculator.DistanceCalculator

object ScrollAnomalyFilter {
    const val MIN_JITTER_THRESHOLD_PX = 3.0
    const val MAX_DELTA_SPIKE_THRESHOLD_PX = 5000.0

    /**
     * Filters out touch micro-jitter and abnormal coordinate spikes.
     * Returns true if the scroll delta is physically plausible.
     */
    fun isValidScroll(deltaX: Float, deltaY: Float): Boolean {
        if (deltaX.isNaN() || deltaY.isNaN() || deltaX.isInfinite() || deltaY.isInfinite()) {
            return false
        }
        val hypotenuse = DistanceCalculator.calculateHypotenusePixels(deltaX, deltaY)
        return hypotenuse in MIN_JITTER_THRESHOLD_PX..MAX_DELTA_SPIKE_THRESHOLD_PX
    }
}
