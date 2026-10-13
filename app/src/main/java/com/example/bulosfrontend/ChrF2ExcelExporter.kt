package com.example.bulosfrontend

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

object ChrF2ExcelExporter {
    private const val TAG = "ChrF2Excel"

    data class EvalResult(
        val flow: String,
        val input: String,
        val reference: String,
        val hypothesis: String,
        val chrf2: Double,
    )

    data class FlowStats(
        val flow: String,
        val count: Int,
        val mean: Double,
        val median: Double,
        val min: Double,
        val max: Double,
    )

    fun runAndExportExcel(context: Context) {
        try {
            val jsonString = context.assets.open("verified_dictionary_additions.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            val allEntries: List<Map<String, String>> = Gson().fromJson(jsonString, type)

            val sampleSize = (allEntries.size * 0.20).toInt().coerceAtLeast(1)
            val sampledEntries = allEntries.shuffled(Random(42)).take(sampleSize)

            val flows = listOf(
                Triple("eng->bul", UiLanguage.ENGLISH, UiLanguage.BULOS),
                Triple("fil->bul", UiLanguage.FILIPINO, UiLanguage.BULOS),
                Triple("bul->eng", UiLanguage.BULOS, UiLanguage.ENGLISH),
                Triple("bul->fil", UiLanguage.BULOS, UiLanguage.FILIPINO),
            )

            val allResults = mutableListOf<EvalResult>()
            val flowStatsList = mutableListOf<FlowStats>()

            val workbook = XSSFWorkbook()

            // Header Style
            val headerFont = workbook.createFont().apply {
                bold = true
                color = IndexedColors.WHITE.index
            }
            val headerStyle = workbook.createCellStyle().apply {
                setFont(headerFont)
                fillForegroundColor = IndexedColors.DARK_GREEN.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                alignment = HorizontalAlignment.CENTER
            }

            val summarySheet = workbook.createSheet("Aggregate Statistics")
            val detailSheet = workbook.createSheet("Detailed Results")

            val summaryHeader = summarySheet.createRow(0)
            listOf("Language Flow", "Sample Count", "Mean chrF2", "Median chrF2", "Min chrF2", "Max chrF2").forEachIndexed { index, header ->
                val cell = summaryHeader.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }

            val detailHeader = detailSheet.createRow(0)
            listOf("Language Flow", "Input Text", "Reference Text", "Hypothesis Text", "chrF2 Score").forEachIndexed { index, header ->
                val cell = detailHeader.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }

            var detailRowIdx = 1
            var summaryRowIdx = 1

            for ((flowName, sourceLang, targetLang) in flows) {
                val sourceKey = sourceLang.displayName
                val targetKey = targetLang.displayName
                val flowScores = mutableListOf<Double>()

                for (entry in sampledEntries) {
                    val input = entry[sourceKey].orEmpty()
                    val reference = entry[targetKey].orEmpty()
                    if (input.isBlank() || reference.isBlank() || reference == "—") continue

                    val dictionary = OfflineDictionary(listOf(entry))
                    val result = dictionary.lookup(input, sourceLang, targetLang)
                    val hypothesis = when (result) {
                        is OfflineLookupResult.Translation -> result.translatedText
                        else -> input
                    }

                    val score = calculateChrF(reference, hypothesis)
                    flowScores.add(score)
                    allResults.add(EvalResult(flowName, input, reference, hypothesis, score))

                    val row = detailSheet.createRow(detailRowIdx++)
                    row.createCell(0).setCellValue(flowName)
                    row.createCell(1).setCellValue(input)
                    row.createCell(2).setCellValue(reference)
                    row.createCell(3).setCellValue(hypothesis)
                    row.createCell(4).setCellValue(score)
                }

                if (flowScores.isNotEmpty()) {
                    val sorted = flowScores.sorted()
                    val mean = sorted.average()
                    val median = if (sorted.size % 2 == 0) {
                        (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
                    } else {
                        sorted[sorted.size / 2]
                    }
                    val min = sorted.first()
                    val max = sorted.last()

                    flowStatsList.add(FlowStats(flowName, flowScores.size, mean, median, min, max))

                    val sRow = summarySheet.createRow(summaryRowIdx++)
                    sRow.createCell(0).setCellValue(flowName)
                    sRow.createCell(1).setCellValue(flowScores.size.toDouble())
                    sRow.createCell(2).setCellValue(mean)
                    sRow.createCell(3).setCellValue(median)
                    sRow.createCell(4).setCellValue(min)
                    sRow.createCell(5).setCellValue(max)
                }
            }

            if (allResults.isNotEmpty()) {
                val allScores = allResults.map { it.chrf2 }.sorted()
                val overallMean = allScores.average()
                val overallMedian = if (allScores.size % 2 == 0) {
                    (allScores[allScores.size / 2 - 1] + allScores[allScores.size / 2]) / 2.0
                } else {
                    allScores[allScores.size / 2]
                }
                val overallMin = allScores.first()
                val overallMax = allScores.last()

                val oRow = summarySheet.createRow(summaryRowIdx)
                oRow.createCell(0).setCellValue("OVERALL")
                oRow.createCell(1).setCellValue(allResults.size.toDouble())
                oRow.createCell(2).setCellValue(overallMean)
                oRow.createCell(3).setCellValue(overallMedian)
                oRow.createCell(4).setCellValue(overallMin)
                oRow.createCell(5).setCellValue(overallMax)
            }

            for (i in 0..5) {
                summarySheet.autoSizeColumn(i)
                detailSheet.autoSizeColumn(i)
            }

            val outputFile = File(context.filesDir, "translation_evaluation_chrf2.xlsx")
            FileOutputStream(outputFile).use { fos ->
                workbook.write(fos as java.io.OutputStream)
            }
            workbook.close()

            Log.i(TAG, "=== FORMATED EXCEL EXPORT COMPLETED ===")
            Log.i(TAG, "Excel saved at: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Excel export failed: ${e.message}", e)
        }
    }

    private fun calculateChrF(reference: String, hypothesis: String, maxOrder: Int = 6, beta: Double = 2.0): Double {
        if (reference.isEmpty() && hypothesis.isEmpty()) return 1.0
        if (reference.isEmpty() || hypothesis.isEmpty()) return 0.0

        val refNorm = reference.lowercase()
        val hypNorm = hypothesis.lowercase()

        var totalPrecision = 0.0
        var totalRecall = 0.0

        for (n in 1..maxOrder) {
            val refNgrams = getNgrams(refNorm, n)
            val hypNgrams = getNgrams(hypNorm, n)

            if (refNgrams.isEmpty() && hypNgrams.isEmpty()) continue
            if (refNgrams.isEmpty() || hypNgrams.isEmpty()) continue

            var overlap = 0.0
            val refCounts = refNgrams.groupingBy { it }.eachCount()
            val hypCounts = hypNgrams.groupingBy { it }.eachCount()

            for ((ngram, hypCount) in hypCounts) {
                val refCount = refCounts[ngram] ?: 0
                overlap += minOf(hypCount, refCount)
            }

            val precision = if (hypNgrams.isNotEmpty()) overlap / hypNgrams.size else 0.0
            val recall = if (refNgrams.isNotEmpty()) overlap / refNgrams.size else 0.0

            totalPrecision += precision
            totalRecall += recall
        }

        val avgPrecision = totalPrecision / maxOrder
        val avgRecall = totalRecall / maxOrder

        if (avgPrecision == 0.0 && avgRecall == 0.0) return 0.0

        val betaSq = beta * beta
        return (1.0 + betaSq) * (avgPrecision * avgRecall) / (betaSq * avgPrecision + betaSq * avgRecall)
    }

    private fun getNgrams(text: String, n: Int): List<String> {
        if (text.length < n) return emptyList()
        return (0..text.length - n).map { text.substring(it, it + n) }
    }

    private val UiLanguage.displayName: String
        get() = when (this) {
            UiLanguage.ENGLISH -> "English"
            UiLanguage.FILIPINO -> "Filipino"
            UiLanguage.BULOS -> "Bulos"
        }
}
