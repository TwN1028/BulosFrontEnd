package com.example.bulosfrontend

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.InputStream

object DatasetConverter {
    private const val TAG = "DatasetConverter"
    const val TARGET_EXCEL_FILENAME = "Dumagat_Dataset_2026-10-22_22-42.xlsx"

    fun convertWorkbookToJson(context: Context) {
        try {
            val excelStream = context.assets.open(TARGET_EXCEL_FILENAME)
            val outputList = convertStreamToList(excelStream)
            val outputFile = File(context.filesDir, "verified_dictionary_additions.json")
            val gson = GsonBuilder().setPrettyPrinting().create()
            outputFile.writer().use { writer ->
                gson.toJson(outputList, writer)
            }

            Log.i(TAG, "=== DATASET CONVERSION REPORT ===")
            Log.i(TAG, "Final deduplicated output rows saved: ${outputList.size}")
            Log.i(TAG, "Output JSON saved to: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Dataset conversion failed: ${e.message}", e)
        }
    }

    fun convertStreamToList(excelStream: InputStream): List<Map<String, String>> {
        val formatter = DataFormatter()
        val allRows = mutableMapOf<String, MutableMap<String, String>>()

        WorkbookFactory.create(excelStream).use { workbook ->
            for (i in 0 until workbook.numberOfSheets) {
                val sheet = workbook.getSheetAt(i)
                val sheetName = sheet.sheetName
                if (sheetName.uppercase().contains("GRAMMAR EVIDENCE") || sheetName.uppercase().contains("ORTHOGRAPHY NOTES")) {
                    continue
                }

                if (sheet.physicalNumberOfRows <= 1) continue

                val headerRow = sheet.getRow(0) ?: continue
                val colMap = mutableMapOf<String, Int>()
                for (cell in headerRow) {
                    val headerText = formatter.formatCellValue(cell).trim().lowercase()
                    if (headerText.contains("english") || headerText.contains("eng")) colMap["English"] = cell.columnIndex
                    if (headerText.contains("filipino") || headerText.contains("tagalog") || headerText.contains("fil") || headerText.contains("tl")) colMap["Filipino"] = cell.columnIndex
                    if (headerText.contains("bulos") || headerText.contains("bul") || headerText.contains("dumaget")) colMap["Bulos"] = cell.columnIndex
                }

                val engIdx = colMap["English"] ?: 0
                val filIdx = colMap["Filipino"] ?: 1
                val bulIdx = colMap["Bulos"] ?: 2

                for (r in 1 until sheet.physicalNumberOfRows) {
                    val row = sheet.getRow(r) ?: continue
                    val eng = formatter.formatCellValue(row.getCell(engIdx)).trim()
                    val fil = formatter.formatCellValue(row.getCell(filIdx)).trim()
                    val bul = formatter.formatCellValue(row.getCell(bulIdx)).trim()

                    if (eng.isBlank() && fil.isBlank() && bul.isBlank()) continue
                    if (eng == "—" && fil == "—" && bul == "—") continue

                    val entry = mapOf(
                        OfflineDictionary.CATEGORY_KEY to sheetName,
                        "English" to if (eng.isNotBlank()) eng else "—",
                        "Filipino" to if (fil.isNotBlank()) fil else "—",
                        "Bulos" to if (bul.isNotBlank()) bul else "—",
                    )

                    val fingerprint = "${entry["English"]?.lowercase()}|${entry["Filipino"]?.lowercase()}|${entry["Bulos"]?.lowercase()}"
                    if (!allRows.containsKey(fingerprint)) {
                        allRows[fingerprint] = entry.toMutableMap()
                    }
                }
            }
        }
        return allRows.values.toList()
    }
}
