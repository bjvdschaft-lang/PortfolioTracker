package com.portfolio.tracker.data.dao

import androidx.room.*
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: PortfolioEntryEntity)

    @Update
    suspend fun updateEntry(entry: PortfolioEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: PortfolioEntryEntity)

    @Query("SELECT * FROM portfolio_entries ORDER BY dateTime DESC")
    fun getAllEntries(): Flow<List<PortfolioEntryEntity>>

    @Query("SELECT * FROM portfolio_entries WHERE entryId = :id")
    suspend fun getEntryById(id: String): PortfolioEntryEntity?

    @Query("DELETE FROM portfolio_entries")
    suspend fun clearAll()
}
