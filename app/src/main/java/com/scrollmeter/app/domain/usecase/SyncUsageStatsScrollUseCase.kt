package com.scrollmeter.app.domain.usecase

import com.scrollmeter.app.core.dispatchers.IoDispatcher
import com.scrollmeter.app.data.datastore.SettingsDataStore
import com.scrollmeter.app.domain.calculator.ScrollCadenceEngine
import com.scrollmeter.app.domain.repository.IDeviceMetricsProvider
import com.scrollmeter.app.domain.repository.IScrollRepository
import com.scrollmeter.app.domain.repository.IUsageStatsProvider
import com.scrollmeter.app.service.filter.PackageBlacklist
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * Synchronizes Android UsageStats foreground minutes into real physical scroll distance
 * using the empirical Doomscroll Cadence Engine with Baseline Snapshot Protection.
 *
 * Runs exclusively in 100% Safe Mode (PACKAGE_USAGE_STATS).
 */
class SyncUsageStatsScrollUseCase @Inject constructor(
    private val usageStatsProvider: IUsageStatsProvider,
    private val scrollRepository: IScrollRepository,
    private val deviceMetricsProvider: IDeviceMetricsProvider,
    private val settingsDataStore: SettingsDataStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    var epochDaysSupplier: () -> Long = { LocalDate.now().toEpochDay() }

    suspend operator fun invoke(forceResetBaseline: Boolean = false): Boolean = withContext(ioDispatcher) {
        if (!usageStatsProvider.isUsageAccessGranted()) {
            return@withContext false
        }

        val rawUsageMap = usageStatsProvider.queryTodayForegroundMillis()
        val usageMap = rawUsageMap.filterKeys { !PackageBlacklist.isBlacklisted(it) }
        val epochDays = epochDaysSupplier()

        // Always purge any stray OS framework entries from the local database
        scrollRepository.deletePackage("android")

        val savedBaselineEpochDay = settingsDataStore.usageBaselineEpochDay.first()
        val savedBaselineData = settingsDataStore.usageBaselineData.first()

        // Initial Start Protection (Day 1 install or forced reset):
        // Avoid retroactively calculating whole-day history from before Scroll-Meter was active.
        if (savedBaselineEpochDay == -1L || forceResetBaseline) {
            scrollRepository.clearDailyScroll(epochDays)
            settingsDataStore.setUsageBaseline(epochDays, serializeBaseline(usageMap))
            return@withContext true
        }

        if (usageMap.isEmpty()) {
            return@withContext true
        }

        val ppi = deviceMetricsProvider.getDevicePpi().first()
        val calibrationMultiplier = deviceMetricsProvider.getCalibrationMultiplier().first()
        val speedMultiplier = settingsDataStore.cadenceSpeedMultiplier.first()

        val baselineMap = if (savedBaselineEpochDay == epochDays) {
            parseBaseline(savedBaselineData)
        } else {
            // New day: Android UsageStatsManager starts from 0 at midnight.
            settingsDataStore.setUsageBaseline(epochDays, "")
            emptyMap()
        }

        try {
            for ((packageName, foregroundMillis) in usageMap) {
                val baseline = baselineMap[packageName] ?: 0L
                val effectiveForegroundMillis = (foregroundMillis - baseline).coerceAtLeast(0L)

                if (effectiveForegroundMillis > 0L) {
                    val meters = ScrollCadenceEngine.calculateMeters(
                        packageName = packageName,
                        foregroundMillis = effectiveForegroundMillis,
                        speedMultiplier = speedMultiplier,
                        calibrationMultiplier = calibrationMultiplier
                    )
                    val mm = meters * 1000.0
                    val pixels = ScrollCadenceEngine.metersToPixels(meters, ppi)

                    scrollRepository.setDailyAppScroll(
                        epochDays = epochDays,
                        packageName = packageName,
                        totalPixels = pixels,
                        totalMm = mm
                    )
                }
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error syncing usage stats scroll telemetry")
            false
        }
    }

    private fun serializeBaseline(map: Map<String, Long>): String {
        return map.entries.joinToString("|") { "${it.key}=${it.value}" }
    }

    private fun parseBaseline(data: String): Map<String, Long> {
        if (data.isBlank()) return emptyMap()
        return try {
            data.split("|").mapNotNull { entry ->
                val parts = entry.split("=")
                if (parts.size == 2) {
                    parts[0] to (parts[1].toLongOrNull() ?: 0L)
                } else null
            }.toMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
