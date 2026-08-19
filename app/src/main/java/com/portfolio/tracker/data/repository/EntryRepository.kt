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
            val rowId = entryDao.insertEntry(entry)
            Log.d(TAG, "✓ Entry inserted successfully into database (row ID: $rowId)")
            if (rowId < 0) {
                Log.e(TAG, "✗ Insert returned negative row ID: $rowId")
            }
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
            Log.d(TAG, "✓ Entry update attempted (rows affected: $rowsUpdated)")
            if (rowsUpdated == 0) {
                Log.w(TAG, "⚠ WARNING: Update affected 0 rows!")
                Log.w(TAG, "  - Entry may not exist in database")
                Log.w(TAG, "  - Check if entry was previously inserted")
                Log.w(TAG, "  - Attempting INSERT instead...")
                // Fallback to insert if update found nothing
                insertEntry(entry)
            } else {
                Log.d(TAG, "✓ Update successful, $rowsUpdated row(s) modified")
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
            Log.d(TAG, "✓ Entry delete attempted (rows affected: $rowsDeleted)")
            if (rowsDeleted == 0) {
                Log.w(TAG, "⚠ WARNING: Delete affected 0 rows!")
                Log.w(TAG, "  - Entry may not exist in database")
                Log.w(TAG, "  - Check if entry was previously inserted")
            } else {
                Log.d(TAG, "✓ Delete successful, $rowsDeleted row(s) removed")
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
            val rowsDeleted = entryDao.clearAll()
            Log.d(TAG, "✓ All entries cleared successfully ($rowsDeleted rows deleted)")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error clearing all entries: ${e.message}", e)
            throw e
        }
    }
}
