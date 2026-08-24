package com.example.medicationreminder.domain.usecase

import com.example.medicationreminder.domain.model.DashboardState
import com.example.medicationreminder.domain.model.MedicationItem
import com.example.medicationreminder.domain.model.MedicationUiStatus
import com.example.medicationreminder.domain.repository.MedicationRepository
import com.example.medicationreminder.domain.util.computeUiStatus
import com.example.medicationreminder.domain.util.isActiveOnDay
import com.example.medicationreminder.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Builds the full "Today" dashboard snapshot: treatment day, the list of
 * medications active today with their per-day status, and completion counts.
 */
class GetDashboardUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    operator fun invoke(): Flow<DashboardState> = flow {
        val today = DateUtils.today
        val treatmentStart = repository.ensureTreatmentStart()
        val day = DateUtils.treatmentDay(treatmentStart, today)

        combine(
            repository.getMedications(),
            repository.getLogsForDate(today),
            repository.observeTreatmentDurationDays()
        ) { meds, logs, duration ->
            val items = meds
                .filter { it.isEnabled && it.isActiveOnDay(day) }
                .map { med ->
                    val scheduledTime = today.toEpochMillisAt(med.hour, med.minute)
                    val log = logs.find { it.medicationId == med.id }
                    val status = med.computeUiStatus(
                        now = System.currentTimeMillis(),
                        scheduledTime = scheduledTime,
                        log = log
                    )
                    MedicationItem(
                        medication = med,
                        scheduledTime = scheduledTime,
                        status = status,
                        snoozeCount = log?.snoozeCount ?: 0
                    )
                }
                .sortedBy { it.scheduledTime }

            val taken = items.count { it.status == MedicationUiStatus.TAKEN }
            DashboardState(
                treatmentDay = day,
                treatmentDurationDays = duration,
                items = items,
                takenCount = taken,
                totalCount = items.size
            )
        }.collect { emit(it) }
    }
}
