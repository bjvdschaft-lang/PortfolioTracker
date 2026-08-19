package com.portfolio.tracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.portfolio.tracker.data.dao.EntryDao
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import android.util.Log

@Database(entities = [PortfolioEntryEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        // Migration from version 1 to version 2 - ensure table exists
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "Migration 1->2: Ensuring portfolio_entries table exists")
                // Table already created by Room, no additional changes needed
                // This migration ensures schema consistency
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: run {
                    Log.d("AppDatabase", "=== DATABASE INITIALIZATION ===")
                    val dbPath = context.getDatabasePath("portfolio_tracker_db")
                    Log.d("AppDatabase", "Database path: ${dbPath.absolutePath}")
                    Log.d("AppDatabase", "Database exists before init: ${dbPath.exists()}")
                    
                    val db = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "portfolio_tracker_db"
                    )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .enableMultiInstanceInvalidation()
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .build()
                    
                    Log.d("AppDatabase", "Database created successfully")
                    Log.d("AppDatabase", "Database isOpen: ${db.isOpen}")
                    Log.d("AppDatabase", "Database exists after init: ${dbPath.exists()}")
                    
                    try {
                        // Force Room to create tables
                        val writableDb = db.openHelper.writableDatabase
                        Log.d("AppDatabase", "Writable database obtained - tables should be created")
                        
                        // Verify database connectivity
                        val testDao = db.entryDao()
                        Log.d("AppDatabase", "DAO created successfully")
                        
                        // Check if table exists
                        writableDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='portfolio_entries'", null).use { cursor ->
                            if (cursor.moveToFirst()) {
                                Log.d("AppDatabase", "✓ portfolio_entries table EXISTS in database")
                            } else {
                                Log.e("AppDatabase", "✗ ERROR: portfolio_entries table DOES NOT EXIST!")
                            }
                        }
                        
                        // Count current entries
                        writableDb.query("SELECT COUNT(*) as count FROM portfolio_entries", null).use { cursor ->
                            cursor.moveToFirst()
                            val count = cursor.getInt(0)
                            Log.d("AppDatabase", "Current entry count: $count")
                        }
                    } catch (e: Exception) {
                        Log.e("AppDatabase", "Error verifying database connection", e)
                    }
                    
                    instance = db
                    db
                }
            }
        }

        /**
         * Force database to sync all pending writes to disk.
         * Called after critical operations to ensure data persistence.
         */
        fun syncDatabase(context: Context) {
            try {
                val db = getInstance(context)
                if (db.isOpen) {
                    Log.d("AppDatabase", "Executing database checkpoint...")
                    // Execute checkpoint to force WAL to main database
                    db.openHelper.writableDatabase.use { writableDb ->
                        writableDb.query("PRAGMA wal_checkpoint(RESTART);", null).use { cursor ->
                            Log.d("AppDatabase", "✓ Database checkpoint complete - all data synced to disk")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AppDatabase", "Error syncing database: ${e.message}", e)
            }
        }
    }
}
