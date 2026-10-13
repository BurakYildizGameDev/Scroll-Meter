package com.scrollmeter.app.presentation.navigation

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RoutesTest {

    @Test
    fun `routes serialize and deserialize cleanly`() {
        val homeJson = Json.encodeToString<ScreenRoute>(ScreenRoute.Home)
        val analyticsJson = Json.encodeToString<ScreenRoute>(ScreenRoute.Analytics)
        val settingsJson = Json.encodeToString<ScreenRoute>(ScreenRoute.Settings)
        val permissionJson = Json.encodeToString<ScreenRoute>(ScreenRoute.Permission)

        assertTrue(homeJson.isNotEmpty())
        assertTrue(analyticsJson.isNotEmpty())
        assertTrue(settingsJson.isNotEmpty())
        assertTrue(permissionJson.isNotEmpty())

        val decodedHome = Json.decodeFromString<ScreenRoute>(homeJson)
        assertNotNull(decodedHome)
    }
}
