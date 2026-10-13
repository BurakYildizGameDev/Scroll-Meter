package com.scrollmeter.app.domain.usecase

import app.cash.turbine.test
import com.scrollmeter.app.core.result.Result
import com.scrollmeter.app.domain.model.AppScrollSession
import com.scrollmeter.app.domain.model.PhysicalDistance
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

class GetAppLeaderboardUseCaseTest {

    private val scrollRepository: IScrollRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var useCase: GetAppLeaderboardUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetAppLeaderboardUseCase(
            scrollRepository = scrollRepository,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `usecase sorts sessions descending by totalPixels and excludes zero pixels`() = runTest(testDispatcher) {
        val epochDay = 19800L
        val rawSessions = listOf(
            AppScrollSession("com.twitter", "X", 300.0, PhysicalDistance(30.0)),
            AppScrollSession("com.instagram", "Instagram", 1200.0, PhysicalDistance(120.0)),
            AppScrollSession("com.empty", "Empty", 0.0, PhysicalDistance.ZERO),
            AppScrollSession("com.reddit", "Reddit", 750.0, PhysicalDistance(75.0))
        )

        every { scrollRepository.getDailyAppSessions(epochDay) } returns flowOf(rawSessions)

        useCase(epochDay).test {
            val result = awaitItem()
            assertInstanceOf(Result.Success::class.java, result)
            val list = (result as Result.Success).data
            assertEquals(3, list.size)
            assertEquals("com.instagram", list[0].packageName)
            assertEquals("com.reddit", list[1].packageName)
            assertEquals("com.twitter", list[2].packageName)
            awaitComplete()
        }
    }
}
