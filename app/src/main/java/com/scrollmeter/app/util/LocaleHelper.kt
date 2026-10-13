package com.scrollmeter.app.util

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

data class LanguageOption(
    val code: String, // "" represents system default
    val displayName: String,
    val flagEmoji: String
)

object LocaleHelper {
    val SUPPORTED_LANGUAGES = listOf(
        LanguageOption(code = "", displayName = "Sistem Varsayılanı (System)", flagEmoji = "🌐"),
        LanguageOption(code = "tr", displayName = "Türkçe", flagEmoji = "🇹🇷"),
        LanguageOption(code = "en", displayName = "English", flagEmoji = "🇬🇧"),
        LanguageOption(code = "de", displayName = "Deutsch", flagEmoji = "🇩🇪"),
        LanguageOption(code = "ru", displayName = "Русский", flagEmoji = "🇷🇺"),
        LanguageOption(code = "zh", displayName = "简体中文", flagEmoji = "🇨🇳"),
        LanguageOption(code = "fr", displayName = "Français", flagEmoji = "🇫🇷"),
        LanguageOption(code = "it", displayName = "Italiano", flagEmoji = "🇮🇹")
    )

    fun getLocale(languageCode: String): Locale {
        return if (languageCode.isBlank()) {
            Locale.getDefault()
        } else {
            val parts = languageCode.split("-", "_")
            if (parts.size > 1) {
                Locale(parts[0], parts[1])
            } else {
                Locale(languageCode)
            }
        }
    }

    fun getLocalizedContext(baseContext: Context, languageCode: String): Context {
        val locale = getLocale(languageCode)
        val config = Configuration(baseContext.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return baseContext.createConfigurationContext(config)
    }

    fun getLocalizedConfiguration(baseContext: Context, languageCode: String): Configuration {
        val locale = getLocale(languageCode)
        val config = Configuration(baseContext.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return config
    }

    fun applyLocale(context: Context, languageCode: String) {
        val localeList = if (languageCode.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageCode)
        }
        AppCompatDelegate.setApplicationLocales(localeList)

        val locale = getLocale(languageCode)
        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        res.updateConfiguration(config, res.displayMetrics)
    }

    fun getCurrentLanguageCode(): String {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        return if (currentLocales.isEmpty) "" else currentLocales.toLanguageTags().substringBefore('-').substringBefore(',')
    }
}

