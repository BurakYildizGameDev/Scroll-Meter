package com.scrollmeter.app.service.filter

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PackageBlacklistTest {

    @Test
    fun `isBlacklisted should return true for null or empty strings`() {
        assertTrue(PackageBlacklist.isBlacklisted(null))
        assertTrue(PackageBlacklist.isBlacklisted(""))
        assertTrue(PackageBlacklist.isBlacklisted("   "))
    }

    @Test
    fun `isBlacklisted should return true for system packages and self package`() {
        assertTrue(PackageBlacklist.isBlacklisted("android"))
        assertTrue(PackageBlacklist.isBlacklisted("com.android.systemui"))
        assertTrue(PackageBlacklist.isBlacklisted("com.google.android.apps.nexuslauncher"))
        assertTrue(PackageBlacklist.isBlacklisted("com.android.launcher3"))
        assertTrue(PackageBlacklist.isBlacklisted("com.android.settings"))
        assertTrue(PackageBlacklist.isBlacklisted("com.google.android.inputmethod.latin"))
        assertTrue(PackageBlacklist.isBlacklisted("com.google.android.gms"))
        assertTrue(PackageBlacklist.isBlacklisted("com.miui.securitycenter"))
        assertTrue(PackageBlacklist.isBlacklisted("com.scrollmeter.app"))
    }

    @Test
    fun `isBlacklisted should return false for user third-party applications`() {
        assertFalse(PackageBlacklist.isBlacklisted("com.instagram.android"))
        assertFalse(PackageBlacklist.isBlacklisted("com.twitter.android"))
        assertFalse(PackageBlacklist.isBlacklisted("org.telegram.messenger"))
        assertFalse(PackageBlacklist.isBlacklisted("com.reddit.frontpage"))
    }
}
