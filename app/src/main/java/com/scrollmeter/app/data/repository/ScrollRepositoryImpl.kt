package com.scrollmeter.app.data.repository

import com.scrollmeter.app.core.dispatchers.IoDispatcher
import com.scrollmeter.app.data.local.dao.ScrollDao
import com.scrollmeter.app.data.resolver.AppNameResolver
import com.scrollmeter.app.domain.calculator.DistanceCalculator
import com.scrollmeter.app.domain.model.AppScrollSession
import com.scrollmeter.app.domain.model.PhysicalDistance
import com.scrollmeter.app.domain.repository.IDeviceMetricsProvider
import com.scrollmeter.app.domain.repository.IScrollRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScrollRepositoryImpl @Inject constructor(
    private val scrollDao: ScrollDao,
    private val appNameResolver: AppNameResolver,
    private val metricsProvider: IDeviceMetricsProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : IScrollRepository {

    override fun getDailyTotalPixels(epochDays: Long): Flow<Double> {
        return scrollDao.getDailyTotalPixels(epochDays)
            .map { it ?: 0.0 }
            .flowOn(ioDispatcher)
    }

    override fun getDailyAppSessions(epochDays: Long): Flow<List<AppScrollSession>> {
        return scrollDao.getDailySessions(epochDays)
            .map { entities ->
                entities.map { entity ->
                    AppScrollSession(
                        packageName = entity.packageName,
                        appDisplayName = appNameResolver.resolveDisplayName(entity.packageName),
                        totalPixels = entity.totalPixels,
                        physicalDistance = PhysicalDistance(entity.totalMilliMeters)
                    )
                }
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun recordScrollBatch(
        epochDays: Long,
        batch: Map<String, Double>
    ) = withContext(ioDispatcher) {
        if (batch.isEmpty()) return@withContext

        val ppi = metricsProvider.getDevicePpi().first()
        val multiplier = metricsProvider.getCalibrationMultiplier().first()
        val now = System.currentTimeMillis()

        batch.forEach { (packageName, pixels) ->
            if (pixels > 0.0) {
                val mm = DistanceCalculator.pixelsToMillimeters(pixels, ppi, multiplier)
                scrollDao.upsertScroll(
                    dateEpochDays = epochDays,
                    packageName = packageName,
                    pixelsToAdd = pixels,
                    mmToAdd = mm,
                    timestamp = now
                )
            }
        }
    }

    override suspend fun setDailyAppScroll(
        epochDays: Long,
        packageName: String,
        totalPixels: Double,
        totalMm: Double
    ) = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        scrollDao.setDailyScroll(
            dateEpochDays = epochDays,
            packageName = packageName,
            totalPixels = totalPixels,
            totalMm = totalMm,
            timestamp = now
        )
    }

    override suspend fun deleteRecordsOlderThan(cutoffEpochDays: Long) = withContext(ioDispatcher) {
        scrollDao.deleteRecordsOlderThan(cutoffEpochDays)
        Unit
    }
}
