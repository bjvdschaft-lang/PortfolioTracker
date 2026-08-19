package com.portfolio.tracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.portfolio.tracker.data.dao.EntryDao
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import android.util.Log
import java.io.File

@Database(entities = [PortfolioEntryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: run {
                    Log.d("AppDatabase", "=== DATABASE INITIALIZATION ===")
                    val dbPath = context.getDatabasePath("portfolio_tracker_db")
                    Log.d("AppDatabase", "Database path: ${dbPath.absolutePath}")
                    Log.d("AppDatabase", "Database exists: ${dbPath.exists()}")
                    
                    val db = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "portfolio_tracker_db"
                    )
                    .fallbackToDestructiveMigration()
                    .enableMultiInstanceInvalidation()
                    .build()
                    
                    Log.d("AppDatabase", "Database created successfully")
                    Log.d("AppDatabase", "Database isOpen: ${db.isOpen}")
                    
                    try {
                        // Verify database connectivity
                        val testDao = db.entryDao()
                        Log.d("AppDatabase", "DAO created successfully")
                        
                        // Force a dummy query to verify database works
                        db.query("SELECT COUNT(*) as count FROM portfolio_entries", null).use { cursor ->
                            cursor.moveToFirst()
                            val count = cursor.getInt(0)
                            Log.d("AppDatabase", "Database query test successful. Current entry count: $count")
                        }
                    } catch (e: Exception) {
                        Log.e("AppDatabase", "Error verifying database connection", e)
                    }
                    
                    instance = db
                    db
                }
            }
        }
    }
}
