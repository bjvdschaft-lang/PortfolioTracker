package com.portfolio.tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_entries")
data class PortfolioEntryEntity(
    @PrimaryKey
    val entryId: String,
    val dateTime: String,
    val type: String,
    val category: String,
    val description: String,
    val amount: Double,
    val currency: String,
    val convertedAmount: Double
)
