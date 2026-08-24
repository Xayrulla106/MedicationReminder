package com.example.medicationreminder.domain.model

/** High-level category used to choose icons / grouping in the UI. */
enum class MedicationCategory {
    TABLET,
    IV_SESSION
}

/**
 * Domain representation of a medication schedule. Pure Kotlin – no Android deps.
 */
data class Medication(
    val id: Long,
    val name: String,
    val dosage: String,
    val quantity: String,
    val hour: Int,
    val minute: Int,
    val instructions: String,
    val category: MedicationCategory,
    val components: List<String>,
    /** 1-based treatment day on which this becomes active. */
    val startDay: Int,
    /** Active days after [startDay]; null = ongoing for the whole program. */
    val durationDays: Int?,
    val isEnabled: Boolean
) {
    /** Inclusive last treatment day this medication is active, or [Int.MAX_VALUE] if ongoing. */
    val endDay: Int
        get() = if (durationDays != null) startDay + durationDays - 1 else Int.MAX_VALUE

    /** "10:00" style label in 24h form. */
    val timeLabel: String
        get() = "%02d:%02d".format(hour, minute)
}
