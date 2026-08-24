package com.example.medicationreminder.data.repository

import com.example.medicationreminder.data.SeedData
import com.example.medicationreminder.data.local.dao.IntakeLogDao
import com.example.medicationreminder.data.local.dao.MedicationDao
import com.example.medicationreminder.data.mapper.toDomain
import com.example.medicationreminder.data.mapper.toEntity
import com.example.medicationreminder.data.preferences.AppPreferences
import com.example.medicationreminder.domain.model.IntakeAction
import com.example.medicationreminder.domain.model.IntakeLog
import com.example.medicationreminder.domain.model.Medication
import com.example.medicationreminder.domain.repository.MedicationRepository
import com.example.medicationreminder.util.Constants.DATE_PATTERN
import com.example.medicationreminder.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepositoryImpl @Inject constructor(
    private val medicationDao: MedicationDao,
    private val intakeLogDao: IntakeLogDao,
    private val appPreferences: AppPreferences
) : MedicationRepository {

    private val fmt = DateTimeFormatter.ofPattern(DATE_PATTERN)

    override fun getMedications(): Flow<List<Medication>> =
        medicationDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getMedication(id: Long): Medication? =
        medicationDao.getById(id)?.toDomain()

    override suspend fun seedIfEmpty() {
        if (medicationDao.count() == 0) {
            medicationDao.insertAll(SeedData.MEDICATIONS)
        }
        appPreferences.ensureTreatmentStart()
    }

    override fun getLogsForDate(date: LocalDate): Flow<List<IntakeLog>> {
        val key = date.format(fmt)
        return intakeLogDao.observeForDate(key).map { it.map { e -> e.toDomain() } }
    }

    override fun getLogsBetween(from: LocalDate, to: LocalDate): Flow<List<IntakeLog>> {
        val fromKey = from.format(fmt)
        val toKey = to.format(fmt)
        return intakeLogDao.observeBetween(fromKey, toKey).map { it.map { e -> e.toDomain() } }
    }

    override suspend fun getLog(medicationId: Long, date: LocalDate): IntakeLog? =
        intakeLogDao.get(medicationId, date.format(fmt))?.toDomain()

    override suspend fun recordIntake(
        medicationId: Long,
        date: LocalDate,
        scheduledTime: Long,
        action: IntakeAction,
        snoozeIncrement: Boolean
    ) {
        val key = date.format(fmt)
        val existing = intakeLogDao.get(medicationId, key)
        val entity = if (existing != null) {
            existing.copy(
                status = action.name,
                actionTime = if (action == IntakeAction.SNOOZED) existing.actionTime else System.currentTimeMillis(),
                snoozeCount = if (snoozeIncrement) existing.snoozeCount + 1 else existing.snoozeCount
            )
        } else {
            IntakeLog(
                id = 0,
                medicationId = medicationId,
                scheduledDate = date,
                scheduledTime = scheduledTime,
                action = action,
                actionTime = if (action == IntakeAction.SNOOZED) null else System.currentTimeMillis(),
                snoozeCount = if (snoozeIncrement) 1 else 0,
                note = null
            ).toEntity()
        }
        intakeLogDao.upsert(entity)
    }

    override suspend fun ensureTreatmentStart(): LocalDate = appPreferences.ensureTreatmentStart()

    override fun observeTreatmentDurationDays(): Flow<Int> = appPreferences.treatmentDurationDays
}
