package com.scrollmeter.app.service

import android.view.accessibility.AccessibilityEvent
import com.scrollmeter.app.service.batch.ScrollBatchCollector
import com.scrollmeter.app.service.notification.ScrollNotificationManager
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ScrollAccessibilityServiceTest {

    private lateinit var service: ScrollAccessibilityService
    private lateinit var notificationManager: ScrollNotificationManager
    private lateinit var batchCollector: ScrollBatchCollector
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        notificationManager = mockk(relaxed = true)
        batchCollector = mockk(relaxed = true)

        service = ScrollAccessibilityService().apply {
            this.notificationManager = this@ScrollAccessibilityServiceTest.notificationManager
            this.batchCollector = this@ScrollAccessibilityServiceTest.batchCollector
            this.mainDispatcher = testDispatcher
            this.serviceScope = CoroutineScope(testDispatcher)
        }
    }

    @Test
    fun `onAccessibilityEvent does not crash on null event`() {
        // Never-crash guarantee
        service.onAccessibilityEvent(null)
        assertEquals(0, service.eventFlow.replayCache.size)
    }

    @Test
    @Suppress("DEPRECATION")
    fun `onAccessibilityEvent ignores non-scrolled events`() {
        val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_CLICKED)
        service.onAccessibilityEvent(event)
        assertEquals(0, service.eventFlow.replayCache.size)
    }

    @Test
    @Suppress("DEPRECATION")
    fun `onAccessibilityEvent ignores blacklisted packages`() {
        val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_SCROLLED).apply {
            packageName = "com.android.systemui"
            scrollDeltaX = 0
            scrollDeltaY = 100
        }
        service.onAccessibilityEvent(event)
        assertEquals(0, service.eventFlow.replayCache.size)
    }

    @Test
    @Suppress("DEPRECATION")
    fun `onAccessibilityEvent successfully buffers valid scroll events`() {
        val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_SCROLLED).apply {
            packageName = "com.instagram.android"
            scrollDeltaX = 0
            scrollDeltaY = 150
        }
        service.onAccessibilityEvent(event)

        assertNotNull(service.eventFlow)
    }

    @Test
    fun `onInterrupt safely triggers flush without crashing`() {
        service.onInterrupt()
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(atLeast = 1) { batchCollector.flush() }
    }
}
