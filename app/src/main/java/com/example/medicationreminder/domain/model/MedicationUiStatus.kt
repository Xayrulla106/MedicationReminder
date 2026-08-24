package com.example.medicationreminder.domain.model

/**
 * Status chips shown on the dashboard. Derived from the scheduled time and the
 * stored [IntakeLog] rather than persisted directly.
 */
enum class MedicationUiStatus {
    UPCOMING,  // time has not arrived yet
    PENDING,   // time arrived, no action taken
    TAKEN,
    SKIPPED
}
