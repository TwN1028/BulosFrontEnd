package com.example.bulosfrontend

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ValidationOfflineTestRunner {

    @Test
    fun generateValidationTestOfflineSpreadsheet() {
        // Clear grammar rule registry
        BulosGrammarRuleRegistry.clear()

        // Load offline dataset
        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        val rows: List<Map<String, String>> = File("src/main/assets/verified_dictionary_additions.json")
            .reader()
            .use { Gson().fromJson(it, type) }
        val offlineDict = OfflineDictionary(rows)

        val excelFile = File("../validation_test_offline.xlsx")
            .takeIf { it.exists() }
            ?: File("src/main/validation_test_offline.xlsx")
            .takeIf { it.exists() }
            ?: File("validation_test_offline.xlsx")
            .takeIf { it.exists() }
            ?: File("src/main/Dumagat_50_Common_Test_Set_REVISED_2026-10-25.xlsx")

        assertTrue(excelFile.exists())

        val workbook = FileInputStream(excelFile).use { stream ->
            WorkbookFactory.create(stream)
        }
        val sheet = workbook.getSheetAt(0)

        val filipinoPrompts = mutableListOf<String>()
        val bulosOutputsFilipino = mutableListOf<String>()

        val englishPrompts = mutableListOf<String>()
        val bulosOutputsEnglish = mutableListOf<String>()

        // 1. Process Items 1 to 25: Filipino -> Bulos (Excel rows 1 to 25, 0-based row index 1 to 25)
        for (i in 0 until 25) {
            val rowIndex = 1 + i
            val row = sheet.getRow(rowIndex) ?: sheet.createRow(rowIndex)
            val flowCell = row.getCell(0) ?: row.createCell(0)
            flowCell.setCellValue("Filipino → Bulos")

            val input = row.getCell(1)?.stringCellValue.orEmpty().trim()
            filipinoPrompts.add(input)

            val lookupResult = offlineDict.lookup(input, UiLanguage.FILIPINO, UiLanguage.BULOS)
            val actualOutput = when (lookupResult) {
                is OfflineLookupResult.Translation -> lookupResult.translatedText
                else -> input
            }
            bulosOutputsFilipino.add(actualOutput)

            val actualCell = row.getCell(3) ?: row.createCell(3)
            actualCell.setCellValue(actualOutput)
        }

        // 2. Process Items 26 to 50: English -> Bulos (Excel rows 26 to 50, 0-based row index 26 to 50)
        for (i in 0 until 25) {
            val rowIndex = 26 + i
            val row = sheet.getRow(rowIndex) ?: sheet.createRow(rowIndex)
            val flowCell = row.getCell(0) ?: row.createCell(0)
            flowCell.setCellValue("English → Bulos")

            val input = row.getCell(1)?.stringCellValue.orEmpty().trim()
            englishPrompts.add(input)

            val lookupResult = offlineDict.lookup(input, UiLanguage.ENGLISH, UiLanguage.BULOS)
            val actualOutput = when (lookupResult) {
                is OfflineLookupResult.Translation -> lookupResult.translatedText
                else -> input
            }
            bulosOutputsEnglish.add(actualOutput)

            val actualCell = row.getCell(3) ?: row.createCell(3)
            actualCell.setCellValue(actualOutput)
        }

        // 3. Process Items 51 to 75: Bulos -> Filipino (Excel rows 51 to 75, 0-based row index 51 to 75)
        for (i in 0 until 25) {
            val rowIndex = 51 + i
            val row = sheet.getRow(rowIndex) ?: sheet.createRow(rowIndex)

            val flowCell = row.getCell(0) ?: row.createCell(0)
            flowCell.setCellValue("Bulos → Filipino")

            val bulosInput = bulosOutputsFilipino[i]
            val expectedFilipino = filipinoPrompts[i]

            val inputCell = row.getCell(1) ?: row.createCell(1)
            inputCell.setCellValue(bulosInput)

            val expectedCell = row.getCell(2) ?: row.createCell(2)
            expectedCell.setCellValue(expectedFilipino)

            val lookupResult = offlineDict.lookup(bulosInput, UiLanguage.BULOS, UiLanguage.FILIPINO)
            val actualOutput = when (lookupResult) {
                is OfflineLookupResult.Translation -> lookupResult.translatedText
                else -> bulosInput
            }

            val actualCell = row.getCell(3) ?: row.createCell(3)
            actualCell.setCellValue(actualOutput)
        }

        // 4. Process Items 76 to 100: Bulos -> English (Excel rows 76 to 100, 0-based row index 76 to 100)
        for (i in 0 until 25) {
            val rowIndex = 76 + i
            val row = sheet.getRow(rowIndex) ?: sheet.createRow(rowIndex)

            val flowCell = row.getCell(0) ?: row.createCell(0)
            flowCell.setCellValue("Bulos → English")

            val bulosInput = bulosOutputsEnglish[i]
            val expectedEnglish = englishPrompts[i]

            val inputCell = row.getCell(1) ?: row.createCell(1)
            inputCell.setCellValue(bulosInput)

            val expectedCell = row.getCell(2) ?: row.createCell(2)
            expectedCell.setCellValue(expectedEnglish)

            // Step 1 Bulos -> Filipino
            val filipinoResult = offlineDict.lookup(bulosInput, UiLanguage.BULOS, UiLanguage.FILIPINO)
            val intermediateFilipino = when (filipinoResult) {
                is OfflineLookupResult.Translation -> filipinoResult.translatedText
                else -> bulosInput
            }

            // Step 2 Filipino -> English via offline lookup
            val englishResult = offlineDict.lookup(intermediateFilipino, UiLanguage.FILIPINO, UiLanguage.ENGLISH)
            val actualOutput = when (englishResult) {
                is OfflineLookupResult.Translation -> englishResult.translatedText
                else -> intermediateFilipino
            }

            val actualCell = row.getCell(3) ?: row.createCell(3)
            actualCell.setCellValue(actualOutput)
        }

        // Save to validation_test_offline.xlsx in app/src/main/ and root directory
        val outputAppMain = File("src/main/validation_test_offline.xlsx")
        runCatching {
            FileOutputStream(outputAppMain).use { out -> workbook.write(out) }
        }
        val outputRoot = File("../validation_test_offline.xlsx")
        runCatching {
            FileOutputStream(outputRoot).use { out -> workbook.write(out) }
        }
        workbook.close()

        assertTrue(outputAppMain.exists())
        assertTrue(outputRoot.exists())
    }
}
