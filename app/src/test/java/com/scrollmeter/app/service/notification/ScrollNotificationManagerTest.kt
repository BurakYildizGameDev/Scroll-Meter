package com.scrollmeter.app.service.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ScrollNotificationManagerTest {

    private lateinit var context: Context
    private lateinit var notificationManager: ScrollNotificationManager
    private lateinit var systemNotificationManager: NotificationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        systemNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager = ScrollNotificationManager(context)
    }

    @Test
    fun `createNotificationChannel creates low importance silent channel`() {
        notificationManager.createNotificationChannel()

        val channel = systemNotificationManager.getNotificationChannel(
            ScrollNotificationManager.CHANNEL_ID
        )
        assertNotNull(channel)
        assertEquals(NotificationManager.IMPORTANCE_LOW, channel.importance)
        assertEquals(false, channel.canShowBadge())
    }

    @Test
    @Suppress("DEPRECATION")
    fun `buildForegroundNotification sets ongoing and low priority flags`() {
        val customText = "Bugünkü mesafe: 120 metre"
        val notification = notificationManager.buildForegroundNotification(customText)

        assertNotNull(notification)
        // Check ONGOING flag
        val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
        assertTrue(isOngoing)
        assertEquals(NotificationCompat.PRIORITY_LOW, notification.priority)
    }
}
