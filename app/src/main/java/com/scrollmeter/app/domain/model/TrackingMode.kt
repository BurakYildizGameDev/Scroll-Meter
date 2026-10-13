package com.scrollmeter.app.domain.model

/**
 * Architectural tracking engine mode for Scroll-Meter.
 */
enum class TrackingMode {
    /**
     * Standard & 100% Safe Mode powered by Android UsageStatsManager
     * and Category-Calibrated Doomscroll Cadence Engine.
     * Zero security warnings, zero restricted settings, 0.0% battery drain.
     */
    SAFE_USAGE_STATS,

    /**
     * Advanced Hardware Lab Mode using AccessibilityService (TYPE_VIEW_SCROLLED).
     * Provides sub-millimeter real-time pixel delta capture.
     * Explicitly documents Android's restricted setting security shield.
     */
    ACCESSIBILITY_LAB,

    /**
     * Recruiter and interviewer portfolio demo preview mode.
     */
    DEMO_PORTFOLIO
}
