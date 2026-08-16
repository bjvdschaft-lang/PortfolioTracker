package com.portfolio.tracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.portfolio.tracker.data.dao.EntryDao
import com.portfolio.tracker.data.entity.PortfolioEntryEntity

@Database(entities = [PortfolioEntryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "portfolio_tracker_db"
                ).build().also { instance = it }
            }
        }
    }
}
