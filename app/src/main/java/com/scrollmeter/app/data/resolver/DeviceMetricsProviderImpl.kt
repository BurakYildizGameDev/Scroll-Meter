package com.scrollmeter.app.data.resolver

import android.content.Context
import android.content.res.Resources
import com.scrollmeter.app.data.datastore.SettingsDataStore
import com.scrollmeter.app.domain.repository.IDeviceMetricsProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceMetricsProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore
) : IDeviceMetricsProvider {

    private val hardwarePpi: Float by lazy {
        val displayMetrics = Resources.getSystem().displayMetrics
        val xdpi = displayMetrics.xdpi
        val ydpi = displayMetrics.ydpi
        val averagePpi = if (xdpi > 0f && ydpi > 0f) {
            (xdpi + ydpi) / 2.0f
        } else {
            displayMetrics.densityDpi.toFloat()
        }
        averagePpi.coerceAtLeast(160.0f)
    }

    override fun getDevicePpi(): Flow<Float> = flowOf(hardwarePpi)

    override fun getCalibrationMultiplier(): Flow<Float> =
        settingsDataStore.calibrationMultiplier

    override suspend fun updateCalibrationMultiplier(multiplier: Float) {
        settingsDataStore.setCalibrationMultiplier(multiplier)
    }
}
