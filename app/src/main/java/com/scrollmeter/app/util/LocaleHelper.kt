package com.scrollmeter.app.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

data class LanguageOption(
    val code: String, // "" represents system default
    val displayName: String,
    val flagEmoji: String
)

object LocaleHelper {
    val SUPPORTED_LANGUAGES = listOf(
        LanguageOption(code = "", displayName = "Sistem Varsayılanı / System Default", flagEmoji = "🌐"),
        LanguageOption(code = "tr", displayName = "Türkçe", flagEmoji = "🇹🇷"),
        LanguageOption(code = "en", displayName = "English", flagEmoji = "🇬🇧"),
        LanguageOption(code = "de", displayName = "Deutsch", flagEmoji = "🇩🇪"),
        LanguageOption(code = "ru", displayName = "Русский", flagEmoji = "🇷🇺"),
        LanguageOption(code = "zh", displayName = "简体中文", flagEmoji = "🇨🇳"),
        LanguageOption(code = "fr", displayName = "Français", flagEmoji = "🇫🇷"),
        LanguageOption(code = "it", displayName = "Italiano", flagEmoji = "🇮🇹")
    )

    fun applyLocale(languageCode: String) {
        val localeList = if (languageCode.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageCode)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun getCurrentLanguageCode(): String {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        return if (currentLocales.isEmpty) "" else currentLocales.toLanguageTags().substringBefore('-').substringBefore(',')
    }
}
