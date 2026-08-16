package com.portfolio.tracker.utils

import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import com.portfolio.tracker.data.repository.EntryRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ImportHelper {
    fun parseCsvLine(line: String): PortfolioEntryEntity? {
        return try {
            val parts = line.split(",")
            if (parts.size < 8) return null

            val dateTime = parts[0].trim()
            val type = parts[1].trim()
            val category = parts[2].trim()
            val description = parts[3].trim()
            val amount = parts[4].trim().toDoubleOrNull() ?: return null
            val currency = parts[5].trim()
            val convertedAmount = parts[6].trim().toDoubleOrNull() ?: return null

            PortfolioEntryEntity(
                entryId = java.util.UUID.randomUUID().toString(),
                dateTime = dateTime,
                type = type,
                category = category,
                description = description,
                amount = amount,
                currency = currency,
                convertedAmount = convertedAmount
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun importCsvData(
        csvContent: String,
        repository: EntryRepository,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ) {
        val lines = csvContent.lines()
            .filter { it.isNotBlank() }
            .drop(1) // Skip header

        lines.forEachIndexed { index, line ->
            parseCsvLine(line)?.let { entry ->
                repository.insertEntry(entry)
                onProgress(index + 1, lines.size)
            }
        }
    }
}