package com.scrollmeter.app.core.logging

import com.scrollmeter.app.BuildConfig
import timber.log.Timber

object TimberInitializer {
    fun init() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
