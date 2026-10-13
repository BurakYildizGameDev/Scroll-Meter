package com.scrollmeter.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "daily_app_scroll",
    primaryKeys = ["dateEpochDays", "packageName"],
    indices = [
        Index(value = ["dateEpochDays"]),
        Index(value = ["packageName"])
    ]
)
data class DailyAppScrollEntity(
    @ColumnInfo(name = "dateEpochDays")
    val dateEpochDays: Long,

    @ColumnInfo(name = "packageName")
    val packageName: String,

    @ColumnInfo(name = "totalPixels", defaultValue = "0.0")
    val totalPixels: Double,

    @ColumnInfo(name = "totalMilliMeters", defaultValue = "0.0")
    val totalMilliMeters: Double,

    @ColumnInfo(name = "lastUpdatedTimestamp")
    val lastUpdatedTimestamp: Long
)
