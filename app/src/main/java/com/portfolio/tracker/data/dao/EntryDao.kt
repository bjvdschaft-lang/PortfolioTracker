package com.portfolio.tracker.data.dao

import androidx.room.*
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import kotlinx.coroutines.flow.Flow
import android.util.Log

@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: PortfolioEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: PortfolioEntryEntity): Int

    @Delete
    suspend fun deleteEntry(entry: PortfolioEntryEntity): Int

    @Query("SELECT * FROM portfolio_entries ORDER BY dateTime DESC")
    fun getAllEntries(): Flow<List<PortfolioEntryEntity>>

    @Query("SELECT * FROM portfolio_entries WHERE entryId = :id")
    suspend fun getEntryById(id: String): PortfolioEntryEntity?

    @Query("DELETE FROM portfolio_entries")
    suspend fun clearAll(): Int

    // Query to verify table exists and get entry count
    @Query("SELECT COUNT(*) FROM portfolio_entries")
    suspend fun getEntryCount(): Int

    // Transaction for batch operations
    @Transaction
    suspend fun insertEntries(entries: List<PortfolioEntryEntity>) {
        entries.forEach { insertEntry(it) }
    }
}
