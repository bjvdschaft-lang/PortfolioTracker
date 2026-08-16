package com.portfolio.tracker

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class PortfolioEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val dateTime: String = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
    val type: String,
    val category: String,
    val description: String,
    val amount: Double,
    val currency: String,
    val convertedAmount: Double
)

object EntryStore {
    private val _entries: SnapshotStateList<PortfolioEntry> = mutableStateListOf()

    val entries: SnapshotStateList<PortfolioEntry>
        get() = _entries

    fun addEntry(entry: PortfolioEntry) {
        _entries.add(0, entry) // Add to beginning for newest first
    }

    fun deleteEntry(id: String) {
        _entries.removeAll { it.id == id }
    }

    fun getAllEntries(): List<PortfolioEntry> {
        return _entries.toList()
    }

    fun getTotalAssets(): Double {
        return _entries
            .filter { it.type == "Assets" }
            .sumOf { it.convertedAmount }
    }

    fun getTotalLiabilities(): Double {
        return _entries
            .filter { it.type == "Liabilities" }
            .sumOf { it.convertedAmount }
    }

    fun getNetWorth(): Double {
        return getTotalAssets() - getTotalLiabilities()
    }

    fun clearAll() {
        _entries.clear()
    }
}