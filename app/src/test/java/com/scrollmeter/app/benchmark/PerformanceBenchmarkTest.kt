package com.scrollmeter.app.benchmark

import com.scrollmeter.app.domain.calculator.DistanceCalculator
import com.scrollmeter.app.domain.filter.ScrollAnomalyFilter
import com.scrollmeter.app.domain.repository.IScrollRepository
import com.scrollmeter.app.service.batch.ScrollBatchCollector
import com.scrollmeter.app.service.model.RawScrollEvent
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

class PerformanceBenchmarkTest {

    @Test
    fun `benchmark DistanceCalculator throughput for 100,000 operations`() {
        val iterations = 100_000

        // JIT warm-up
        for (i in 0 until 10_000) {
            DistanceCalculator.calculateHypotenusePixels(0f, 150f)
            DistanceCalculator.pixelsToMillimeters(150.0, 440.0f, 1.0f)
        }

        val elapsedMs = measureTimeMillis {
            for (i in 0 until iterations) {
                DistanceCalculator.calculateHypotenusePixels(0f, 180f)
                DistanceCalculator.pixelsToMillimeters(
                    pixels = 180.0,
                    devicePpi = 440.0f,
                    calibrationMultiplier = 1.02f
                )
            }
        }

        val opsPerSec = (iterations.toDouble() / (elapsedMs.coerceAtLeast(1) / 1000.0)).toLong()
        println("=========================================================")
        println("⚡ BENCHMARK: DistanceCalculator Throughput")
        println("Operations: $iterations")
        println("Duration:   ${elapsedMs}ms")
        println("Throughput: $opsPerSec ops/second")
        println("=========================================================")

        assertTrue(elapsedMs < 200, "DistanceCalculator calculation took ${elapsedMs}ms (expected < 200ms)")
    }

    @Test
    fun `benchmark ScrollAnomalyFilter throughput for 100,000 gesture validations`() {
        val iterations = 100_000

        // JIT warm-up
        for (i in 0 until 10_000) {
            ScrollAnomalyFilter.isValidScroll(0f, 150f)
        }

        val elapsedMs = measureTimeMillis {
            for (i in 0 until iterations) {
                ScrollAnomalyFilter.isValidScroll(0f, 85f)
            }
        }

        val opsPerSec = (iterations.toDouble() / (elapsedMs.coerceAtLeast(1) / 1000.0)).toLong()
        println("=========================================================")
        println("⚡ BENCHMARK: ScrollAnomalyFilter Throughput")
        println("Operations: $iterations")
        println("Duration:   ${elapsedMs}ms")
        println("Throughput: $opsPerSec ops/second")
        println("=========================================================")

        assertTrue(elapsedMs < 100, "ScrollAnomalyFilter took ${elapsedMs}ms (expected < 100ms)")
    }

    @Test
    fun `benchmark ScrollBatchCollector 10,000-event memory buffering and debouncing`() = runTest {
        val repository = mockk<IScrollRepository>(relaxed = true)
        val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)
        val collector = ScrollBatchCollector(
            scrollRepository = repository,
            ioDispatcher = testDispatcher
        ).apply {
            epochDaySupplier = { 20000L }
        }

        val iterations = 10_000
        val elapsedMs = measureTimeMillis {
            for (i in 0 until iterations) {
                collector.addEvent(
                    RawScrollEvent(
                        packageName = "com.instagram.android",
                        deltaX = 0f,
                        deltaY = 120f
                    )
                )
            }
            collector.flush()
        }

        val opsPerSec = (iterations.toDouble() / (elapsedMs.coerceAtLeast(1) / 1000.0)).toLong()
        println("=========================================================")
        println("⚡ BENCHMARK: ScrollBatchCollector In-Memory Buffer")
        println("Events Buffered: $iterations")
        println("Duration:        ${elapsedMs}ms")
        println("Throughput:      $opsPerSec events/second")
        println("=========================================================")

        assertTrue(elapsedMs < 300, "ScrollBatchCollector buffering took ${elapsedMs}ms (expected < 300ms)")
    }
}
