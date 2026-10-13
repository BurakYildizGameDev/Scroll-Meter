package com.scrollmeter.app.domain.usecase

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CalculateMetaphorUseCaseTest {

    private val useCase = CalculateMetaphorUseCase()

    @Test
    fun `0 meters starts at ant with giraffe as next goal`() {
        val progress = useCase(0.0)
        assertEquals("ant", progress.currentMilestone.id)
        assertNotNull(progress.nextMilestone)
        assertEquals("giraffe", progress.nextMilestone?.id)
        assertEquals(5.0, progress.remainingMetersToNext, 0.001)
        assertEquals(0.0f, progress.progressFractionToNext, 0.001f)
    }

    @Test
    fun `10 meters achieves giraffe with bus as next goal`() {
        val progress = useCase(10.0)
        assertEquals("giraffe", progress.currentMilestone.id)
        assertEquals("bus", progress.nextMilestone?.id)
        assertEquals(5.0, progress.remainingMetersToNext, 0.001)
        // From giraffe (5m) to bus (15m), 10m is 50%
        assertEquals(0.5f, progress.progressFractionToNext, 0.001f)
    }

    @Test
    fun `excess meters past Everest conquers final milestone`() {
        val progress = useCase(10000.0)
        assertEquals("everest", progress.currentMilestone.id)
        assertNull(progress.nextMilestone)
        assertEquals(0.0, progress.remainingMetersToNext, 0.001)
        assertEquals(1.0f, progress.progressFractionToNext, 0.001f)
    }
}
