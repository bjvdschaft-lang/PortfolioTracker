package com.portfolio.tracker.data.repository

import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class EntryRepository {
    private val mutex = Mutex()
    private val _entries = mutableListOf<PortfolioEntryEntity>()

    fun getAllEntries(): List<PortfolioEntryEntity> = synchronized(_entries) { _entries.toList() }

    suspend fun insertEntry(entry: PortfolioEntryEntity) = mutex.withLock {
        val newId = if (_entries.isEmpty()) 1 else _entries.maxOf { it.id } + 1
        _entries.add(entry.copy(id = newId))
    }

    suspend fun updateEntry(entry: PortfolioEntryEntity) = mutex.withLock {
        val index = _entries.indexOfFirst { it.id == entry.id }
        if (index >= 0) _entries[index] = entry
    }

    suspend fun deleteEntry(entry: PortfolioEntryEntity) = mutex.withLock {
        _entries.removeAll { it.id == entry.id }
    }
}
