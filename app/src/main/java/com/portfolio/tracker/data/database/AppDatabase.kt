package com.portfolio.tracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.portfolio.tracker.data.dao.EntryDao
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import android.util.Log

@Database(entities = [PortfolioEntryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: run {
                    Log.d("AppDatabase", "Creating new database instance with WAL enabled")
                    val db = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "portfolio_tracker_db"
                    )
                    .fallbackToDestructiveMigration()
                    .enableMultiInstanceInvalidation()
                    .build()
                    
                    // Enable WAL and ensure data is synced to disk
                    db.openHelper.writableDatabase.apply {
                        // Enable WAL (Write-Ahead Logging) for better reliability
                        enableWriteAheadLogging()
                        Log.d("AppDatabase", "Database path: ${path}")
                        Log.d("AppDatabase", "WAL enabled: ${isWriteAheadLoggingEnabled}")
                    }
                    
                    instance = db
                    db
                }
            }
        }
    }
}
