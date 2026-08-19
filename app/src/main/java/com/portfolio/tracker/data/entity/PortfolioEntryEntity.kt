package com.portfolio.tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_entries")
data class PortfolioEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,  // This MUST stay 0 - Room auto-generates it on insert
    val entryId: String,  // Your custom UUID string
    val dateTime: String,
    val type: String,
    val category: String,
    val description: String,
    val amount: Double,
    val currency: String,
    val convertedAmount: Double
)
