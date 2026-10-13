package com.scrollmeter.app.service.receiver

import android.content.Context
import android.content.Intent
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ScreenStateReceiverTest {

    @Test
    fun `onReceive triggers onScreenOff when ACTION_SCREEN_OFF is received`() {
        var screenOffCalled = 0
        var screenOnCalled = 0

        val receiver = ScreenStateReceiver(
            onScreenOff = { screenOffCalled++ },
            onScreenOn = { screenOnCalled++ }
        )

        val context = mockk<Context>(relaxed = true)
        val intent = Intent(Intent.ACTION_SCREEN_OFF)

        receiver.onReceive(context, intent)

        assertEquals(1, screenOffCalled)
        assertEquals(0, screenOnCalled)
    }

    @Test
    fun `onReceive triggers onScreenOn when ACTION_SCREEN_ON is received`() {
        var screenOffCalled = 0
        var screenOnCalled = 0

        val receiver = ScreenStateReceiver(
            onScreenOff = { screenOffCalled++ },
            onScreenOn = { screenOnCalled++ }
        )

        val context = mockk<Context>(relaxed = true)
        val intent = Intent(Intent.ACTION_SCREEN_ON)

        receiver.onReceive(context, intent)

        assertEquals(0, screenOffCalled)
        assertEquals(1, screenOnCalled)
    }

    @Test
    fun `onReceive ignores unrelated actions`() {
        var screenOffCalled = 0
        var screenOnCalled = 0

        val receiver = ScreenStateReceiver(
            onScreenOff = { screenOffCalled++ },
            onScreenOn = { screenOnCalled++ }
        )

        val context = mockk<Context>(relaxed = true)
        val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED)

        receiver.onReceive(context, intent)

        assertEquals(0, screenOffCalled)
        assertEquals(0, screenOnCalled)
    }

    @Test
    fun `createIntentFilter includes both SCREEN_ON and SCREEN_OFF`() {
        val filter = ScreenStateReceiver.createIntentFilter()
        assertTrue(filter.hasAction(Intent.ACTION_SCREEN_OFF))
        assertTrue(filter.hasAction(Intent.ACTION_SCREEN_ON))
    }
}
