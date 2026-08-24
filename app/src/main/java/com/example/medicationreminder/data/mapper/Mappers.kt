package com.example.medicationreminder.data.mapper

import com.example.medicationreminder.data.local.entity.IntakeLogEntity
import com.example.medicationreminder.data.local.entity.MedicationEntity
import com.example.medicationreminder.domain.model.IntakeAction
import com.example.medicationreminder.domain.model.IntakeLog
import com.example.medicationreminder.domain.model.Medication
import com.example.medicationreminder.domain.model.MedicationCategory
import com.example.medicationreminder.util.Constants.DATE_PATTERN
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)

fun MedicationEntity.toDomain(): Medication = Medication(
    id = id,
    name = name,
    dosage = dosage,
    quantity = quantity,
    hour = hour,
    minute = minute,
    instructions = instructions,
    category = runCatching { MedicationCategory.valueOf(category) }
        .getOrDefault(MedicationCategory.TABLET),
    components = components,
    startDay = startDay,
    durationDays = durationDays,
    isEnabled = isEnabled
)

fun MedicationEntity.toDomainOrNull(): Medication? = runCatching { toDomain() }.getOrNull()

fun IntakeLogEntity.toDomain(): IntakeLog = IntakeLog(
    id = id,
    medicationId = medicationId,
    scheduledDate = LocalDate.parse(scheduledDate, fmt),
    scheduledTime = scheduledTime,
    action = runCatching { IntakeAction.valueOf(status) }.getOrDefault(IntakeAction.MISSED),
    actionTime = actionTime,
    snoozeCount = snoozeCount,
    note = note
)

fun IntakeLog.toEntity(): IntakeLogEntity = IntakeLogEntity(
    id = id,
    medicationId = medicationId,
    scheduledDate = scheduledDate.format(fmt),
    scheduledTime = scheduledTime,
    status = action.name,
    actionTime = actionTime,
    snoozeCount = snoozeCount,
    note = note
)
