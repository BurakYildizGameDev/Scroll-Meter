package com.scrollmeter.app.domain.filter

import com.scrollmeter.app.domain.calculator.DistanceCalculator

object ScrollAnomalyFilter {
    const val MIN_JITTER_THRESHOLD_PX = 3.0
    const val HIGH_REFRESH_MIN_JITTER_PX = 1.0
    const val MAX_DELTA_SPIKE_THRESHOLD_PX = 5000.0

    /**
     * Filters out touch micro-jitter and abnormal coordinate spikes.
     * Supports high-refresh rate (90Hz/120Hz/144Hz) displays with 1.0px threshold.
     */
    fun isValidScroll(deltaX: Float, deltaY: Float, isHighRefreshRate: Boolean = false): Boolean {
        if (deltaX.isNaN() || deltaY.isNaN() || deltaX.isInfinite() || deltaY.isInfinite()) {
            return false
        }
        val hypotenuse = DistanceCalculator.calculateHypotenusePixels(deltaX, deltaY)
        val minThreshold = if (isHighRefreshRate) HIGH_REFRESH_MIN_JITTER_PX else MIN_JITTER_THRESHOLD_PX
        return hypotenuse in minThreshold..MAX_DELTA_SPIKE_THRESHOLD_PX
    }
}
