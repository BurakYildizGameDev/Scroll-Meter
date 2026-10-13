package com.scrollmeter.app.domain.repository

import com.scrollmeter.app.domain.model.AppScrollSession
import kotlinx.coroutines.flow.Flow

interface IScrollRepository {
    fun getDailyTotalPixels(epochDays: Long): Flow<Double>
    fun getDailyAppSessions(epochDays: Long): Flow<List<AppScrollSession>>
    suspend fun recordScrollBatch(epochDays: Long, batch: Map<String, Double>)
    suspend fun setDailyAppScroll(epochDays: Long, packageName: String, totalPixels: Double, totalMm: Double)
    suspend fun deleteRecordsOlderThan(cutoffEpochDays: Long)
    suspend fun clearDailyScroll(epochDays: Long)
    suspend fun deletePackage(packageName: String)
}
