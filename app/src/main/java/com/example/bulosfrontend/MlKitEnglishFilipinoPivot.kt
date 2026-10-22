package com.example.bulosfrontend

import android.content.Context
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class MlKitEnglishFilipinoPivot(private val context: Context) {
    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.TAGALOG)
        .build()
    private val translator = Translation.getClient(options)

    suspend fun translateEnglishToFilipino(text: String): String? {
        return try {
            translator.downloadModelIfNeeded().await()
            val result = translator.translate(text).await()
            result.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }
}
