package com.gentleai.colorrush.data.local.datastore

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.gentleai.colorrush.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Integration test for [PreferencesDataStoreRepository] using DataStore
 * backed by a test-only in-memory or isolated file.
 *
 * Verifies:
 * - Default values (language="eu", soundEnabled=true)
 * - Read/write cycles for both preferences
 * - Persistence across get/set operations
 */
@RunWith(RobolectricTestRunner::class)
class PreferencesDataStoreRepositoryTest {

    private lateinit var repository: PreferencesRepository
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        repository = PreferencesDataStoreRepository(context)
    }

    @Test
    fun `default language is eu`() = runTest {
        val lang = repository.language.first()
        assertEquals("eu", lang)
    }

    @Test
    fun `default sound enabled is true`() = runTest {
        val enabled = repository.soundEnabled.first()
        assertTrue(enabled)
    }

    @Test
    fun `setLanguage persists the change`() = runTest {
        repository.setLanguage("en")
        val lang = repository.language.first()
        assertEquals("en", lang)
    }

    @Test
    fun `setLanguage to es persists`() = runTest {
        repository.setLanguage("es")
        val lang = repository.language.first()
        assertEquals("es", lang)
    }

    @Test
    fun `setLanguage to eu persists`() = runTest {
        repository.setLanguage("eu")
        val lang = repository.language.first()
        assertEquals("eu", lang)
    }

    @Test
    fun `setSoundEnabled false persists`() = runTest {
        repository.setSoundEnabled(false)
        val enabled = repository.soundEnabled.first()
        assertFalse(enabled)
    }

    @Test
    fun `setSoundEnabled true persists`() = runTest {
        // First set to false, then back to true
        repository.setSoundEnabled(false)
        repository.setSoundEnabled(true)
        val enabled = repository.soundEnabled.first()
        assertTrue(enabled)
    }

    @Test
    fun `language changes independently from sound`() = runTest {
        repository.setLanguage("es")
        repository.setSoundEnabled(false)

        assertEquals("es", repository.language.first())
        assertFalse(repository.soundEnabled.first())
    }

    @Test
    fun `multiple language changes update correctly`() = runTest {
        repository.setLanguage("eu")
        assertEquals("eu", repository.language.first())

        repository.setLanguage("en")
        assertEquals("en", repository.language.first())

        repository.setLanguage("es")
        assertEquals("es", repository.language.first())
    }
}
