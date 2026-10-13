package com.scrollmeter.app.domain.usecase

import com.scrollmeter.app.core.dispatchers.IoDispatcher
import com.scrollmeter.app.core.result.Result
import com.scrollmeter.app.data.datastore.SettingsDataStore
import com.scrollmeter.app.domain.model.Achievement
import com.scrollmeter.app.domain.model.AchievementCategory
import com.scrollmeter.app.domain.repository.IScrollRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.math.min

class GetAchievementsUseCase @Inject constructor(
    private val getDailyTotalDistanceUseCase: GetDailyTotalDistanceUseCase,
    private val scrollRepository: IScrollRepository,
    private val settingsDataStore: SettingsDataStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    operator fun invoke(epochDays: Long): Flow<Result<List<Achievement>>> {
        return combine(
            getDailyTotalDistanceUseCase(epochDays),
            scrollRepository.getDailyAppSessions(epochDays),
            settingsDataStore.calibrationMultiplier
        ) { distanceResult, sessions, calibration ->
            val todayMeters = if (distanceResult is Result.Success) distanceResult.data.inMeters else 0.0
            val distinctAppsCount = sessions.filter { it.totalPixels > 0.0 }.size
            val maxSingleAppMeters = sessions.maxOfOrNull { it.physicalDistance.inMeters } ?: 0.0

            val achievements = listOf(
                // 1. First Step
                Achievement(
                    id = "FIRST_STEP",
                    title = "First Step",
                    description = "Scroll your first 100 meters across any app.",
                    iconEmoji = "🥉",
                    category = AchievementCategory.MILESTONE,
                    isUnlocked = todayMeters >= 100.0,
                    progress = min(1.0f, (todayMeters / 100.0).toFloat()),
                    progressText = "${String.format("%.1f", min(todayMeters, 100.0))}m / 100m"
                ),
                // 2. Giraffe Club
                Achievement(
                    id = "GIRAFFE_CLUB",
                    title = "Giraffe Club",
                    description = "Outscroll the tallest living land animal (5.5m).",
                    iconEmoji = "🦒",
                    category = AchievementCategory.MILESTONE,
                    isUnlocked = todayMeters >= 5.5,
                    progress = min(1.0f, (todayMeters / 5.5).toFloat()),
                    progressText = "${String.format("%.1f", min(todayMeters, 5.5))}m / 5.5m"
                ),
                // 3. Statue of Liberty
                Achievement(
                    id = "STATUE_OF_LIBERTY",
                    title = "Liberty Torch",
                    description = "Climb past the Statue of Liberty pedestal (93m).",
                    iconEmoji = "🗽",
                    category = AchievementCategory.MILESTONE,
                    isUnlocked = todayMeters >= 93.0,
                    progress = min(1.0f, (todayMeters / 93.0).toFloat()),
                    progressText = "${String.format("%.1f", min(todayMeters, 93.0))}m / 93m"
                ),
                // 4. Eiffel Climber
                Achievement(
                    id = "EIFFEL_CLIMBER",
                    title = "Eiffel Climber",
                    description = "Scale the tip of the Eiffel Tower (330m) in a single day.",
                    iconEmoji = "🗼",
                    category = AchievementCategory.MILESTONE,
                    isUnlocked = todayMeters >= 330.0,
                    progress = min(1.0f, (todayMeters / 330.0).toFloat()),
                    progressText = "${String.format("%.1f", min(todayMeters, 330.0))}m / 330m"
                ),
                // 5. Burj Khalifa
                Achievement(
                    id = "BURJ_KHALIFA",
                    title = "Cloud Piercer",
                    description = "Reach the observation deck of Burj Khalifa (828m).",
                    iconEmoji = "🏙️",
                    category = AchievementCategory.MILESTONE,
                    isUnlocked = todayMeters >= 828.0,
                    progress = min(1.0f, (todayMeters / 828.0).toFloat()),
                    progressText = "${String.format("%.1f", min(todayMeters, 828.0))}m / 828m"
                ),
                // 6. Marathon Runner
                Achievement(
                    id = "MARATHON_RUNNER",
                    title = "Thumb Marathon",
                    description = "Reach 1,000 meters in a single high-intensity day.",
                    iconEmoji = "🏃‍♂️",
                    category = AchievementCategory.HABIT,
                    isUnlocked = todayMeters >= 1000.0,
                    progress = min(1.0f, (todayMeters / 1000.0).toFloat()),
                    progressText = "${String.format("%.1f", min(todayMeters, 1000.0))}m / 1000m"
                ),
                // 7. Speed Demon
                Achievement(
                    id = "SPEED_DEMON",
                    title = "Speed Demon",
                    description = "Accumulate over 250m inside a single dominant application.",
                    iconEmoji = "⚡",
                    category = AchievementCategory.HABIT,
                    isUnlocked = maxSingleAppMeters >= 250.0,
                    progress = min(1.0f, (maxSingleAppMeters / 250.0).toFloat()),
                    progressText = "${String.format("%.1f", min(maxSingleAppMeters, 250.0))}m / 250m"
                ),
                // 8. Social Explorer
                Achievement(
                    id = "SOCIAL_EXPLORER",
                    title = "Social Explorer",
                    description = "Log active scroll sessions across at least 3 distinct apps.",
                    iconEmoji = "📱",
                    category = AchievementCategory.HABIT,
                    isUnlocked = distinctAppsCount >= 3,
                    progress = min(1.0f, distinctAppsCount.toFloat() / 3f),
                    progressText = "$distinctAppsCount / 3 apps"
                ),
                // 9. Precision Tuner
                Achievement(
                    id = "PRECISION_TUNER",
                    title = "Precision Tuner",
                    description = "Calibrate screen physical scale using the on-screen ruler.",
                    iconEmoji = "📐",
                    category = AchievementCategory.PRECISION,
                    isUnlocked = calibration != 1.0f,
                    progress = if (calibration != 1.0f) 1.0f else 0.0f,
                    progressText = if (calibration != 1.0f) "Calibrated" else "Not calibrated"
                )
            )

            Result.Success(achievements) as Result<List<Achievement>>
        }
            .catch { emit(Result.Error(it)) }
            .flowOn(ioDispatcher)
    }
}
