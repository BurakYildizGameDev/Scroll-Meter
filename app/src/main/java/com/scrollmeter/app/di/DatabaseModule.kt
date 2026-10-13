package com.scrollmeter.app.di

import android.content.Context
import androidx.room.Room
import com.scrollmeter.app.data.local.dao.ScrollDao
import com.scrollmeter.app.data.local.database.ScrollMeterDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ScrollMeterDatabase {
        return Room.databaseBuilder(
            context,
            ScrollMeterDatabase::class.java,
            ScrollMeterDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideScrollDao(database: ScrollMeterDatabase): ScrollDao {
        return database.scrollDao()
    }
}
