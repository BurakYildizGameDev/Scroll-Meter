package com.scrollmeter.app.domain.repository

/**
 * Provider contract for accessing Android UsageStatsManager
 * to compute foreground session durations in 100% Safe Mode.
 */
interface IUsageStatsProvider {
    fun isUsageAccessGranted(): Boolean
    fun queryTodayForegroundMillis(): Map<String, Long>
}
