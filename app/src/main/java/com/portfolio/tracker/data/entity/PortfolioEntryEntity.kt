package com.portfolio.tracker.data.entity

// Plain data class - no Room/database annotations
// Database can be properly configured later
data class PortfolioEntryEntity(
    val id: Int = 0,
    val type: String,
    val category: String,
    val description: String,
    val amount: Double,
    val currency: String,
    val convertedAmount: String,
    val dateTime: String
)
