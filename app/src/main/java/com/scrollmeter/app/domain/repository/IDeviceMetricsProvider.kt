package com.scrollmeter.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface IDeviceMetricsProvider {
    fun getDevicePpi(): Flow<Float>
    fun getCalibrationMultiplier(): Flow<Float>
    suspend fun updateCalibrationMultiplier(multiplier: Float)
}
