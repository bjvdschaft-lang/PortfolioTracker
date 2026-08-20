package com.portfolio.tracker.data.repository

import com.portfolio.tracker.data.dao.EntryDao
import com.portfolio.tracker.data.database.AppDatabase
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import android.util.Log
import android.content.Context

class EntryRepository(private val entryDao: EntryDao, private val context: Context? = null) {
    private val TAG = "EntryRepository"

    fun getAllEntries(): Flow<List<PortfolioEntryEntity>> {
        Log.d(TAG, "getAllEntries() called - setting up flow collector")
        return entryDao.getAllEntries()
            .onStart { Log.d(TAG, "Flow subscribed - querying database for entries") }
            .onEach { entries -> Log.d(TAG, "Flow emitted ${entries.size} entries from database") }
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
            } else {
                // Verify insertion by checking total count
                val countAfter = entryDao.getEntryCount()
                Log.d(TAG, "✓ Database now contains $countAfter total entries")
                
                if (countAfter > 0) {
                    // Force sync to disk after successful insert
                    Log.d(TAG, "Syncing database to disk...")
                    context?.let { AppDatabase.syncDatabase(it) }
                    Log.d(TAG, "✓ Database synced - data is now persistent")
                } else {
                    Log.e(TAG, "✗ ERROR: Entry count is 0 after insert!")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error inserting entry: ${e.message}", e)
            e.printStackTrace()
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
                insertEntry(entry)
            } else {
                Log.d(TAG, "✓ Update successful, $rowsUpdated row(s) modified")
                
                // Verify update by checking total count
                val countAfter = entryDao.getEntryCount()
                Log.d(TAG, "✓ Database contains $countAfter total entries after update")
                
                // Force sync to disk after update
                Log.d(TAG, "Syncing database to disk...")
                context?.let { AppDatabase.syncDatabase(it) }
                Log.d(TAG, "✓ Database synced - data is now persistent")
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error updating entry: ${e.message}", e)
            e.printStackTrace()
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
                
                // Verify deletion by checking total count
                val countAfter = entryDao.getEntryCount()
                Log.d(TAG, "✓ Database now contains $countAfter total entries after delete")
                
                // Force sync to disk after delete
                Log.d(TAG, "Syncing database to disk...")
                context?.let { AppDatabase.syncDatabase(it) }
                Log.d(TAG, "✓ Database synced - data is now persistent")
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error deleting entry: ${e.message}", e)
            e.printStackTrace()
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
            e.printStackTrace()
            throw e
        }
    }

    suspend fun clearAll() {
        Log.d(TAG, "=== CLEAR ALL ENTRIES ===")
        Log.w(TAG, "⚠ WARNING: About to delete ALL entries from database!")
        
        try {
            val rowsDeleted = entryDao.clearAll()
            Log.d(TAG, "✓ All entries cleared successfully ($rowsDeleted rows deleted)")
            
            // Verify clear by checking total count
            val countAfter = entryDao.getEntryCount()
            Log.d(TAG, "✓ Database now contains $countAfter total entries (should be 0)")
            
            if (countAfter == 0) {
                Log.d(TAG, "✓ Clear successful - database is empty")
            } else {
                Log.e(TAG, "✗ ERROR: Database still contains entries after clear!")
            }
            
            // Force sync to disk after clear
            Log.d(TAG, "Syncing database to disk...")
            context?.let { AppDatabase.syncDatabase(it) }
            Log.d(TAG, "✓ Database synced - data is now persistent")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error clearing all entries: ${e.message}", e)
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getEntryCount(): Int {
        return try {
            val count = entryDao.getEntryCount()
            Log.d(TAG, "Database entry count: $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting entry count: ${e.message}", e)
            0
        }
    }
}
