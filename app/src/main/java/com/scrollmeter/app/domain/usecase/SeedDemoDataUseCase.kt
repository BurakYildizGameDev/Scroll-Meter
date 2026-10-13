package com.scrollmeter.app.domain.usecase

import com.scrollmeter.app.core.dispatchers.IoDispatcher
import com.scrollmeter.app.data.datastore.SettingsDataStore
import com.scrollmeter.app.data.local.dao.ScrollDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

/**
 * Seeds realistic telemetry and unlocks achievement badges for recruiter preview
 * and portfolio demonstrations without requiring Android Accessibility Service permissions.
 */
class SeedDemoDataUseCase @Inject constructor(
    private val scrollDao: ScrollDao,
    private val settingsDataStore: SettingsDataStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke() = withContext(ioDispatcher) {
        val todayEpoch = LocalDate.now().toEpochDay()
        val now = System.currentTimeMillis()

        // Today's dominant doomscroll sessions (~1,655 meters total)
        scrollDao.upsertScroll(todayEpoch, "com.zhiliaoapp.musically", 4350000.0, 650000.0, now)
        scrollDao.upsertScroll(todayEpoch, "com.instagram.android", 3200000.0, 480000.0, now)
        scrollDao.upsertScroll(todayEpoch, "com.twitter.android", 1870000.0, 280000.0, now)
        scrollDao.upsertScroll(todayEpoch, "com.reddit.frontpage", 1000000.0, 150000.0, now)
        scrollDao.upsertScroll(todayEpoch, "com.android.chrome", 635000.0, 95000.0, now)

        // Historical sessions for weekly bar chart visualization
        scrollDao.upsertScroll(todayEpoch - 1, "com.instagram.android", 3500000.0, 520000.0, now)
        scrollDao.upsertScroll(todayEpoch - 1, "com.zhiliaoapp.musically", 2000000.0, 300000.0, now)

        scrollDao.upsertScroll(todayEpoch - 2, "com.twitter.android", 5000000.0, 750000.0, now)
        scrollDao.upsertScroll(todayEpoch - 2, "com.reddit.frontpage", 2700000.0, 400000.0, now)

        scrollDao.upsertScroll(todayEpoch - 3, "com.google.android.youtube", 3600000.0, 540000.0, now)
        scrollDao.upsertScroll(todayEpoch - 3, "com.android.chrome", 1400000.0, 210000.0, now)

        scrollDao.upsertScroll(todayEpoch - 4, "com.instagram.android", 6100000.0, 910000.0, now)

        // Enable Demo Mode & complete onboarding
        settingsDataStore.setDemoMode(true)
        settingsDataStore.setTrackingMode(com.scrollmeter.app.domain.model.TrackingMode.DEMO_PORTFOLIO)
        settingsDataStore.setOnboardingCompleted(true)
    }
}
