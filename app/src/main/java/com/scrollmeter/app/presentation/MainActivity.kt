package com.scrollmeter.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.scrollmeter.app.presentation.navigation.ScreenRoute
import com.scrollmeter.app.presentation.navigation.ScrollNavHost
import com.scrollmeter.app.presentation.onboarding.isAccessibilityPermissionGranted
import com.scrollmeter.app.presentation.theme.ObsidianVoid
import com.scrollmeter.app.presentation.theme.ScrollMeterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val hasPermission = isAccessibilityPermissionGranted(this)
        val initialDestination = if (hasPermission) ScreenRoute.Home else ScreenRoute.Permission

        setContent {
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
