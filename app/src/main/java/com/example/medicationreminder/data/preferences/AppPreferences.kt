package com.example.medicationreminder.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.medicationreminder.util.Constants.DATE_PATTERN
import com.example.medicationreminder.util.Constants.DEFAULT_TREATMENT_DURATION_DAYS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private val TREATMENT_START = stringPreferencesKey("treatment_start_date")
private val TREATMENT_DURATION = intPreferencesKey("treatment_duration_days")

/**
 * Small wrapper over DataStore holding program-level settings that drive
 * scheduling and the "Day X of N" label.
 */
@Singleton
class AppPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val fmt = DateTimeFormatter.ofPattern(DATE_PATTERN)

    val treatmentStart: Flow<LocalDate?> = dataStore.data.map { prefs ->
        prefs[TREATMENT_START]?.let { LocalDate.parse(it, fmt) }
    }

    val treatmentDurationDays: Flow<Int> = dataStore.data.map { prefs ->
        prefs[TREATMENT_DURATION] ?: DEFAULT_TREATMENT_DURATION_DAYS
    }

    /** Returns the stored start date, initialising it to [default] on first access. */
    suspend fun ensureTreatmentStart(default: LocalDate = LocalDate.now()): LocalDate {
        val current = dataStore.data.first()[TREATMENT_START]
        val date = current?.let { LocalDate.parse(it, fmt) } ?: default
        if (current == null) {
            dataStore.edit { it[TREATMENT_START] = date.format(fmt) }
        }
        return date
    }

    suspend fun setTreatmentStart(date: LocalDate) {
        dataStore.edit { it[TREATMENT_START] = date.format(fmt) }
    }

    suspend fun setTreatmentDurationDays(days: Int) {
        dataStore.edit { it[TREATMENT_DURATION] = days }
    }
}
