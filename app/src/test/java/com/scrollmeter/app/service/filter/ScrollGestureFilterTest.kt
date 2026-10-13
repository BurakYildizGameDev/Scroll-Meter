package com.scrollmeter.app.service.filter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ScrollGestureFilterTest {

    @Test
    fun `processScrollGesture should reject null or blacklisted package`() {
        assertNull(ScrollGestureFilter.processScrollGesture(null, 10f, 10f))
        assertNull(ScrollGestureFilter.processScrollGesture("", 10f, 10f))
        assertNull(ScrollGestureFilter.processScrollGesture("com.android.systemui", 50f, 50f))
        assertNull(ScrollGestureFilter.processScrollGesture("com.scrollmeter.app", 50f, 50f))
    }

    @Test
    fun `processScrollGesture should reject micro jitter below 3px`() {
        assertNull(ScrollGestureFilter.processScrollGesture("com.instagram.android", 1.0f, 1.0f))
        assertNull(ScrollGestureFilter.processScrollGesture("com.instagram.android", 0.0f, 2.0f))
    }

    @Test
    fun `processScrollGesture should reject extreme anomaly spikes above 5000px`() {
        assertNull(ScrollGestureFilter.processScrollGesture("com.google.android.apps.maps", 4000f, 4000f))
        assertNull(ScrollGestureFilter.processScrollGesture("com.instagram.android", 0f, 5500f))
    }

    @Test
    fun `processScrollGesture should reject NaN or Infinity`() {
        assertNull(ScrollGestureFilter.processScrollGesture("com.twitter.android", Float.NaN, 50f))
        assertNull(ScrollGestureFilter.processScrollGesture("com.twitter.android", 50f, Float.POSITIVE_INFINITY))
    }

    @Test
    fun `processScrollGesture should accept normal valid scroll`() {
        val distance = ScrollGestureFilter.processScrollGesture("com.instagram.android", 0f, 100f)
        assertNotNull(distance)
        assertEquals(100.0, distance!!, 0.001)

        val diagonalDistance = ScrollGestureFilter.processScrollGesture("com.instagram.android", 30f, 40f)
        assertNotNull(diagonalDistance)
        assertEquals(50.0, diagonalDistance!!, 0.001)
    }
}
