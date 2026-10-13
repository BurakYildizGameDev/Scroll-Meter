package com.scrollmeter.app.domain.usecase

import com.scrollmeter.app.data.datastore.SettingsDataStore
import com.scrollmeter.app.data.local.dao.ScrollDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeedDemoDataUseCaseTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var scrollDao: ScrollDao
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var useCase: SeedDemoDataUseCase

    @BeforeEach
    fun setUp() {
        scrollDao = mockk(relaxed = true)
        settingsDataStore = mockk(relaxed = true)
        useCase = SeedDemoDataUseCase(
            scrollDao = scrollDao,
            settingsDataStore = settingsDataStore,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `invoke seeds mock telemetry and activates demo mode`() = runTest(testDispatcher) {
        useCase()

        // Verify multiple upsert calls to scrollDao
        coVerify(atLeast = 5) {
            scrollDao.upsertScroll(any(), any(), any(), any(), any())
        }

        // Verify demo mode, tracking mode, and onboarding flag activated
        coVerify(exactly = 1) { settingsDataStore.setDemoMode(true) }
        coVerify(exactly = 1) { settingsDataStore.setTrackingMode(com.scrollmeter.app.domain.model.TrackingMode.DEMO_PORTFOLIO) }
        coVerify(exactly = 1) { settingsDataStore.setOnboardingCompleted(true) }
    }
}
