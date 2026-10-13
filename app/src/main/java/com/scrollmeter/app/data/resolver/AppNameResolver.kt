package com.scrollmeter.app.data.resolver

import android.content.Context
import android.content.pm.PackageManager
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNameResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val memoryCache = LruCache<String, String>(250)

    fun resolveDisplayName(packageName: String): String {
        if (packageName.isBlank()) return "Bilinmeyen Uygulama"

        // Check memory cache first to avoid slow IPC Binder call
        memoryCache.get(packageName)?.let { return it }

        val resolvedName = try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            // Friendly formatting for deleted/uninstalled apps
            packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
        } catch (_: Exception) {
            packageName
        }

        memoryCache.put(packageName, resolvedName)
        return resolvedName
    }
}
