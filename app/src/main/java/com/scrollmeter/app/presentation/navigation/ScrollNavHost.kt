package com.scrollmeter.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.scrollmeter.app.presentation.dashboard.DashboardScreen
import com.scrollmeter.app.presentation.onboarding.PermissionScreen

/**
 * Type-Safe Navigation Host for Scroll-Meter.
 */
@Composable
fun ScrollNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: ScreenRoute = ScreenRoute.Home
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<ScreenRoute.Permission> {
            PermissionScreen(
                onPermissionGranted = {
                    navController.navigate(ScreenRoute.Home) {
                        popUpTo<ScreenRoute.Permission> { inclusive = true }
                    }
                }
            )
        }

        composable<ScreenRoute.Home> {
            DashboardScreen(
                onNavigateToAnalytics = {
                    navController.navigate(ScreenRoute.Analytics)
                },
                onNavigateToSettings = {
                    navController.navigate(ScreenRoute.Settings)
                }
            )
        }

        composable<ScreenRoute.Analytics> {
            com.scrollmeter.app.presentation.analytics.AnalyticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<ScreenRoute.Settings> {
            com.scrollmeter.app.presentation.settings.SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
