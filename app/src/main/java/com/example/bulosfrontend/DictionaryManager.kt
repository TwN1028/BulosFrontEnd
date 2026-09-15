package com.example.bulosfrontend

import android.content.Context
import org.apache.poi.ss.usermodel.WorkbookFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DictionaryManager(private val context: Context) {
    // Standardized storage: Each row is a map from language enum to its translation
    private val dictionaryData = mutableListOf<Map<UiLanguage, String>>()
    
    // Multi-word phrases extracted from all sheets, sorted by word count descending
    private val phrasesCache = mutableListOf<Map<UiLanguage, String>>()
    
    private var sheetCount = 0
    var isLoaded = false
        private set

    // Normalizes strings: removes non-breaking spaces, hidden characters, and trims
    private fun clean(input: String?): String {
        if (input == null) return ""
        return input.replace("\u00A0", " ") // Replace non-breaking spaces
                    .replace(Regex("[\\p{C}]"), "") // Remove hidden control characters
                    .trim()
    }

    suspend fun load() = withContext(Dispatchers.IO) {
        if (isLoaded) return@withContext
        try {
            val assetManager = context.assets
            assetManager.open("dictionary.xlsx").use { inputStream ->
                WorkbookFactory.create(inputStream).use { workbook ->
                    dictionaryData.clear()
                    sheetCount = workbook.numberOfSheets
                    
                    for (s in 0 until workbook.numberOfSheets) {
                        val sheet = workbook.getSheetAt(s)
                        
                        // 1. Identify the header row and its column mapping for THIS sheet
                        var bestHeaderRowIndex = -1
                        var maxMatches = 0
                        val sheetMapping = mutableMapOf<Int, UiLanguage>()
                        val headerKeywords = listOf("english", "filipino", "bulos", "tagalog", "en", "fil", "bul", "ingles", "tag")

                        for (i in 0..minOf(10, sheet.lastRowNum)) {
                            val row = sheet.getRow(i) ?: continue
                            var matches = 0
                            val currentMapping = mutableMapOf<Int, UiLanguage>()
                            
                            for (j in 0 until row.lastCellNum) {
                                val cellValue = clean(row.getCell(j)?.toString()?.lowercase())
                                if (headerKeywords.any { cellValue.contains(it) }) {
                                    when {
                                        cellValue.contains("english") || cellValue == "en" || cellValue == "eng" || cellValue == "ingles" -> {
                                            currentMapping[j] = UiLanguage.ENGLISH
                                            matches++
                                        }
                                        cellValue.contains("filipino") || cellValue.contains("tagalog") || cellValue == "fil" || cellValue == "tag" -> {
                                            currentMapping[j] = UiLanguage.FILIPINO
                                            matches++
                                        }
                                        cellValue.contains("bulos") || cellValue == "bul" -> {
                                            currentMapping[j] = UiLanguage.BULOS
                                            matches++
                                        }
                                    }
                                }
                            }
                            
                            if (matches > maxMatches) {
                                maxMatches = matches
                                bestHeaderRowIndex = i
                                sheetMapping.clear()
                                sheetMapping.putAll(currentMapping)
                            }
                        }

                        // 2. Load Data using the sheet-specific mapping
                        if (sheetMapping.isNotEmpty()) {
                            val startDataRow = if (bestHeaderRowIndex != -1) bestHeaderRowIndex + 1 else 0
                            for (i in startDataRow..sheet.lastRowNum) {
                                val row = sheet.getRow(i) ?: continue
                                val entry = mutableMapOf<UiLanguage, String>()
                                sheetMapping.forEach { (colIndex, language) ->
                                    val cellValue = clean(row.getCell(colIndex)?.toString())
                                    if (cellValue.isNotEmpty()) {
                                        entry[language] = cellValue
                                    }
                                }
                                if (entry.isNotEmpty()) {
                                    dictionaryData.add(entry)
                                }
                            }
                        }
                    }
                    
                    preparePhrasesCache()
                }
            }
            isLoaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun preparePhrasesCache() {
        phrasesCache.clear()
        // Extract multi-word entries (any entry with at least one space)
        val allPhrases = dictionaryData.filter { row ->
            row.values.any { it.trim().contains(" ") }
        }
        
        // Sort by word count descending to ensure greedy matching (longest phrases first)
        phrasesCache.addAll(allPhrases.sortedByDescending { row ->
            row.values.maxOfOrNull { it.split(Regex("\\s+")).size } ?: 0
        })
    }

    private fun findExactMatch(text: String, sourceLang: UiLanguage, targetLang: UiLanguage): String? {
        val input = clean(text)
        val match = dictionaryData.find { row ->
            row[sourceLang]?.equals(input, ignoreCase = true) == true
        }
        return match?.get(targetLang)
    }

    fun translate(text: String, sourceLang: UiLanguage, targetLang: UiLanguage): String {
        val rawInput = text.trim()
        if (rawInput.isEmpty()) return ""
        
        // Diagnostic
        if (rawInput.uppercase() == "DEBUG_DICT") {
            return "Status: LOADED, Sheets: $sheetCount, Rows: ${dictionaryData.size}, Phrases: ${phrasesCache.size}"
        }

        if (!isLoaded) return "Dictionary loading..."
        if (dictionaryData.isEmpty()) return "Dictionary is empty"

        // --- TIER 1: Full Sentence Match ---
        // Clean punctuation for matching but preserve for result
        val sentenceRegex = Regex("^(.+?)([,.!?]+)?$")
        val matchResult = sentenceRegex.find(rawInput)
        val inputBody = clean(matchResult?.groupValues?.get(1) ?: rawInput)
        val inputPunctuation = matchResult?.groupValues?.getOrNull(2) ?: ""

        val fullMatch = findExactMatch(inputBody, sourceLang, targetLang)
        if (fullMatch != null) return fullMatch + inputPunctuation

        // --- TIER 2: Greedy Phrase Match ---
        var currentText = rawInput
        val replacements = mutableListOf<String>()
        
        // phrasesCache is already sorted by word count descending
        phrasesCache.forEach { row ->
            val phrase = row[sourceLang] ?: ""
            val translation = row[targetLang] ?: ""
            
            if (phrase.isNotEmpty() && phrase.contains(" ")) {
                // Word boundary check (respects punctuation/spaces)
                val regex = Regex("(?i)(?<![a-zA-Z0-9-])${Regex.escape(phrase)}(?![a-zA-Z0-9-])")
                if (regex.containsMatchIn(currentText)) {
                    val placeholder = "[[P${replacements.size}]]"
                    replacements.add(translation)
                    currentText = currentText.replace(regex, placeholder)
                }
            }
        }

        // --- TIER 3: Word-by-Word ---
        val tokens = currentText.split(Regex("\\s+"))
        val translatedTokens = tokens.map { token ->
            if (token.startsWith("[[P") && token.endsWith("]]")) return@map token

            // Try direct word match
            val directMatch = findExactMatch(token, sourceLang, targetLang)
            if (directMatch != null) return@map directMatch

            // Punctuation-aware word match
            val wordMatch = Regex("^(.+?)([,.!?]+)?$").find(token)
            if (wordMatch != null) {
                val coreWord = wordMatch.groupValues[1]
                val punctuation = wordMatch.groupValues[2]
                val coreMatch = findExactMatch(coreWord, sourceLang, targetLang)
                if (coreMatch != null) return@map coreMatch + punctuation
            }
            
            token // Keep original if no match
        }

        var finalResult = translatedTokens.joinToString(" ")
        
        // Restore replaced phrases
        replacements.forEachIndexed { index, translation ->
            finalResult = finalResult.replace("[[P$index]]", translation)
        }

        return finalResult
    }
}
