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

    companion object {
        private val KNOWN_APP_NAMES = mapOf(
            "com.instagram.android" to "Instagram",
            "com.github.android" to "GitHub",
            "com.twitter.android" to "X (Twitter)",
            "com.zhiliaoapp.musically" to "TikTok",
            "com.ss.android.ugc.trill" to "TikTok",
            "com.whatsapp" to "WhatsApp",
            "com.whatsapp.w4b" to "WhatsApp Business",
            "com.android.chrome" to "Google Chrome",
            "com.google.android.youtube" to "YouTube",
            "com.reddit.frontpage" to "Reddit",
            "com.spotify.music" to "Spotify",
            "com.hyilmaz.okey.yuzbir" to "101 Okey Plus",
            "com.facebook.katana" to "Facebook",
            "org.telegram.messenger" to "Telegram",
            "com.netflix.mediaclient" to "Netflix",
            "com.google.android.apps.photos" to "Google Fotoğraflar",
            "com.google.android.gm" to "Gmail"
        )
    }

    fun resolveDisplayName(packageName: String): String {
        if (packageName.isBlank()) return "Bilinmeyen Uygulama"
        if (packageName == "android") return "Android Sistemi"

        // Check memory cache first to avoid slow IPC Binder call
        memoryCache.get(packageName)?.let { return it }

        val resolvedName = try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            KNOWN_APP_NAMES[packageName] ?: formatFallbackPackageName(packageName)
        } catch (_: Exception) {
            KNOWN_APP_NAMES[packageName] ?: formatFallbackPackageName(packageName)
        }

        memoryCache.put(packageName, resolvedName)
        return resolvedName
    }

    private fun formatFallbackPackageName(packageName: String): String {
        val clean = if (packageName.endsWith(".android", ignoreCase = true)) {
            packageName.substringBeforeLast(".android")
        } else {
            packageName
        }
        val rawSegment = clean.substringAfterLast('.')
        return rawSegment.replaceFirstChar { it.uppercase() }
    }
}
