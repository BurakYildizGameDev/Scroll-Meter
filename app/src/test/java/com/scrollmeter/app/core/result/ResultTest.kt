package com.scrollmeter.app.core.result

import app.cash.turbine.test
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ResultTest {

    @Test
    fun `map transforms Success value accurately`() {
        val result: Result<Int> = Result.Success(42)
        val transformed = result.map { it * 2 }

        assertInstanceOf(Result.Success::class.java, transformed)
        assertEquals(84, (transformed as Result.Success).data)
    }

    @Test
    fun `map preserves Error throwable`() {
        val exception = IllegalStateException("Test error")
        val result: Result<Int> = Result.Error(exception)
        val transformed = result.map { it * 2 }

        assertInstanceOf(Result.Error::class.java, transformed)
        assertEquals(exception, (transformed as Result.Error).throwable)
    }

    @Test
    fun `map preserves Loading state`() {
        val result: Result<Int> = Result.Loading
        val transformed = result.map { it * 2 }

        assertInstanceOf(Result.Loading::class.java, transformed)
    }

    @Test
    fun `onSuccess triggers only for Success state`() {
        var triggered = false
        val result: Result<String> = Result.Success("Antigravity")
        result.onSuccess { triggered = true }
        assertTrue(triggered)

        var errorTriggered = false
        val errorResult: Result<String> = Result.Error(RuntimeException())
        errorResult.onSuccess { errorTriggered = true }
        assertTrue(!errorTriggered)
    }

    @Test
    fun `asResult emits Loading then Success`() = runTest {
        val flow = flowOf("Live Data")
        flow.asResult().test {
            assertInstanceOf(Result.Loading::class.java, awaitItem())
            val item = awaitItem()
            assertInstanceOf(Result.Success::class.java, item)
            assertEquals("Live Data", (item as Result.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun `asResult emits Loading then Error on failure`() = runTest {
        val flow = flow<String> {
            throw RuntimeException("Crash simulation")
        }
        flow.asResult().test {
            assertInstanceOf(Result.Loading::class.java, awaitItem())
            val item = awaitItem()
            assertInstanceOf(Result.Error::class.java, item)
            assertEquals("Crash simulation", (item as Result.Error).throwable.message)
            awaitComplete()
        }
    }
}
