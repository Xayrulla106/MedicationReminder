package com.example.medicationreminder.data.local

import androidx.room.TypeConverter
import com.example.medicationreminder.util.Constants.DATE_PATTERN
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Room type converters for the non-primitive columns we persist:
 *  - [List<String>] (medication components) stored as a CSV string.
 *  - [LocalDate]    (scheduled dates)        stored as an ISO yyyy-MM-dd string.
 */
object Converters {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)

    @TypeConverter
    @JvmStatic
    fun fromStringList(list: List<String>?): String? =
        list?.joinToString(separator = ",")

    @TypeConverter
    @JvmStatic
    fun toStringList(csv: String?): List<String> =
        csv?.takeIf { it.isNotBlank() }?.split(",")?.map { it.trim() } ?: emptyList()

    @TypeConverter
    @JvmStatic
    fun fromLocalDate(date: LocalDate?): String? =
        date?.format(dateFormatter)

    @TypeConverter
    @JvmStatic
    fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it, dateFormatter) }
}
