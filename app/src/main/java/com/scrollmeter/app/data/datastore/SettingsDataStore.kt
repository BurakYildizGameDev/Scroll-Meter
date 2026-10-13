package com.scrollmeter.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scroll_meter_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    val calibrationMultiplier: Flow<Float> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_CALIBRATION_MULTIPLIER] ?: 1.0f
        }

    val dailyGoalMeters: Flow<Float> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_DAILY_GOAL_METERS] ?: 100.0f
        }

    val themeMode: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_THEME_MODE] ?: 0
        }

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_IS_ONBOARDING_COMPLETED] ?: false
        }

    val isDemoModeActive: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_IS_DEMO_MODE] ?: false
        }

    suspend fun setCalibrationMultiplier(multiplier: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_CALIBRATION_MULTIPLIER] = multiplier.coerceAtLeast(0.1f)
        }
    }

    suspend fun setDailyGoalMeters(meters: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_DAILY_GOAL_METERS] = meters.coerceAtLeast(1.0f)
        }
    }

    suspend fun setThemeMode(mode: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setDemoMode(active: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_DEMO_MODE] = active
        }
    }

    companion object {
        val KEY_CALIBRATION_MULTIPLIER = floatPreferencesKey("calibration_multiplier")
        val KEY_DAILY_GOAL_METERS = floatPreferencesKey("daily_goal_meters")
        val KEY_THEME_MODE = intPreferencesKey("theme_mode")
        val KEY_IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val KEY_IS_DEMO_MODE = booleanPreferencesKey("is_demo_mode")
    }
}
