package com.example.bulosfrontend

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class ChrF2ExcelExportTest {

    @Test
    fun convertCsvToFormattedExcel() {
        runConversion()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runConversion()
        }

        fun runConversion() {
            val csvFile = File("translation_evaluation_chrf2.csv")
            if (!csvFile.exists()) {
                println("CSV file not found at ${csvFile.absolutePath}")
                return
            }

            val lines = csvFile.readLines()
            val workbook = XSSFWorkbook()

            // Styles
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

            var isDetail = false
            var summaryRowIdx = 0
            var detailRowIdx = 0

            for (line in lines) {
                if (line.isBlank()) {
                    isDetail = true
                    continue
                }
                val tokens = parseCsvLine(line)
                if (tokens.isEmpty()) continue

                if (!isDetail) {
                    val row = summarySheet.createRow(summaryRowIdx++)
                    tokens.forEachIndexed { colIdx, token ->
                        val cell = row.createCell(colIdx)
                        if (summaryRowIdx == 1) {
                            cell.setCellValue(token)
                            cell.cellStyle = headerStyle
                        } else {
                            val num = token.toDoubleOrNull()
                            if (num != null) cell.setCellValue(num) else cell.setCellValue(token)
                        }
                    }
                } else {
                    val row = detailSheet.createRow(detailRowIdx++)
                    tokens.forEachIndexed { colIdx, token ->
                        val cell = row.createCell(colIdx)
                        if (detailRowIdx == 1) {
                            cell.setCellValue(token)
                            cell.cellStyle = headerStyle
                        } else {
                            if (colIdx == 4) {
                                val score = token.toDoubleOrNull()
                                if (score != null) cell.setCellValue(score) else cell.setCellValue(token)
                            } else {
                                cell.setCellValue(token)
                            }
                        }
                    }
                }
            }

            // Auto-size columns
            for (i in 0..5) {
                summarySheet.autoSizeColumn(i)
                detailSheet.autoSizeColumn(i)
            }

            val excelFile = File("translation_evaluation_chrf2.xlsx")
            FileOutputStream(excelFile).use { fos ->
                workbook.write(fos)
            }
            workbook.close()

            println("Formatted Excel spreadsheet successfully generated at: ${excelFile.absolutePath}")
        }

        private fun parseCsvLine(line: String): List<String> {
            val result = mutableListOf<String>()
            var inQuotes = false
            val sb = StringBuilder()
            var i = 0
            while (i < line.length) {
                val c = line[i]
                when {
                    c == '"' -> {
                        if (i + 1 < line.length && line[i + 1] == '"') {
                            sb.append('"')
                            i++
                        } else {
                            inQuotes = !inQuotes
                        }
                    }
                    c == ',' && !inQuotes -> {
                        result.add(sb.toString())
                        sb.setLength(0)
                    }
                    else -> {
                        sb.append(c)
                    }
                }
                i++
            }
            result.add(sb.toString())
            return result
        }
    }
}
