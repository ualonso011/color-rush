package com.gentleai.colorrush

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ColorRushApp : Application() {

    @Inject
    lateinit var preferencesRepository: com.gentleai.colorrush.data.local.datastore.PreferencesDataStoreRepository

    override fun onCreate() {
        super.onCreate()
        // Apply persisted locale on startup so Euskera is active before any Activity is created
        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            val lang = preferencesRepository.language.first()
            val localeList = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }
}
