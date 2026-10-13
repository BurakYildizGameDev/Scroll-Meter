package com.scrollmeter.app.presentation.theme

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ThemeTest {

    @Test
    fun `typography defines distinct large distance styles`() {
        assertNotNull(Typography.displayLarge)
        assertNotNull(Typography.displayMedium)
        assertNotNull(Typography.headlineLarge)
        assertEquals(54f, Typography.displayLarge.fontSize.value)
        assertEquals(42f, Typography.displayMedium.fontSize.value)
    }

    @Test
    fun `brand colors are accurately defined`() {
        assertNotNull(EmeraldPrimaryLight)
        assertNotNull(EmeraldPrimaryDark)
        assertNotNull(TurquoiseSecondaryDark)
        assertNotNull(BackgroundDark)
        assertNotNull(ElectricMint)
        assertNotNull(CyberCyan)
        assertNotNull(ObsidianVoid)
    }
}
