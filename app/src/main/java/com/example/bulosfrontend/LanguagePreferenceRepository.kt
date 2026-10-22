package com.example.bulosfrontend

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val LANGUAGE_PREFERENCES_NAME = "ui_language_preferences"
private val Context.languageDataStore by preferencesDataStore(name = LANGUAGE_PREFERENCES_NAME)

class LanguagePreferenceRepository(private val context: Context) {
    private val languageKey = stringPreferencesKey("selected_ui_language")
    private val fontSizeKey = stringPreferencesKey("selected_font_size")
    private val offlineModeKey = booleanPreferencesKey("offline_mode_enabled")
    private val skipTranscriptionScreenKey = booleanPreferencesKey("skip_transcription_screen")
    private val autoSaveEnabledKey = booleanPreferencesKey("auto_save_enabled")
    private val preservationIntroCompletedKey = booleanPreferencesKey("has_completed_preservation_intro")
    private val onboardingCompletedKey = booleanPreferencesKey("has_completed_onboarding")
    private val sttWalkthroughCompletedKey = booleanPreferencesKey("has_completed_stt_walkthrough")

    val selectedLanguage: Flow<UiLanguage?> = context.languageDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences: Preferences -> UiLanguage.fromStoredValue(preferences[languageKey]) }

    val hasCompletedPreservationIntro: Flow<Boolean?> = context.languageDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences: Preferences -> preferences[preservationIntroCompletedKey] ?: false }

    /** Existing completed introductions remain completed after the guide replaces the old screen. */
    val hasCompletedOnboarding: Flow<Boolean> = context.languageDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences: Preferences ->
            preferences[onboardingCompletedKey]
                ?: preferences[preservationIntroCompletedKey]
                ?: false
        }

    val hasCompletedSttWalkthrough: Flow<Boolean> = context.languageDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences: Preferences -> preferences[sttWalkthroughCompletedKey] ?: false }

    val selectedFontSize: Flow<AppFontSize> = context.languageDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences: Preferences -> AppFontSize.fromStoredValue(preferences[fontSizeKey]) }

    val offlineModeEnabled: Flow<Boolean> = context.languageDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences: Preferences -> preferences[offlineModeKey] ?: false }

    val skipTranscriptionScreen: Flow<Boolean> = context.languageDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences: Preferences -> preferences[skipTranscriptionScreenKey] ?: true }

    val autoSaveEnabled: Flow<Boolean> = context.languageDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences: Preferences -> preferences[autoSaveEnabledKey] ?: true }

    suspend fun saveLanguage(language: UiLanguage) {
        context.languageDataStore.edit { preferences ->
            preferences[languageKey] = language.name
        }
    }

    suspend fun saveFontSize(fontSize: AppFontSize) {
        context.languageDataStore.edit { preferences ->
            preferences[fontSizeKey] = fontSize.name
        }
    }

    suspend fun saveOfflineMode(enabled: Boolean) {
        context.languageDataStore.edit { preferences ->
            preferences[offlineModeKey] = enabled
        }
    }

    suspend fun saveSkipTranscriptionScreen(skip: Boolean) {
        context.languageDataStore.edit { preferences ->
            preferences[skipTranscriptionScreenKey] = skip
        }
    }

    suspend fun saveAutoSaveEnabled(enabled: Boolean) {
        context.languageDataStore.edit { preferences ->
            preferences[autoSaveEnabledKey] = enabled
        }
    }

    suspend fun markPreservationIntroCompleted() {
        context.languageDataStore.edit { preferences ->
            preferences[preservationIntroCompletedKey] = true
        }
    }

    suspend fun markOnboardingCompleted() {
        context.languageDataStore.edit { preferences ->
            preferences[onboardingCompletedKey] = true
            preferences[preservationIntroCompletedKey] = true
        }
    }

    suspend fun markSttWalkthroughCompleted() {
        context.languageDataStore.edit { preferences ->
            preferences[sttWalkthroughCompletedKey] = true
        }
    }
}
