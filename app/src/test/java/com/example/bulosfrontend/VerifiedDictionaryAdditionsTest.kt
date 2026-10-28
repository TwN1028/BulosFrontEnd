package com.example.bulosfrontend

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VerifiedDictionaryAdditionsTest {

    @Test
    fun `convert workbook to verified_dictionary_additions json`() {
        val excelFile = File("src/main/assets/Dumagat_Dataset_2026-10-22_22-42.xlsx")
            .takeIf { it.exists() }
            ?: File("src/main/Dumagat_Dataset_2026-10-22_22-42.xlsx")

        assertTrue(excelFile.exists())
        val rows = excelFile.inputStream().use { stream ->
            DatasetConverter.convertStreamToList(stream)
        }
        assertTrue(rows.isNotEmpty())

        val outputFile = File("src/main/assets/verified_dictionary_additions.json")
        outputFile.writer().use { writer ->
            GsonBuilder().setPrettyPrinting().create().toJson(rows, writer)
        }
        assertTrue(outputFile.exists())
    }

    @Test
    fun `bundled additions contain unique complete rows`() {
        val additions = bundledAdditions()

        val merged = mergeVerifiedDictionaryAdditions(additions, emptyList())

        assertTrue(additions.isNotEmpty())
        assertTrue(merged.isNotEmpty())
        assertEquals(merged, mergeVerifiedDictionaryAdditions(merged, emptyList()))
    }

    @Test
    fun `bundled phrase translates exactly in all four supported directions`() {
        val phrase = bundledAdditions().first {
            val en = it["English"].orEmpty()
            val fil = it["Filipino"].orEmpty()
            val bul = it["Bulos"].orEmpty()
            en.isNotBlank() && en != "—" && en != "-" &&
                fil.isNotBlank() && fil != "—" && fil != "-" &&
                bul.isNotBlank() && bul != "—" && bul != "-" &&
                !en.endsWith(".") && !bul.endsWith(".")
        }
        val dictionary = OfflineDictionary(mergeVerifiedDictionaryAdditions(listOf(phrase), emptyList()))

        assertTranslation(dictionary, phrase, UiLanguage.ENGLISH, UiLanguage.BULOS)
        assertTranslation(dictionary, phrase, UiLanguage.FILIPINO, UiLanguage.BULOS)
        assertTranslation(dictionary, phrase, UiLanguage.BULOS, UiLanguage.ENGLISH)
        assertTranslation(dictionary, phrase, UiLanguage.BULOS, UiLanguage.FILIPINO)
    }

    @Test
    fun `repeated initialization and sync-style replacement retain additions without duplicates`() {
        val additions = bundledAdditions()
        val mergedAdditions = mergeVerifiedDictionaryAdditions(additions, emptyList())
        val initialCache = listOf(row("Existing", "Umiiral", "Masan"))
        val afterLoad = mergeVerifiedDictionaryAdditions(mergedAdditions, initialCache)
        val afterRepeatedLoad = mergeVerifiedDictionaryAdditions(afterLoad, mergedAdditions)
        val afterSync = mergeVerifiedDictionaryAdditions(mergedAdditions, listOf(row("Synced", "Na-sync", "Na-sync")))

        assertEquals(1 + mergedAdditions.size, afterLoad.size)
        assertEquals(afterLoad, afterRepeatedLoad)
        assertEquals(1 + mergedAdditions.size, afterSync.size)
    }

    @Test
    fun `bundled verified additions take authority over secondary cached entries when they collide`() {
        val bundled = bundledAdditions().first { it["English"] != "—" && it["English"] != "-" && it["Bulos"] != "—" && it["Bulos"] != "-" }
        val cached = mapOf(
            OfflineDictionary.CATEGORY_KEY to "Sentences / Phrases",
            "English" to bundled.getValue("English"),
            "Filipino" to "Cached Filipino value",
            "Bulos" to "Cached Bulos value",
        )

        val merged = mergeVerifiedDictionaryAdditions(listOf(bundled), listOf(cached))
        val result = OfflineDictionary(merged).lookup(
            bundled.getValue("English"),
            UiLanguage.ENGLISH,
            UiLanguage.BULOS,
        )

        assertEquals(1, merged.size)
        val expected = bundled.getValue("Bulos").trim().replaceFirstChar { if (it.isLowerCase()) it.uppercaseChar() else it }
        assertEquals(expected, (result as OfflineLookupResult.Translation).translatedText)
    }

    @Test
    fun `preserves verified dataset rows and adds only collision-free secondary rows`() {
        val verified = listOf(
            row(english = "Hello", filipino = "Kumusta", bulos = "Kumusta"),
        )
        val cached = listOf(
            row(english = "hello!", filipino = "Iba", bulos = "Iba"),
            row(english = "Good morning", filipino = "Magandang umaga", bulos = "Masdep a abak"),
            row(english = "Incomplete", filipino = "Hindi kumpleto", bulos = "—"),
        )

        val merged = mergeVerifiedDictionaryAdditions(verified, cached)

        assertEquals(verified.single(), merged.first())
        assertEquals(2, merged.size)
        assertEquals("Good morning", merged.last().getValue("English"))
        val lookup = OfflineDictionary(merged).lookup("hello", UiLanguage.ENGLISH, UiLanguage.BULOS)
        assertTrue(lookup is OfflineLookupResult.Translation)
        assertEquals("Kumusta", (lookup as OfflineLookupResult.Translation).translatedText)
    }

    @Test
    fun `does not add secondary rows that conflict with an earlier verified row`() {
        val verified = listOf(
            row(english = "Source", filipino = "Pinagmulan", bulos = "Inapuan"),
        )
        val secondary = listOf(
            row(english = "Different source", filipino = "pinagmulan", bulos = "Iba"),
        )

        val merged = mergeVerifiedDictionaryAdditions(verified, secondary)

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
        val expected = phrase.getValue(target.displayName).trim().replaceFirstChar { if (it.isLowerCase()) it.uppercaseChar() else it }
        assertEquals(expected, (result as OfflineLookupResult.Translation).translatedText)
    }

    private val UiLanguage.displayName: String
        get() = when (this) {
            UiLanguage.ENGLISH -> "English"
            UiLanguage.FILIPINO -> "Filipino"
            UiLanguage.BULOS -> "Bulos"
        }
}
