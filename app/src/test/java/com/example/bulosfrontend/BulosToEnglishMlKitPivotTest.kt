package com.example.bulosfrontend

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BulosToEnglishMlKitPivotTest {

    @Test
    fun `offline bulos to filipino step converts buot ko eya to gusto ko siya`() {
        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        val rows: List<Map<String, String>> = File("src/main/assets/verified_dictionary_additions.json")
            .reader()
            .use { Gson().fromJson(it, type) }
        val offlineDict = OfflineDictionary(rows)

        val result = offlineDict.lookup("buot ko eya", UiLanguage.BULOS, UiLanguage.FILIPINO)
        assertTrue(result is OfflineLookupResult.Translation)

        val filipinoText = (result as OfflineLookupResult.Translation).translatedText
        assertEquals("Gusto ko siya", filipinoText)
    }

    @Test
    fun `offline bulos to filipino step converts buot ka eya to gusto ikaw siya`() {
        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        val rows: List<Map<String, String>> = File("src/main/assets/verified_dictionary_additions.json")
            .reader()
            .use { Gson().fromJson(it, type) }
        val offlineDict = OfflineDictionary(rows)

        val result = offlineDict.lookup("buot ka eya", UiLanguage.BULOS, UiLanguage.FILIPINO)
        assertTrue(result is OfflineLookupResult.Translation)

        val filipinoText = (result as OfflineLookupResult.Translation).translatedText
        assertEquals("Gusto ikaw siya", filipinoText)
    }
}
