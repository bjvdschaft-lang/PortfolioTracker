package com.portfolio.tracker.data.repository

import com.portfolio.tracker.data.dao.EntryDao
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import kotlinx.coroutines.flow.Flow

class EntryRepository(private val entryDao: EntryDao) {
    fun getAllEntries(): Flow<List<PortfolioEntryEntity>> = entryDao.getAllEntries()

    suspend fun insertEntry(entry: PortfolioEntryEntity) = entryDao.insertEntry(entry)

    suspend fun updateEntry(entry: PortfolioEntryEntity) = entryDao.updateEntry(entry)

    suspend fun deleteEntry(entry: PortfolioEntryEntity) = entryDao.deleteEntry(entry)

    suspend fun getEntryById(id: String) = entryDao.getEntryById(id)

    suspend fun clearAll() = entryDao.clearAll()
}
