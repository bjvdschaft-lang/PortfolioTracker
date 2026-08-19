package com.portfolio.tracker.data.repository

import com.portfolio.tracker.data.dao.EntryDao
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import kotlinx.coroutines.flow.Flow
import android.util.Log

class EntryRepository(private val entryDao: EntryDao) {
    private val TAG = "EntryRepository"

    fun getAllEntries(): Flow<List<PortfolioEntryEntity>> {
        Log.d(TAG, "getAllEntries() called - setting up flow collector")
        return entryDao.getAllEntries().also { flow ->
            Log.d(TAG, "Flow created, will emit entries when database changes")
        }
    }

    suspend fun insertEntry(entry: PortfolioEntryEntity) {
        Log.d(TAG, "=== INSERT ENTRY ===")
        Log.d(TAG, "Entry ID: ${entry.entryId}")
        Log.d(TAG, "Description: ${entry.description}")
        Log.d(TAG, "Type: ${entry.type}")
        Log.d(TAG, "Amount: ${entry.amount} ${entry.currency}")
        Log.d(TAG, "Converted: €${entry.convertedAmount}")
        Log.d(TAG, "DateTime: ${entry.dateTime}")
        
        try {
            entryDao.insertEntry(entry)
            Log.d(TAG, "✓ Entry inserted successfully into database")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error inserting entry: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateEntry(entry: PortfolioEntryEntity) {
        Log.d(TAG, "=== UPDATE ENTRY ===")
        Log.d(TAG, "Entry ID: ${entry.entryId}")
        Log.d(TAG, "Description: ${entry.description}")
        Log.d(TAG, "Type: ${entry.type}")
        Log.d(TAG, "Amount: ${entry.amount} ${entry.currency}")
        Log.d(TAG, "Converted: €${entry.convertedAmount}")
        Log.d(TAG, "DateTime: ${entry.dateTime}")
        
        try {
            val rowsUpdated = entryDao.updateEntry(entry)
            Log.d(TAG, "✓ Entry updated successfully (rows affected: $rowsUpdated)")
            if (rowsUpdated == 0) {
                Log.w(TAG, "⚠ Warning: Update affected 0 rows. Entry may not exist in database.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error updating entry: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteEntry(entry: PortfolioEntryEntity) {
        Log.d(TAG, "=== DELETE ENTRY ===")
        Log.d(TAG, "Entry ID: ${entry.entryId}")
        Log.d(TAG, "Description: ${entry.description}")
        
        try {
            val rowsDeleted = entryDao.deleteEntry(entry)
            Log.d(TAG, "✓ Entry deleted successfully (rows affected: $rowsDeleted)")
            if (rowsDeleted == 0) {
                Log.w(TAG, "⚠ Warning: Delete affected 0 rows. Entry may not exist in database.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error deleting entry: ${e.message}", e)
            throw e
        }
    }

    suspend fun getEntryById(id: String) {
        Log.d(TAG, "=== GET ENTRY BY ID ===")
        Log.d(TAG, "Looking for entry ID: $id")
        
        try {
            val entry = entryDao.getEntryById(id)
            if (entry != null) {
                Log.d(TAG, "✓ Entry found: ${entry.description}")
            } else {
                Log.w(TAG, "⚠ Entry not found with ID: $id")
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error getting entry by ID: ${e.message}", e)
            throw e
        }
    }

    suspend fun clearAll() {
        Log.d(TAG, "=== CLEAR ALL ENTRIES ===")
        Log.w(TAG, "⚠ WARNING: About to delete ALL entries from database!")
        
        try {
            entryDao.clearAll()
            Log.d(TAG, "✓ All entries cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error clearing all entries: ${e.message}", e)
            throw e
        }
    }
}
