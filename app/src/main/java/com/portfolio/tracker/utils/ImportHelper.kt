package com.portfolio.tracker.utils

import com.portfolio.tracker.data.entity.PortfolioEntryEntity

object ImportHelper {

    /**
     * Parses a CSV string into a list of [PortfolioEntryEntity].
     *
     * Expected column order (header row optional):
     * type, category, description, amount, currency, convertedAmount, dateTime
     */
    fun parseCsv(csv: String): List<PortfolioEntryEntity> {
        val lines = csv.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        // Skip header row if present
        val dataLines = if (lines.firstOrNull()?.startsWith("type", ignoreCase = true) == true) {
            lines.drop(1)
        } else {
            lines
        }

        return dataLines.mapNotNull { line ->
            val cols = line.split(",").map { it.trim() }
            if (cols.size < 7) return@mapNotNull null
            runCatching {
                PortfolioEntryEntity(
                    id = 0,
                    type = cols[0],
                    category = cols[1],
                    description = cols[2],
                    amount = cols[3].toDouble(),
                    currency = cols[4],
                    convertedAmount = cols[5].toDouble(),
                    dateTime = cols[6]
                )
            }.getOrNull()
        }
    }
}
