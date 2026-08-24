package com.example.medicationreminder.domain.model

import java.time.LocalDate

/**
 * A single row on the "Today" timeline: the medication plus its computed
 * status for the current day.
 */
data class MedicationItem(
    val medication: Medication,
    /** Epoch millis of today's scheduled fire time. */
    val scheduledTime: Long,
    val status: MedicationUiStatus,
    val snoozeCount: Int
)

/**
 * Immutable snapshot the Dashboard UI renders. Emitted by [com.example.medicationreminder.domain.usecase.GetDashboardUseCase].
 */
data class DashboardState(
    /** Current treatment day (1-based). */
    val treatmentDay: Int,
    /** Total program length in days (for "Day X of N"). */
    val treatmentDurationDays: Int,
    val items: List<MedicationItem>,
    val takenCount: Int,
    val totalCount: Int
) {
    val progress: Float
        get() = if (totalCount == 0) 0f else takenCount.toFloat() / totalCount

    /** "Day 3 of 30" */
    val dayLabel: String
        get() = "Day $treatmentDay of $treatmentDurationDays"
}

/**
 * History aggregation per calendar day.
 */
data class HistoryDay(
    val date: LocalDate,
    val items: List<MedicationItem>,
    val taken: Int,
    val total: Int
) {
    val adherenceRate: Float
        get() = if (total == 0) 0f else taken.toFloat() / total
}
