package com.scrollmeter.app.domain.usecase

import com.scrollmeter.app.core.dispatchers.IoDispatcher
import com.scrollmeter.app.data.datastore.SettingsDataStore
import com.scrollmeter.app.domain.calculator.ScrollCadenceEngine
import com.scrollmeter.app.domain.repository.IDeviceMetricsProvider
import com.scrollmeter.app.domain.repository.IScrollRepository
import com.scrollmeter.app.domain.repository.IUsageStatsProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * Synchronizes Android UsageStats foreground minutes into real physical scroll distance
 * using the empirical Doomscroll Cadence Engine.
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

    suspend operator fun invoke(): Boolean = withContext(ioDispatcher) {
        if (!usageStatsProvider.isUsageAccessGranted()) {
            return@withContext false
        }

        val usageMap = usageStatsProvider.queryTodayForegroundMillis()
        if (usageMap.isEmpty()) {
            return@withContext true
        }

        val ppi = deviceMetricsProvider.getDevicePpi().first()
        val calibrationMultiplier = deviceMetricsProvider.getCalibrationMultiplier().first()
        val speedMultiplier = settingsDataStore.cadenceSpeedMultiplier.first()
        val epochDays = epochDaysSupplier()

        try {
            for ((packageName, foregroundMillis) in usageMap) {
                if (foregroundMillis > 0L) {
                    val meters = ScrollCadenceEngine.calculateMeters(
                        packageName = packageName,
                        foregroundMillis = foregroundMillis,
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
}
