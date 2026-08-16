package com.portfolio.tracker.data.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PortfolioEntryEntity(
    val id: Int = 0,
    val type: String,
    val category: String,
    val description: String,
    val amount: Double,
    val currency: String,
    val convertedAmount: Double,
    val dateTime: String
) : Parcelable
