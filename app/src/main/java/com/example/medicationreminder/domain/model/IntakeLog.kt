package com.example.medicationreminder.domain.model

import java.time.LocalDate

/**
 * What the user did with a single scheduled dose.
 */
enum class IntakeAction {
    TAKEN,
    SKIPPED,
    SNOOZED,
    MISSED
}

/**
 * Domain representation of an intake record for a (medication, day) pair.
 */
data class IntakeLog(
    val id: Long,
    val medicationId: Long,
    val scheduledDate: LocalDate,
    val scheduledTime: Long,
    val action: IntakeAction,
    val actionTime: Long?,
    val snoozeCount: Int,
    val note: String?
)
