package com.example.medicationreminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single medication (or clinical session) that must be taken on a schedule.
 *
 * The active treatment window is derived from [startDay] and [durationDays]:
 *   active from treatment-day [startDay] through treatment-day [startDay]+[durationDays]-1.
 * A null [durationDays] means "ongoing" (active until the end of the treatment program).
 */
@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    /** e.g. "50 mg" */
    val dosage: String,
    /** e.g. "1 tab", "1/2 tab", "2 IV + 2 IM" */
    val quantity: String,

    /** Local time-of-day the reminder fires. */
    val hour: Int,
    val minute: Int,

    /** Free-text intake instruction shown to the user. */
    val instructions: String,

    /** "TABLET" or "IV_SESSION" – drives UI grouping/iconography. */
    val category: String,

    /** Ordered sub-components (empty for plain tablets, 4 items for the IV session). */
    val components: List<String>,

    /** 1-based treatment day on which this becomes active. */
    val startDay: Int,

    /** Number of active days; null = ongoing for the whole program. */
    val durationDays: Int?,

    val isEnabled: Boolean = true,

    val createdAt: Long = System.currentTimeMillis()
)
