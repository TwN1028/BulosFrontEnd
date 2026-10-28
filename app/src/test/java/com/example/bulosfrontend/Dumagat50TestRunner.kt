package com.example.bulosfrontend

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

class Dumagat50TestRunner {

    data class TestItem(
        val id: Int,
        val sourceLanguage: String, // "Filipino" or "English"
        val input: String,
        val langCode: String,      // "tl" or "en"
    )

    private val testItems = listOf(
        // Items 1-25: Filipino
        TestItem(1, "Filipino", "Pakisara ang pinto.", "tl"),
        TestItem(2, "Filipino", "Nasaan ang susi ng kotse?", "tl"),
        TestItem(3, "Filipino", "Magkita tayo mamayang hapon.", "tl"),
        TestItem(4, "Filipino", "May dala akong pagkain para sa iyo.", "tl"),
        TestItem(5, "Filipino", "Sandali lang, babalikan kita.", "tl"),
        TestItem(6, "Filipino", "Huwag mong kalimutan ang payong mo.", "tl"),
        TestItem(7, "Filipino", "Pakilagay ito sa ibabaw ng mesa.", "tl"),
        TestItem(8, "Filipino", "Anong oras nagsasara ang tindahan?", "tl"),
        TestItem(9, "Filipino", "Medyo malayo ang bahay nila.", "tl"),
        TestItem(10, "Filipino", "Pwede bang makiraan?", "tl"),
        TestItem(11, "Filipino", "Nag-iwan siya ng mensahe para sa iyo.", "tl"),
        TestItem(12, "Filipino", "Maaga akong nagising kaninang umaga.", "tl"),
        TestItem(13, "Filipino", "Wala nang laman ang bote.", "tl"),
        TestItem(14, "Filipino", "Pakibuksan ang bintana.", "tl"),
        TestItem(15, "Filipino", "Nag-aaral ang mga bata sa silid.", "tl"),
        TestItem(16, "Filipino", "Nasira ang charger ng telepono ko.", "tl"),
        TestItem(17, "Filipino", "May pasok ba bukas?", "tl"),
        TestItem(18, "Filipino", "Magdala ka ng ekstrang damit.", "tl"),
        TestItem(19, "Filipino", "Hindi ko makita ang hinahanap ko.", "tl"),
        TestItem(20, "Filipino", "Masarap ang niluto mong ulam.", "tl"),
        TestItem(21, "Filipino", "Umuuwi kami bago dumilim.", "tl"),
        TestItem(22, "Filipino", "Pakisabi sa kanya na nandito na ako.", "tl"),
        TestItem(23, "Filipino", "Kailangan kong bumili ng gamot.", "tl"),
        TestItem(24, "Filipino", "Maingay sa labas ng bahay.", "tl"),
        TestItem(25, "Filipino", "Maghintay ka sa tapat ng paaralan.", "tl"),

        // Items 26-50: English
        TestItem(26, "English", "Please keep your voice down.", "en"),
        TestItem(27, "English", "The keys are inside the drawer.", "en"),
        TestItem(28, "English", "We missed the last bus.", "en"),
        TestItem(29, "English", "Could you check the address again?", "en"),
        TestItem(30, "English", "Her phone battery is almost empty.", "en"),
        TestItem(31, "English", "I left my umbrella at the office.", "en"),
        TestItem(32, "English", "The meeting starts in half an hour.", "en"),
        TestItem(33, "English", "Please save a copy of this file.", "en"),
        TestItem(34, "English", "Someone is knocking at the back door.", "en"),
        TestItem(35, "English", "The soup needs a little more salt.", "en"),
        TestItem(36, "English", "He forgot to bring his lunch.", "en"),
        TestItem(37, "English", "Can you move this chair closer?", "en"),
        TestItem(38, "English", "The road is closed for repairs.", "en"),
        TestItem(39, "English", "I will send the details tonight.", "en"),
        TestItem(40, "English", "She has been waiting since noon.", "en"),
        TestItem(41, "English", "Put the groceries on the kitchen counter.", "en"),
        TestItem(42, "English", "Our appointment was moved to Friday.", "en"),
        TestItem(43, "English", "Please charge the tablet before class.", "en"),
        TestItem(44, "English", "The package arrived earlier than expected.", "en"),
        TestItem(45, "English", "I need a quiet place to study.", "en"),
        TestItem(46, "English", "Turn left at the next street.", "en"),
        TestItem(47, "English", "Neither answer seems correct.", "en"),
        TestItem(48, "English", "We ran out of clean towels.", "en"),
        TestItem(49, "English", "Keep this receipt in case you need it.", "en"),
        TestItem(50, "English", "Let me know when you reach home.", "en"),
    )

