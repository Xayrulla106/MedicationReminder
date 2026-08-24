package com.example.medicationreminder.di

import androidx.hilt.EntryPoint
import androidx.hilt.InstallIn
import androidx.hilt.components.SingletonComponent
import com.example.medicationreminder.alarm.AlarmManagerHelper
import com.example.medicationreminder.alarm.NotificationHelper
import com.example.medicationreminder.domain.repository.MedicationRepository

/**
 * Hilt entry-point used by [com.example.medicationreminder.alarm.AlarmReceiver]
 * (BroadcastReceivers cannot be @AndroidEntryPoint directly). It exposes the
 * singletons the receiver needs without holding a Context reference itself.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AlarmEntryPoint {
    fun medicationRepository(): MedicationRepository
    fun alarmManagerHelper(): AlarmManagerHelper
    fun notificationHelper(): NotificationHelper
}
