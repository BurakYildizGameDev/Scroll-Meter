package com.scrollmeter.app.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.scrollmeter.app.data.local.database.ScrollMeterDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ScrollDaoTest {

    private lateinit var database: ScrollMeterDatabase
    private lateinit var dao: ScrollDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ScrollMeterDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.scrollDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `upsertScroll inserts new record and cumulative updates existing record`() = runTest {
        val epochDay = 20000L
        val packageName = "com.instagram.android"

        // First insert
        dao.upsertScroll(
            dateEpochDays = epochDay,
            packageName = packageName,
            pixelsToAdd = 500.0,
            mmToAdd = 50.0,
            timestamp = 1000L
        )

        dao.getDailyTotalPixels(epochDay).test {
            assertEquals(500.0, awaitItem() ?: 0.0, 0.001)

            // Second insert for the exact same package and date
            dao.upsertScroll(
                dateEpochDays = epochDay,
                packageName = packageName,
                pixelsToAdd = 300.0,
                mmToAdd = 30.0,
                timestamp = 2000L
            )

            // Should cumulatively sum to 800.0
            assertEquals(800.0, awaitItem() ?: 0.0, 0.001)
        }
    }

    @Test
    fun `getDailySessions isolates different dates and orders by totalPixels descending`() = runTest {
        val today = 20000L
        val yesterday = 19999L

        dao.upsertScroll(today, "com.twitter", 200.0, 20.0, 1000L)
        dao.upsertScroll(today, "com.tiktok", 1200.0, 120.0, 1000L)
        dao.upsertScroll(yesterday, "com.yesterday.app", 5000.0, 500.0, 1000L)

        dao.getDailySessions(today).test {
            val sessions = awaitItem()
            assertEquals(2, sessions.size)
            assertEquals("com.tiktok", sessions[0].packageName)
            assertEquals(1200.0, sessions[0].totalPixels, 0.001)
            assertEquals("com.twitter", sessions[1].packageName)
            assertEquals(200.0, sessions[1].totalPixels, 0.001)
        }
    }

    @Test
    fun `deleteRecordsOlderThan deletes only records before cutoff epoch`() = runTest {
        val oldDay = 100L
        val recentDay = 200L

        dao.upsertScroll(oldDay, "com.old.app", 500.0, 50.0, 1000L)
        dao.upsertScroll(recentDay, "com.new.app", 500.0, 50.0, 1000L)

        val deletedCount = dao.deleteRecordsOlderThan(150L)
        assertEquals(1, deletedCount)

        dao.getDailySessions(oldDay).test {
            assertEquals(0, awaitItem().size)
        }

        dao.getDailySessions(recentDay).test {
            assertEquals(1, awaitItem().size)
        }
    }
}
