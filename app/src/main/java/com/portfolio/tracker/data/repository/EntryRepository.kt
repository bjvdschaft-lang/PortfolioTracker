package com.portfolio.tracker.data.repository

import com.portfolio.tracker.data.entity.PortfolioEntryEntity

class EntryRepository {
    private val _entries = mutableListOf<PortfolioEntryEntity>()

    fun getAllEntries(): List<PortfolioEntryEntity> = _entries.toList()

    suspend fun insertEntry(entry: PortfolioEntryEntity) {
        val newId = if (_entries.isEmpty()) 1 else _entries.maxOf { it.id } + 1
        _entries.add(entry.copy(id = newId))
    }

    suspend fun updateEntry(entry: PortfolioEntryEntity) {
        val index = _entries.indexOfFirst { it.id == entry.id }
        if (index >= 0) _entries[index] = entry
    }

    suspend fun deleteEntry(entry: PortfolioEntryEntity) {
        _entries.removeAll { it.id == entry.id }
    }
}
