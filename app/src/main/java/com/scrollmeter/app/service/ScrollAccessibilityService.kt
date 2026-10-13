package com.scrollmeter.app.service

import android.accessibilityservice.AccessibilityService
import android.content.pm.ServiceInfo
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.ServiceCompat
import com.scrollmeter.app.core.dispatchers.MainDispatcher
import com.scrollmeter.app.service.batch.ScrollBatchCollector
import com.scrollmeter.app.service.filter.PackageBlacklist
import com.scrollmeter.app.service.model.RawScrollEvent
import com.scrollmeter.app.service.notification.ScrollNotificationManager
import com.scrollmeter.app.service.receiver.ScreenStateReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Core Accessibility Service responsible for capturing user scroll gestures
 * across all active Android applications with zero noticeable battery or UI thread impact.
 */
@AndroidEntryPoint
class ScrollAccessibilityService : AccessibilityService() {

    @Inject lateinit var notificationManager: ScrollNotificationManager
    @Inject lateinit var batchCollector: ScrollBatchCollector
    @MainDispatcher @Inject lateinit var mainDispatcher: CoroutineDispatcher

    private val supervisorJob = SupervisorJob()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Never-Crash: Unhandled coroutine exception in ScrollAccessibilityService")
    }
    var serviceScope: CoroutineScope = CoroutineScope(SupervisorJob() + exceptionHandler)
        internal set

    private val _eventFlow = MutableSharedFlow<RawScrollEvent>(
        extraBufferCapacity = 1000,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val eventFlow: SharedFlow<RawScrollEvent> = _eventFlow.asSharedFlow()

    private var screenReceiver: ScreenStateReceiver? = null

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(mainDispatcher + supervisorJob + exceptionHandler)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        try {
            Timber.d("ScrollAccessibilityService connected.")

            // 1. Initialize notification channel & start foreground service
            notificationManager.createNotificationChannel()
            val notification = notificationManager.buildForegroundNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceCompat.startForeground(
                    this,
                    ScrollNotificationManager.NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                ServiceCompat.startForeground(
                    this,
                    ScrollNotificationManager.NOTIFICATION_ID,
                    notification,
                    0
                )
            }

            // 2. Start batch collection loop
            batchCollector.startCollecting(serviceScope, _eventFlow)

            // 3. Register Screen ON/OFF receiver for 0% battery consumption when screen is locked
            registerScreenStateReceiver()

        } catch (t: Throwable) {
            Timber.e(t, "Never-Crash: Error during onServiceConnected")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            if (event == null || event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED) {
                return
            }

            val packageName = event.packageName?.toString()
            if (PackageBlacklist.isBlacklisted(packageName)) {
                return
            }

            val deltaX = event.scrollDeltaX.toFloat()
            val deltaY = event.scrollDeltaY.toFloat()
            val timestamp = if (event.eventTime > 0) event.eventTime else System.currentTimeMillis()

            // Ultra fast-path buffer emit - non-blocking for UI thread
            _eventFlow.tryEmit(
                RawScrollEvent(
                    packageName = packageName!!,
                    deltaX = deltaX,
                    deltaY = deltaY,
                    timestamp = timestamp
                )
            )
        } catch (t: Throwable) {
            Timber.e(t, "Never-Crash: Error processing accessibility event")
        }
    }

    override fun onInterrupt() {
        try {
            Timber.w("ScrollAccessibilityService interrupted.")
            serviceScope.launch {
                batchCollector.flush()
            }
        } catch (t: Throwable) {
            Timber.e(t, "Never-Crash: Error during onInterrupt")
        }
    }

    override fun onDestroy() {
        try {
            Timber.d("ScrollAccessibilityService destroying.")
            unregisterScreenStateReceiver()
            kotlinx.coroutines.runBlocking(mainDispatcher) {
                kotlinx.coroutines.withTimeoutOrNull(1000) {
                    batchCollector.pause()
                }
            }
            supervisorJob.cancel()
        } catch (t: Throwable) {
            Timber.e(t, "Never-Crash: Error during onDestroy")
        } finally {
            super.onDestroy()
        }
    }

    private fun registerScreenStateReceiver() {
        if (screenReceiver != null) return
        screenReceiver = ScreenStateReceiver(
            onScreenOff = {
                serviceScope.launch {
                    batchCollector.pause()
                }
            },
            onScreenOn = {
                batchCollector.resume(serviceScope, _eventFlow)
            }
        )
        registerReceiver(screenReceiver, ScreenStateReceiver.createIntentFilter())
    }

    private fun unregisterScreenStateReceiver() {
        screenReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                Timber.e(e, "Error unregistering screen receiver")
            }
            screenReceiver = null
        }
    }
}
