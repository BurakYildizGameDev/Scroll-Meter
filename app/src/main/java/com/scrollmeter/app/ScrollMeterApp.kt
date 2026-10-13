package com.scrollmeter.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class ScrollMeterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        com.scrollmeter.app.core.logging.TimberInitializer.init()
    }
}
