package com.portfolio.tracker.data.dao

import androidx.room.*
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import kotlinx.coroutines.flow.Flow
import android.util.Log

@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: PortfolioEntryEntity) {
        Log.d("EntryDao", "insertEntry called: $entry")
    }

    @Update
    suspend fun updateEntry(entry: PortfolioEntryEntity) {
        Log.d("EntryDao", "updateEntry called: $entry")
    }

    @Delete
    suspend fun deleteEntry(entry: PortfolioEntryEntity) {
        Log.d("EntryDao", "deleteEntry called: $entry")
    }

    @Query("SELECT * FROM portfolio_entries ORDER BY dateTime DESC")
    fun getAllEntries(): Flow<List<PortfolioEntryEntity>>

    @Query("SELECT * FROM portfolio_entries WHERE entryId = :id")
    suspend fun getEntryById(id: String): PortfolioEntryEntity?

    @Query("DELETE FROM portfolio_entries")
    suspend fun clearAll() {
        Log.d("EntryDao", "clearAll called")
    }
}
