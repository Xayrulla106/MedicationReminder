package com.example.medicationreminder.domain.usecase

import com.example.medicationreminder.domain.model.HistoryDay
import com.example.medicationreminder.domain.model.MedicationItem
import com.example.medicationreminder.domain.model.MedicationUiStatus
import com.example.medicationreminder.domain.repository.MedicationRepository
import com.example.medicationreminder.domain.util.isActiveOnDay
import com.example.medicationreminder.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Builds a per-day adherence history for the last [days] days, returning the
 * most recent days first. Days with no active medications are omitted.
 */
class GetHistoryUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    operator fun invoke(days: Int = 30): Flow<List<HistoryDay>> = flow {
        val today = DateUtils.today
        val treatmentStart = repository.ensureTreatmentStart()
        val from = today.minusDays((days - 1).toLong())

        repository.getLogsBetween(from, today).collect { logs ->
            val meds = repository.getMedications().first()

            val byDate = logs.groupBy { it.scheduledDate }
            val result = byDate.mapNotNull { (date, dayLogs) ->
                val day = DateUtils.treatmentDay(treatmentStart, date)
                val items = dayLogs.mapNotNull { log ->
                    val med = meds.find { m -> m.id == log.medicationId } ?: return@mapNotNull null
                    if (!med.isActiveOnDay(day)) return@mapNotNull null
                    MedicationItem(
                        medication = med,
                        scheduledTime = log.scheduledTime,
                        status = when (log.action) {
                            com.example.medicationreminder.domain.model.IntakeAction.TAKEN -> MedicationUiStatus.TAKEN
                            com.example.medicationreminder.domain.model.IntakeAction.SKIPPED -> MedicationUiStatus.SKIPPED
                            else -> MedicationUiStatus.PENDING
                        },
                        snoozeCount = log.snoozeCount
                    )
                }
                if (items.isEmpty()) null
                else HistoryDay(
                    date = date,
                    items = items.sortedBy { it.scheduledTime },
                    taken = items.count { it.status == MedicationUiStatus.TAKEN },
                    total = items.size
                )
            }.sortedByDescending { it.date }

            emit(result)
        }
    }
}