    @Test
    fun execute50ItemRegressionTest() {
        // Clear grammar registry
        BulosGrammarRuleRegistry.clear()

        // Load offline dataset
        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        val rows: List<Map<String, String>> = File("src/main/assets/verified_dictionary_additions.json")
            .reader()
            .use { Gson().fromJson(it, type) }
        val offlineDict = OfflineDictionary(rows)

        // Setup OkHttpClient for Backend API call
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        val gson = Gson()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val testDeviceId = UUID.randomUUID().toString()

        val sb = StringBuilder()
        sb.appendLine("# 50-Item Bilingual Translator Regression Test Results")
        sb.appendLine()
        sb.appendLine("| # | Source Language | Input | Backend Output | Android App Offline Output | Lookup Method | Classification / Issue |")
        sb.appendLine("|---|---|---|---|---|---|---|")

        var exactMatches = 0
        var wordFallbackCount = 0
        var unchangedCount = 0
        var backendSuccessCount = 0

        for (item in testItems) {
            val srcUiLang = if (item.sourceLanguage == "Filipino") UiLanguage.FILIPINO else UiLanguage.ENGLISH

            // 1. Android Offline Lookup
            val offlineLookup = offlineDict.lookup(item.input, srcUiLang, UiLanguage.BULOS)
            val androidOutput = when (offlineLookup) {
                is OfflineLookupResult.Translation -> offlineLookup.translatedText
                else -> item.input
            }
            val method = when (offlineLookup) {
                is OfflineLookupResult.Translation -> offlineLookup.method
                else -> "none"
            }

            // 2. Online Backend API Call
            var backendOutput = "N/A (Server Timeout / Offline)"
            try {
                val jsonPayload = gson.toJson(
                    mapOf(
                        "source_language" to item.langCode,
                        "target_language" to "bul",
                        "text" to item.input,
                    )
                )
                val request = Request.Builder()
                    .url("https://buotkoikitam.onrender.com/api/v1/translate/")
                    .header("X-Device-ID", testDeviceId)
                    .post(jsonPayload.toRequestBody(mediaType))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string()
                        if (!bodyString.isNullOrBlank()) {
                            val respMap = gson.fromJson(bodyString, Map::class.java)
                            backendOutput = respMap["translated_text"]?.toString() ?: "Empty response"
                            backendSuccessCount++
                        }
                    } else {
                        backendOutput = "HTTP ${response.code}"
                    }
                }
            } catch (e: Exception) {
                backendOutput = "Server unavailable (${e.javaClass.simpleName})"
            }

            val issueLabel = when {
                backendOutput.startsWith("Server unavailable") || backendOutput.startsWith("N/A") || backendOutput.startsWith("HTTP") -> "Server Offline / Endpoint Error"
                androidOutput == item.input -> "Unchanged source text"
                method == "offline_word_by_word_fallback" -> "Word-by-word fallback"
                method == "offline_bridge_english_filipino" -> "English-Filipino bridge"
                method == "offline_exact_phrase" -> "Exact phrase match"
                else -> "Evaluated"
            }

            when (method) {
                "offline_exact_phrase" -> exactMatches++
                "offline_word_by_word_fallback", "offline_bridge_english_filipino" -> wordFallbackCount++
                "offline_original_fallback" -> unchangedCount++
            }

            sb.appendLine("| ${item.id} | ${item.sourceLanguage} | ${item.input} | $backendOutput | $androidOutput | $method | $issueLabel |")
        }

        sb.appendLine()
        sb.appendLine("## Summary Statistics")
        sb.appendLine("- **Total Scored Items**: 50 (25 Filipino, 25 English)")
        sb.appendLine("- **Backend Online API Successes**: $backendSuccessCount / 50")
        sb.appendLine("- **Android App Offline Exact Phrase Matches**: $exactMatches / 50")
        sb.appendLine("- **Android App Offline Word/Bridge Fallbacks**: $wordFallbackCount / 50")
        sb.appendLine("- **Android App Offline Unchanged Fallbacks**: $unchangedCount / 50")

        val resultFile = File("src/test/java/com/example/bulosfrontend/dumagat_50_results.md")
        resultFile.writeText(sb.toString())

        assertTrue(resultFile.exists())
    }
}
