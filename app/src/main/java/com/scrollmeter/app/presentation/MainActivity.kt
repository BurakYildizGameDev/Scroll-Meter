package com.scrollmeter.app.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scrollmeter.app.data.datastore.SettingsDataStore
import com.scrollmeter.app.domain.model.TrackingMode
import com.scrollmeter.app.domain.repository.IUsageStatsProvider
import com.scrollmeter.app.presentation.navigation.ScreenRoute
import com.scrollmeter.app.presentation.navigation.ScrollNavHost
import com.scrollmeter.app.presentation.onboarding.isAccessibilityPermissionGranted
import com.scrollmeter.app.presentation.theme.ObsidianVoid
import com.scrollmeter.app.presentation.theme.ScrollMeterTheme
import com.scrollmeter.app.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var settingsDataStore: SettingsDataStore
    @Inject lateinit var usageStatsProvider: IUsageStatsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val savedLang = runBlocking {
            try {
                settingsDataStore.selectedLanguage.first()
            } catch (_: Exception) {
                ""
            }
        }
        if (savedLang.isNotBlank()) {
            LocaleHelper.applyLocale(this, savedLang)
        }

        val isDemoActive = runBlocking {
            try {
                settingsDataStore.isDemoModeActive.first()
            } catch (_: Exception) {
                false
            }
        }
        val trackingMode = runBlocking {
            try {
                settingsDataStore.trackingMode.first()
            } catch (_: Exception) {
                TrackingMode.SAFE_USAGE_STATS
            }
        }
        val hasUsage = usageStatsProvider.isUsageAccessGranted()
        val hasAccessibility = isAccessibilityPermissionGranted(this)

        val hasValidPermission = when (trackingMode) {
            TrackingMode.SAFE_USAGE_STATS -> hasUsage
            TrackingMode.ACCESSIBILITY_LAB -> hasAccessibility
            TrackingMode.DEMO_PORTFOLIO -> true
        }

        val initialDestination = if (hasValidPermission || isDemoActive) ScreenRoute.Home else ScreenRoute.Permission

        setContent {
            val selectedLanguage by settingsDataStore.selectedLanguage.collectAsStateWithLifecycle(initialValue = savedLang)
            val baseContext = LocalContext.current
            val localizedContext = remember(selectedLanguage) {
                LocaleHelper.getLocalizedContext(baseContext, selectedLanguage)
            }
            val localizedConfig = remember(selectedLanguage) {
                LocaleHelper.getLocalizedConfiguration(baseContext, selectedLanguage)
            }

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfig,
                LocalContext provides localizedContext
            ) {
                ScrollMeterTheme(darkTheme = true) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = ObsidianVoid
                    ) {
                        ScrollNavHost(
                            startDestination = initialDestination
                        )
                    }
                }
            }
        }
    }
}
