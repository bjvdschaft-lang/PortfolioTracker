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
                    Log.d("AppDatabase", "Creating new database instance")
                    val db = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "portfolio_tracker_db"
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    Log.d("AppDatabase", "Database created: ${db.openHelper.writableDatabase.path}")
                    instance = db
                    db
                }
            }
        }
    }
}
