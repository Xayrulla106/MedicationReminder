package com.example.medicationreminder.domain.repository

import com.example.medicationreminder.domain.model.IntakeAction
import com.example.medicationreminder.domain.model.IntakeLog
import com.example.medicationreminder.domain.model.Medication
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MedicationRepository {

    /** Emits the full medication list whenever it changes. */
    fun getMedications(): Flow<List<Medication>>

    suspend fun getMedication(id: Long): Medication?

    /** Inserts the seed schedule the first time the app runs. */
    suspend fun seedIfEmpty()

    /** All intake logs for a single calendar day. */
    fun getLogsForDate(date: LocalDate): Flow<List<IntakeLog>>

    /** All intake logs between two dates (inclusive), newest first. */
    fun getLogsBetween(from: LocalDate, to: LocalDate): Flow<List<IntakeLog>>

    suspend fun getLog(medicationId: Long, date: LocalDate): IntakeLog?

    /** Records (upserts) a dose action for a (medication, day). */
    suspend fun recordIntake(
        medicationId: Long,
        date: LocalDate,
        scheduledTime: Long,
        action: IntakeAction,
        snoozeIncrement: Boolean = false
    )

    /** Program start date (initialised on first launch). */
    suspend fun ensureTreatmentStart(): LocalDate

    fun observeTreatmentDurationDays(): Flow<Int>
}
