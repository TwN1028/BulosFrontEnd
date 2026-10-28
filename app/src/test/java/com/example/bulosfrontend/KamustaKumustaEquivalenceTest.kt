package com.example.bulosfrontend

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KamustaKumustaEquivalenceTest {

    @Test
    fun kamustaAndKumustaProduceIdenticalTranslations() {
        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        val rows: List<Map<String, String>> = File("src/main/assets/verified_dictionary_additions.json")
            .reader()
            .use { Gson().fromJson(it, type) }
        val fullDict = OfflineDictionary(rows)

        val resKamusta = fullDict.lookup("kamusta", UiLanguage.FILIPINO, UiLanguage.BULOS)
        val resKumusta = fullDict.lookup("kumusta", UiLanguage.FILIPINO, UiLanguage.BULOS)

        assertTrue(resKamusta is OfflineLookupResult.Translation)
        assertTrue(resKumusta is OfflineLookupResult.Translation)

        val textKamusta = (resKamusta as OfflineLookupResult.Translation).translatedText
        val textKumusta = (resKumusta as OfflineLookupResult.Translation).translatedText

        assertEquals("Magiano kamu di?", textKamusta)
        assertEquals("Magiano kamu di?", textKumusta)
        assertEquals(textKamusta, textKumusta)
    }

    @Test
    fun kamustaKaAndKumustaKaProduceIdenticalTranslations() {
        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        val rows: List<Map<String, String>> = File("src/main/assets/verified_dictionary_additions.json")
            .reader()
            .use { Gson().fromJson(it, type) }
        val fullDict = OfflineDictionary(rows)

        val resKamustaKa = fullDict.lookup("kamusta ka", UiLanguage.FILIPINO, UiLanguage.BULOS)
        val resKumustaKa = fullDict.lookup("kumusta ka", UiLanguage.FILIPINO, UiLanguage.BULOS)

        assertTrue(resKamustaKa is OfflineLookupResult.Translation)
        assertTrue(resKumustaKa is OfflineLookupResult.Translation)

        val textKamustaKa = (resKamustaKa as OfflineLookupResult.Translation).translatedText
        val textKumustaKa = (resKumustaKa as OfflineLookupResult.Translation).translatedText

        assertEquals("Magiano kamu di?", textKamustaKa)
        assertEquals("Magiano kamu di?", textKumustaKa)
        assertEquals(textKamustaKa, textKumustaKa)
    }

    @Test
    fun textNormalizerMapsKamustaToKumustaInMatchingForm() {
        assertEquals("kumusta", TextNormalizer.normalizeForMatching("kamusta"))
        assertEquals("kumusta ka", TextNormalizer.normalizeForMatching("kamusta ka?"))
        assertEquals("kumusta po kayo", TextNormalizer.normalizeForMatching("Kamusta po kayo!"))
    }
}
