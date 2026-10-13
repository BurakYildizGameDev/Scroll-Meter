package com.scrollmeter.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.scrollmeter.app.data.local.dao.ScrollDao
import com.scrollmeter.app.data.local.entity.DailyAppScrollEntity

@Database(
    entities = [DailyAppScrollEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ScrollMeterDatabase : RoomDatabase() {
    abstract fun scrollDao(): ScrollDao

    companion object {
        const val DATABASE_NAME = "scroll_meter.db"
    }
}
