package com.portfolio.tracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import com.portfolio.tracker.data.repository.EntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Lightweight state holder for portfolio entries.
 * Not a full AndroidViewModel — kept simple to avoid lifecycle imports.
 */
class PortfolioViewModel(private val repository: EntryRepository) {

    var entries: List<PortfolioEntryEntity> by mutableStateOf(repository.getAllEntries())
        private set

    fun refresh() {
        entries = repository.getAllEntries()
    }

    fun addEntry(scope: CoroutineScope, entry: PortfolioEntryEntity, onDone: () -> Unit = {}) {
        scope.launch {
            repository.insertEntry(entry)
            refresh()
            onDone()
        }
    }

    fun updateEntry(scope: CoroutineScope, entry: PortfolioEntryEntity, onDone: () -> Unit = {}) {
        scope.launch {
            repository.updateEntry(entry)
            refresh()
            onDone()
        }
    }

    fun deleteEntry(scope: CoroutineScope, entry: PortfolioEntryEntity, onDone: () -> Unit = {}) {
        scope.launch {
            repository.deleteEntry(entry)
            refresh()
            onDone()
        }
    }
}
