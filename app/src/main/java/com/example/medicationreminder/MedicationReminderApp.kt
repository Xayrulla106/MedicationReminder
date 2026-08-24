package com.example.medicationreminder

import android.app.Application
import com.example.medicationreminder.alarm.AlarmManagerHelper
import com.example.medicationreminder.alarm.NotificationHelper
import com.example.medicationreminder.domain.repository.MedicationRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MedicationReminderApp : Application() {

    @Inject
    lateinit var repository: MedicationRepository

    @Inject
    lateinit var alarmManagerHelper: AlarmManagerHelper

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createChannel()

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            // Seed the hardcoded schedule (first run) and arm the exact alarms.
            repository.seedIfEmpty()
            alarmManagerHelper.scheduleAll()
        }
    }
}
