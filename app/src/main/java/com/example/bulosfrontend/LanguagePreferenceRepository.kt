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
    private val themeKey = stringPreferencesKey("selected_app_theme")
    private val preservationIntroCompletedKey = booleanPreferencesKey("has_completed_preservation_intro")

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

    val selectedTheme: Flow<AppTheme> = context.languageDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences: Preferences -> AppTheme.fromStoredValue(preferences[themeKey]) }

    suspend fun saveLanguage(language: UiLanguage) {
        context.languageDataStore.edit { preferences ->
            preferences[languageKey] = language.name
        }
    }

    suspend fun saveTheme(theme: AppTheme) {
        context.languageDataStore.edit { preferences ->
            preferences[themeKey] = theme.name
        }
    }

    suspend fun markPreservationIntroCompleted() {
        context.languageDataStore.edit { preferences ->
            preferences[preservationIntroCompletedKey] = true
        }
    }
}
