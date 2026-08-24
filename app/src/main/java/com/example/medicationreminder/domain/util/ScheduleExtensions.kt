package com.example.medicationreminder.domain.util

import com.example.medicationreminder.domain.model.IntakeAction
import com.example.medicationreminder.domain.model.IntakeLog
import com.example.medicationreminder.domain.model.Medication
import com.example.medicationreminder.domain.model.MedicationUiStatus
import com.example.medicationreminder.util.DateUtils

/** True if [med] is part of the active program on the given 1-based treatment day. */
fun Medication.isActiveOnDay(treatmentDay: Int): Boolean =
    treatmentDay in startDay..endDay

/**
 * Derives the dashboard chip status for a medication on a given day.
 *
 * @param now epoch millis "now" used to distinguish UPCOMING vs PENDING.
 * @param scheduledTime epoch millis of today's scheduled fire time.
 * @param log the stored intake record for this (med, day), if any.
 */
fun Medication.computeUiStatus(
    now: Long,
    scheduledTime: Long,
    log: IntakeLog?
): MedicationUiStatus = when {
    log?.action == IntakeAction.TAKEN -> MedicationUiStatus.TAKEN
    log?.action == IntakeAction.SKIPPED -> MedicationUiStatus.SKIPPED
    now < scheduledTime -> MedicationUiStatus.UPCOMING
    else -> MedicationUiStatus.PENDING
}

/** Convenience wrapper around [DateUtils.treatmentDay]. */
fun currentTreatmentDay(treatmentStart: java.time.LocalDate): Int =
    DateUtils.treatmentDay(treatmentStart)
