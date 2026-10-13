package com.scrollmeter.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.scrollmeter.app.data.local.entity.DailyAppScrollEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScrollDao {

    @Query("""
        INSERT INTO daily_app_scroll (dateEpochDays, packageName, totalPixels, totalMilliMeters, lastUpdatedTimestamp)
        VALUES (:dateEpochDays, :packageName, :pixelsToAdd, :mmToAdd, :timestamp)
        ON CONFLICT(dateEpochDays, packageName) DO UPDATE SET
            totalPixels = daily_app_scroll.totalPixels + excluded.totalPixels,
            totalMilliMeters = daily_app_scroll.totalMilliMeters + excluded.totalMilliMeters,
            lastUpdatedTimestamp = excluded.lastUpdatedTimestamp
    """)
    suspend fun upsertScroll(
        dateEpochDays: Long,
        packageName: String,
        pixelsToAdd: Double,
        mmToAdd: Double,
        timestamp: Long
    )

    @Query("SELECT SUM(totalPixels) FROM daily_app_scroll WHERE dateEpochDays = :epochDays")
    fun getDailyTotalPixels(epochDays: Long): Flow<Double?>

    @Query("SELECT * FROM daily_app_scroll WHERE dateEpochDays = :epochDays ORDER BY totalPixels DESC")
    fun getDailySessions(epochDays: Long): Flow<List<DailyAppScrollEntity>>

    @Query("DELETE FROM daily_app_scroll WHERE dateEpochDays < :cutoffEpochDays")
    suspend fun deleteRecordsOlderThan(cutoffEpochDays: Long): Int
}
