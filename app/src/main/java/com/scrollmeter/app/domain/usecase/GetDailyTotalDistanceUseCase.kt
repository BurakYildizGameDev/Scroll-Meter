package com.scrollmeter.app.domain.usecase

import com.scrollmeter.app.core.dispatchers.IoDispatcher
import com.scrollmeter.app.core.result.Result
import com.scrollmeter.app.domain.calculator.DistanceCalculator
import com.scrollmeter.app.domain.model.PhysicalDistance
import com.scrollmeter.app.domain.repository.IDeviceMetricsProvider
import com.scrollmeter.app.domain.repository.IScrollRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetDailyTotalDistanceUseCase @Inject constructor(
    private val scrollRepository: IScrollRepository,
    private val deviceMetricsProvider: IDeviceMetricsProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    operator fun invoke(epochDays: Long): Flow<Result<PhysicalDistance>> {
        return combine(
            scrollRepository.getDailyTotalPixels(epochDays),
            deviceMetricsProvider.getDevicePpi(),
            deviceMetricsProvider.getCalibrationMultiplier()
        ) { totalPixels, ppi, multiplier ->
            val mm = DistanceCalculator.pixelsToMillimeters(
                pixels = totalPixels,
                devicePpi = ppi,
                calibrationMultiplier = multiplier
            )
            PhysicalDistance(mm)
        }
            .map<PhysicalDistance, Result<PhysicalDistance>> { Result.Success(it) }
            .catch { emit(Result.Error(it)) }
            .flowOn(ioDispatcher)
    }
}
