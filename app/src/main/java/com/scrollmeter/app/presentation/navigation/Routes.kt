package com.scrollmeter.app.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe destination routes for Navigation Compose.
 */
@Serializable
sealed interface ScreenRoute {
    @Serializable
    data object Home : ScreenRoute

    @Serializable
    data object Analytics : ScreenRoute

    @Serializable
    data object Settings : ScreenRoute

    @Serializable
    data object Permission : ScreenRoute

    @Serializable
    data object Achievements : ScreenRoute
}

// Aliases matching specific Phase 44 guide naming
typealias HomeRoute = ScreenRoute.Home
typealias AnalyticsRoute = ScreenRoute.Analytics
typealias SettingsRoute = ScreenRoute.Settings
typealias PermissionRoute = ScreenRoute.Permission
typealias AchievementsRoute = ScreenRoute.Achievements

