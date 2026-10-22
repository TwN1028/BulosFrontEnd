package com.example.bulosfrontend

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VerifiedDictionaryAdditionsTest {
    @Test
    fun `bundled additions contain unique complete rows`() {
        val additions = bundledAdditions()

        val merged = mergeVerifiedDictionaryAdditions(emptyList(), additions)

        assertTrue(additions.isNotEmpty())
        assertTrue(merged.isNotEmpty())
        assertEquals(merged, mergeVerifiedDictionaryAdditions(emptyList(), merged))
    }

    @Test
    fun `bundled phrase translates exactly in all four supported directions`() {
        val phrase = bundledAdditions().first {
            val en = it["English"].orEmpty()
            val bul = it["Bulos"].orEmpty()
            en.isNotBlank() && bul.isNotBlank() && !en.endsWith(".") && !bul.endsWith(".") && it[OfflineDictionary.CATEGORY_KEY]?.contains("phrase", true) == true
        }
        val dictionary = OfflineDictionary(mergeVerifiedDictionaryAdditions(emptyList(), listOf(phrase)))

        assertTranslation(dictionary, phrase, UiLanguage.ENGLISH, UiLanguage.BULOS)
        assertTranslation(dictionary, phrase, UiLanguage.FILIPINO, UiLanguage.BULOS)
        assertTranslation(dictionary, phrase, UiLanguage.BULOS, UiLanguage.ENGLISH)
        assertTranslation(dictionary, phrase, UiLanguage.BULOS, UiLanguage.FILIPINO)
    }

    @Test
    fun `repeated initialization and sync-style replacement retain additions without duplicates`() {
        val additions = bundledAdditions()
        val mergedAdditions = mergeVerifiedDictionaryAdditions(emptyList(), additions)
        val initialCache = listOf(row("Existing", "Umiiral", "Masan"))
        val afterLoad = mergeVerifiedDictionaryAdditions(initialCache, mergedAdditions)
        val afterRepeatedLoad = mergeVerifiedDictionaryAdditions(afterLoad, mergedAdditions)
        val afterSync = mergeVerifiedDictionaryAdditions(listOf(row("Synced", "Na-sync", "Na-sync")), mergedAdditions)

        assertEquals(1 + mergedAdditions.size, afterLoad.size)
        assertEquals(afterLoad, afterRepeatedLoad)
        assertEquals(1 + mergedAdditions.size, afterSync.size)
        assertTranslation(
            OfflineDictionary(afterSync),
            mergedAdditions.first { it["English"] != "—" && it["Bulos"] != "—" },
            UiLanguage.ENGLISH,
            UiLanguage.BULOS,
        )
    }

    @Test
    fun `cache phrase remains authoritative when it collides with a bundled phrase`() {
        val bundled = bundledAdditions().first { it["English"] != "—" && it["Bulos"] != "—" }
        val cached = mapOf(
            OfflineDictionary.CATEGORY_KEY to "Sentences / Phrases",
            "English" to bundled.getValue("English"),
            "Filipino" to "Cached Filipino value",
            "Bulos" to "Cached Bulos value",
        )

        val merged = mergeVerifiedDictionaryAdditions(listOf(cached), listOf(bundled))
        val result = OfflineDictionary(merged).lookup(
            bundled.getValue("English"),
            UiLanguage.ENGLISH,
            UiLanguage.BULOS,
        )

        assertEquals(1, merged.size)
        assertEquals("Cached Bulos value", (result as OfflineLookupResult.Translation).translatedText)
    }

    @Test
    fun `preserves cached rows and adds only collision-free complete rows`() {
        val cached = listOf(
            row(english = "Hello", filipino = "Kumusta", bulos = "Kumusta"),
        )
        val additions = listOf(
            row(english = "hello!", filipino = "Iba", bulos = "Iba"),
            row(english = "Good morning", filipino = "Magandang umaga", bulos = "Masdep a abak"),
            row(english = "Incomplete", filipino = "Hindi kumpleto", bulos = "—"),
        )

        val merged = mergeVerifiedDictionaryAdditions(cached, additions)

        assertEquals(cached.single(), merged.first())
        assertEquals(2, merged.size)
        assertEquals("Good morning", merged.last().getValue("English"))
        val lookup = OfflineDictionary(merged).lookup("hello", UiLanguage.ENGLISH, UiLanguage.BULOS)
        assertTrue(lookup is OfflineLookupResult.Translation)
        assertEquals("Kumusta", (lookup as OfflineLookupResult.Translation).translatedText)
    }

    @Test
    fun `does not add rows that conflict with an earlier bundled row`() {
        val additions = listOf(
            row(english = "Source", filipino = "Pinagmulan", bulos = "Inapuan"),
            row(english = "Different source", filipino = "pinagmulan", bulos = "Iba"),
        )

        val merged = mergeVerifiedDictionaryAdditions(emptyList(), additions)

        assertEquals(1, merged.size)
        assertEquals("Source", merged.single().getValue("English"))
    }

    private fun row(english: String, filipino: String, bulos: String): Map<String, String> = mapOf(
        OfflineDictionary.CATEGORY_KEY to "Words",
        "English" to english,
        "Filipino" to filipino,
        "Bulos" to bulos,
    )

    private fun bundledAdditions(): List<Map<String, String>> {
        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        return File("src/main/assets/verified_dictionary_additions.json")
            .reader()
            .use { Gson().fromJson(it, type) }
    }

    private fun assertTranslation(
        dictionary: OfflineDictionary,
        phrase: Map<String, String>,
        source: UiLanguage,
        target: UiLanguage,
    ) {
        val result = dictionary.lookup(phrase.getValue(source.displayName), source, target)
        assertTrue(result is OfflineLookupResult.Translation)
        assertEquals(phrase.getValue(target.displayName), (result as OfflineLookupResult.Translation).translatedText)
    }

    private val UiLanguage.displayName: String
        get() = when (this) {
            UiLanguage.ENGLISH -> "English"
            UiLanguage.FILIPINO -> "Filipino"
            UiLanguage.BULOS -> "Bulos"
        }
}
