package com.scrollmeter.app.domain.usecase

import app.cash.turbine.test
import com.scrollmeter.app.core.result.Result
import com.scrollmeter.app.domain.model.PhysicalDistance
import com.scrollmeter.app.domain.repository.IDeviceMetricsProvider
import com.scrollmeter.app.domain.repository.IScrollRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetDailyTotalDistanceUseCaseTest {

    private val scrollRepository: IScrollRepository = mockk()
    private val deviceMetricsProvider: IDeviceMetricsProvider = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var useCase: GetDailyTotalDistanceUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetDailyTotalDistanceUseCase(
            scrollRepository = scrollRepository,
            deviceMetricsProvider = deviceMetricsProvider,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `usecase combines pixel flow and PPI into calculated PhysicalDistance Result`() = runTest(testDispatcher) {
        val epochDay = 19800L
        // At 254 PPI, 2540 pixels = 10 inches = 254 mm
        every { scrollRepository.getDailyTotalPixels(epochDay) } returns flowOf(2540.0)
        every { deviceMetricsProvider.getDevicePpi() } returns flowOf(254.0f)
        every { deviceMetricsProvider.getCalibrationMultiplier() } returns flowOf(1.0f)

        useCase(epochDay).test {
            val result = awaitItem()
            assertInstanceOf(Result.Success::class.java, result)
            val distance = (result as Result.Success).data
            assertEquals(254.0, distance.totalMilliMeters, 0.001)
            assertEquals(0.254, distance.inMeters, 0.001)
            awaitComplete()
        }
    }
}
