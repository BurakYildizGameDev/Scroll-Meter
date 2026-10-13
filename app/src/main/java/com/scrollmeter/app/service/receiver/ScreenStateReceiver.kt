package com.scrollmeter.app.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import timber.log.Timber

/**
 * BroadcastReceiver listening for SCREEN_ON and SCREEN_OFF system events
 * to halt background collection and conserve 100% battery when display is inactive.
 */
class ScreenStateReceiver(
    private val onScreenOff: () -> Unit,
    private val onScreenOn: () -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> {
                Timber.d("Screen OFF detected. Halting scroll collection.")
                onScreenOff()
            }
            Intent.ACTION_SCREEN_ON -> {
                Timber.d("Screen ON detected. Resuming scroll collection.")
                onScreenOn()
            }
        }
    }

    companion object {
        fun createIntentFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            }
        }
    }
}
