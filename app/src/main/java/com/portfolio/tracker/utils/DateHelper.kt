package com.portfolio.tracker.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateHelper {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun now(): String = LocalDateTime.now().format(formatter)

    fun format(dateTime: LocalDateTime): String = dateTime.format(formatter)

    fun parse(value: String): LocalDateTime? = runCatching {
        LocalDateTime.parse(value, formatter)
    }.getOrNull()

    fun displayDate(value: String): String = value.take(10)
}
