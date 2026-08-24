package com.example.medicationreminder.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {
    private val zone: ZoneId = ZoneId.systemDefault()
    val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern(Constants.DATE_PATTERN)

    val today: LocalDate get() = LocalDate.now()

    fun LocalDate.toEpochMillisAt(hour: Int, minute: Int): Long =
        this.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

    fun epochToLocalDateTime(epoch: Long): LocalDateTime =
        java.time.Instant.ofEpochMilli(epoch).atZone(zone).toLocalDateTime()

    fun epochToLocalDate(epoch: Long): LocalDate =
        java.time.Instant.ofEpochMilli(epoch).atZone(zone).toLocalDate()

    /** 1-based treatment day relative to [treatmentStart]. */
    fun treatmentDay(treatmentStart: LocalDate, date: LocalDate = today): Int =
        (ChronoUnit.DAYS.between(treatmentStart, date).toInt()) + 1

    fun formatDate(date: LocalDate): String = date.format(DATE_FMT)

    fun parseDate(value: String): LocalDate = LocalDate.parse(value, DATE_FMT)
}
