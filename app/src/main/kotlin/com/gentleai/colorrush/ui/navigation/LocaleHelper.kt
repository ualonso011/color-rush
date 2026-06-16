package com.gentleai.colorrush.ui.navigation

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Utility for applying the selected language at runtime using
 * [AppCompatDelegate.setApplicationLocales].
 *
 * This is called from [MainViewModel] when the user switches language.
 * The system recreates the activity with the new locale immediately.
 */
object LocaleHelper {

    /**
     * Applies the given [languageCode] (ISO 639-1, e.g. "eu", "es", "en")
     * to the entire application via AppCompat.
     */
    fun applyLanguage(languageCode: String) {
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}
