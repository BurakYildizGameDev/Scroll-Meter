package com.scrollmeter.app.data.resolver

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process
import com.scrollmeter.app.domain.repository.IUsageStatsProvider
import com.scrollmeter.app.service.filter.PackageBlacklist
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageStatsProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : IUsageStatsProvider {

    override fun isUsageAccessGranted(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun queryTodayForegroundMillis(): Map<String, Long> {
        if (!isUsageAccessGranted()) {
            return emptyMap()
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyMap()

        // Midnight today (00:00:00.000)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        if (startTime >= endTime) return emptyMap()

        val results = mutableMapOf<String, Long>()

        try {
            // 1. Long-term aggregate stats
            val aggregated = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
            if (!aggregated.isNullOrEmpty()) {
                for ((packageName, stats) in aggregated) {
                    val foregroundTime = stats.totalTimeInForeground
                    if (foregroundTime > 0L && !PackageBlacklist.isBlacklisted(packageName)) {
                        results[packageName] = foregroundTime
                    }
                }
            }

            // Fallback if aggregate returned nothing
            if (results.isEmpty()) {
                val list = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime
                )
                if (!list.isNullOrEmpty()) {
                    for (stats in list) {
                        val foregroundTime = stats.totalTimeInForeground
                        if (foregroundTime > 0L && !PackageBlacklist.isBlacklisted(stats.packageName)) {
                            val current = results[stats.packageName] ?: 0L
                            results[stats.packageName] = maxOf(current, foregroundTime)
                        }
                    }
                }
            }

            // 2. Real-time UsageEvents stream (sub-second responsiveness without waiting for Android system flushes)
            val events = usageStatsManager.queryEvents(startTime, endTime)
            val event = UsageEvents.Event()
            val lastResumedMap = mutableMapOf<String, Long>()
            val liveSessionDeltas = mutableMapOf<String, Long>()

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val pkg = event.packageName ?: continue
                if (PackageBlacklist.isBlacklisted(pkg)) continue

                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        lastResumedMap[pkg] = event.timeStamp
                    }
                    UsageEvents.Event.ACTIVITY_PAUSED -> {
                        val resumedAt = lastResumedMap.remove(pkg)
                        if (resumedAt != null && event.timeStamp > resumedAt) {
                            val delta = event.timeStamp - resumedAt
                            liveSessionDeltas[pkg] = (liveSessionDeltas[pkg] ?: 0L) + delta
                        }
                    }
                }
            }

            // Accumulate active sessions up to current time
            for ((pkg, resumedAt) in lastResumedMap) {
                if (endTime > resumedAt) {
                    val delta = endTime - resumedAt
                    liveSessionDeltas[pkg] = (liveSessionDeltas[pkg] ?: 0L) + delta
                }
            }

            // Merge event deltas to ensure recent app usage updates immediately
            for ((pkg, liveDelta) in liveSessionDeltas) {
                if (liveDelta > 0L && !PackageBlacklist.isBlacklisted(pkg)) {
                    val current = results[pkg] ?: 0L
                    results[pkg] = maxOf(current, liveDelta)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to query Android UsageStatsManager")
        }

        return results
    }
}
