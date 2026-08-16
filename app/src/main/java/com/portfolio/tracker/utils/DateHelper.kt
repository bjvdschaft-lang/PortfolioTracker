package com.portfolio.tracker.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateHelper {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy")
    private val monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    fun getCurrentDateTime(): String {
        return LocalDateTime.now().format(dateTimeFormatter)
    }

    fun getCurrentDate(): String {
        return LocalDate.now().format(dateFormatter)
    }

    fun formatDateForDisplay(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, dateFormatter)
            date.format(displayFormatter)
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatDateForChart(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, dateFormatter)
            date.format(monthFormatter)
        } catch (e: Exception) {
            dateString
        }
    }

    fun getDaysDifference(startDate: String, endDate: String): Long {
        return try {
            val start = LocalDate.parse(startDate, dateFormatter)
            val end = LocalDate.parse(endDate, dateFormatter)
            ChronoUnit.DAYS.between(start, end)
        } catch (e: Exception) {
            0L
        }
    }

    fun getDateRangeLabel(days: Long): String {
        return when (days) {
            1L -> "1D"
            7L -> "1W"
            30L -> "1M"
            90L -> "3M"
            180L -> "6M"
            270L -> "9M"
            365L -> "1Y"
            730L -> "2Y"
            else -> "${days}D"
        }
    }

    fun getDateRangeStartDate(daysBack: Long): String {
        return LocalDate.now().minusDays(daysBack).format(dateFormatter)
    }

    fun getDateRangeEndDate(): String {
        return LocalDate.now().format(dateFormatter)
    }

    fun getPeriods(): List<Pair<String, Long>> {
        return listOf(
            "1D" to 1L,
            "1W" to 7L,
            "1M" to 30L,
            "3M" to 90L,
            "6M" to 180L,
            "9M" to 270L,
            "1Y" to 365L,
            "2Y" to 730L
        )
    }
}