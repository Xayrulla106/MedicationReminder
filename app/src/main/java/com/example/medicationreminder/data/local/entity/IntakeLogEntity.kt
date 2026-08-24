package com.example.medicationreminder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One record per (medication, scheduled day) capturing what the user did.
 *
 * [status] is one of: TAKEN, SKIPPED, SNOOZED, MISSED (see [IntakeAction]).
 * A [scheduledDate] + [medicationId] pair is unique, so re-logging upserts.
 */
@Entity(
    tableName = "intake_logs",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicationId"), Index("scheduledDate")]
)
data class IntakeLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val medicationId: Long,
    /** yyyy-MM-dd of the scheduled day (local). */
    val scheduledDate: String,
    /** Epoch millis of the scheduled fire time. */
    val scheduledTime: Long,

    val status: String,
    /** Epoch millis of when the user acted, or null if not yet. */
    val actionTime: Long? = null,
    /** How many times this dose was snoozed. */
    val snoozeCount: Int = 0,
    /** Optional free-text note. */
    val note: String? = null
)
