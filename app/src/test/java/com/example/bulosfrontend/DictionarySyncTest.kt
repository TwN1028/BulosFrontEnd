package com.example.bulosfrontend

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class DictionarySyncTest {
    @Test
    fun categoryWithFewerThan100EntriesUsesOneRequest() = runBlocking {
        assertPagination(total = 37, expectedSkips = listOf(0))
    }

    @Test
    fun categoryWithExactly100EntriesUsesOneRequest() = runBlocking {
        assertPagination(total = 100, expectedSkips = listOf(0))
    }

    @Test
    fun categoryWith101EntriesUsesSecondRequestAndIncludesLastEntry() = runBlocking {
        val requests = mutableListOf<Pair<Int, Int>>()
        val source = entries(100) + OfflineDictionaryEntryResponse(
            english = "one hundred",
            filipino = "isang daan",
            bulos = "isin a dean",
        )

        val downloaded = downloadCompleteDictionary(
            categories = listOf(DictionaryCategoryResponse("numbers-from-0-to-100", 101)),
            fetchPage = { _, skip, limit ->
                requests += skip to limit
                source.drop(skip).take(limit)
            },
        )

        assertEquals(listOf(0 to 100, 100 to 100), requests)
        assertEquals(101, downloaded.size)
        assertTrue(
            downloaded.any {
                it["English"] == "one hundred" &&
                    it["Filipino"] == "isang daan" &&
                    it["Bulos"] == "isin a dean" &&
                    it[OfflineDictionary.CATEGORY_KEY] == "numbers-from-0-to-100"
            },
        )
    }

    @Test
    fun completeDownloadCanSaveAll949DeclaredEntries() = runBlocking {
        val counts = List(27) { 30 } + listOf(38, 101)
        val categories = counts.mapIndexed { index, count -> DictionaryCategoryResponse("category-$index", count) }
        var saved: List<Map<String, String>> = emptyList()

        refreshCompleteDictionary(
            categories = categories,
            fetchPage = { category, skip, limit ->
                val categoryIndex = category.substringAfterLast('-').toInt()
                entries(counts[categoryIndex], "$category-").drop(skip).take(limit)
            },
            save = { saved = it },
        )

        assertEquals(949, saved.size)
    }

    @Test
    fun failedRefreshDoesNotReplaceExistingCache() = runBlocking {
        val existingCache = listOf(mapOf("English" to "existing", "Bulos" to "cached"))
        var saved = existingCache

        runCatching {
            refreshCompleteDictionary(
                categories = listOf(DictionaryCategoryResponse("first", 1), DictionaryCategoryResponse("broken", 1)),
                fetchPage = { category, _, _ ->
                    if (category == "broken") throw IOException("network failed")
                    entries(1)
                },
                save = { saved = it },
            )
        }

        assertEquals(existingCache, saved)
    }

    private suspend fun assertPagination(total: Int, expectedSkips: List<Int>) {
        val source = entries(total)
        val requests = mutableListOf<Int>()
        val downloaded = downloadCompleteDictionary(
            categories = listOf(DictionaryCategoryResponse("test", total)),
            fetchPage = { _, skip, limit ->
                requests += skip
                source.drop(skip).take(limit)
            },
        )
        assertEquals(expectedSkips, requests)
        assertEquals(total, downloaded.size)
    }

    private fun entries(count: Int, prefix: String = "entry-"): List<OfflineDictionaryEntryResponse> =
        List(count) { index ->
            OfflineDictionaryEntryResponse(
                english = "$prefix$index-en",
                filipino = "$prefix$index-fil",
                bulos = "$prefix$index-bul",
            )
        }
}
