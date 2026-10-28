package com.example.bulosfrontend

import android.content.Context
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class MlKitEnglishFilipinoPivot(private val context: Context) {
    private val englishToFilipinoOptions = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.TAGALOG)
        .build()
    private val englishToFilipinoTranslator = Translation.getClient(englishToFilipinoOptions)

    private val filipinoToEnglishOptions = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.TAGALOG)
        .setTargetLanguage(TranslateLanguage.ENGLISH)
        .build()
    private val filipinoToEnglishTranslator = Translation.getClient(filipinoToEnglishOptions)

    suspend fun translateEnglishToFilipino(text: String): String? {
        return try {
            englishToFilipinoTranslator.downloadModelIfNeeded().await()
            val result = englishToFilipinoTranslator.translate(text).await()
            result.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun translateFilipinoToEnglish(text: String): String? {
        return try {
            filipinoToEnglishTranslator.downloadModelIfNeeded().await()
            val result = filipinoToEnglishTranslator.translate(text).await()
            result.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }
}
