package com.example.medicationreminder.domain.usecase

import com.example.medicationreminder.domain.model.IntakeAction
import com.example.medicationreminder.domain.model.MedicationUiStatus
import com.example.medicationreminder.domain.repository.MedicationRepository
import com.example.medicationreminder.util.DateUtils
import java.time.LocalDate
import javax.inject.Inject

/**
 * Records a user action (taken / skipped / snoozed) for a dose and returns the
 * resulting [MedicationUiStatus] the UI should reflect.
 */
class MarkIntakeUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(
        medicationId: Long,
        date: LocalDate = DateUtils.today,
        scheduledTime: Long,
        action: IntakeAction
    ): MedicationUiStatus {
        repository.recordIntake(
            medicationId = medicationId,
            date = date,
            scheduledTime = scheduledTime,
            action = action,
            snoozeIncrement = action == IntakeAction.SNOOZED
        )
        return when (action) {
            IntakeAction.TAKEN -> MedicationUiStatus.TAKEN
            IntakeAction.SKIPPED -> MedicationUiStatus.SKIPPED
            else -> MedicationUiStatus.PENDING // SNOOZED / MISSED -> still pending
        }
    }
}
