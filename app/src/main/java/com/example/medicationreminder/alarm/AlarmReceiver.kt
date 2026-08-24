package com.example.medicationreminder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.hilt.EntryPointAccessors
import com.example.medicationreminder.di.AlarmEntryPoint
import com.example.medicationreminder.domain.model.IntakeAction
import com.example.medicationreminder.domain.util.isActiveOnDay
import com.example.medicationreminder.util.Constants.SNOOZE_MINUTES
import com.example.medicationreminder.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Central receiver for:
 *  - [AlarmContract.ACTION_TRIGGER] : an exact alarm fired -> show notification
 *    (unless already taken/skipped) and re-arm the next day.
 *  - [AlarmContract.ACTION_MARK_TAKEN] / [AlarmContract.ACTION_SNOOZE] :
 *    fired by the notification's action buttons.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: AlarmContract.ACTION_TRIGGER
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    AlarmEntryPoint::class.java
                )
                when (action) {
                    AlarmContract.ACTION_MARK_TAKEN -> handleMarkTaken(context, entryPoint, intent)
                    AlarmContract.ACTION_SNOOZE -> handleSnooze(context, entryPoint, intent)
                    else -> handleTrigger(context, entryPoint, intent)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleTrigger(
        context: Context,
        ep: AlarmEntryPoint,
        intent: Intent
    ) {
        val medId = intent.getLongExtra(AlarmContract.EXTRA_MED_ID, -1L)
        val dateStr = intent.getStringExtra(AlarmContract.EXTRA_DATE) ?: return
        val timeEpoch = intent.getLongExtra(AlarmContract.EXTRA_TIME, 0L)
        val isSnooze = intent.getBooleanExtra(AlarmContract.EXTRA_IS_SNOOZE, false)
        if (medId <= 0) return

        val repo = ep.medicationRepository()
        val med = repo.getMedication(medId) ?: return
        val treatmentStart = repo.ensureTreatmentStart()
        val firedDate = DateUtils.parseDate(dateStr)
        val day = DateUtils.treatmentDay(treatmentStart, firedDate)

        // If the medication is no longer in its active window, drop it silently.
        if (!med.isActiveOnDay(day)) {
            if (!isSnooze) ep.alarmManagerHelper().scheduleNextDay(med, firedDate, treatmentStart)
            return
        }

        val log = repo.getLog(medId, firedDate)
        val alreadyDone = log?.action == IntakeAction.TAKEN || log?.action == IntakeAction.SKIPPED

        if (!alreadyDone) {
            ep.notificationHelper().notifyMedicationDue(med, dateStr, timeEpoch, isSnooze)
        }

        // Re-arm the daily chain for tomorrow (only for the original daily alarm).
        if (!isSnooze) {
            ep.alarmManagerHelper().scheduleNextDay(med, firedDate, treatmentStart)
        }
    }

    private suspend fun handleMarkTaken(
        context: Context,
        ep: AlarmEntryPoint,
        intent: Intent
    ) {
        val medId = intent.getLongExtra(AlarmContract.EXTRA_MED_ID, -1L)
        val dateStr = intent.getStringExtra(AlarmContract.EXTRA_DATE) ?: return
        val timeEpoch = intent.getLongExtra(AlarmContract.EXTRA_TIME, 0L)
        if (medId <= 0) return

        val repo = ep.medicationRepository()
        repo.recordIntake(medId, DateUtils.parseDate(dateStr), timeEpoch, IntakeAction.TAKEN)
        ep.notificationHelper().cancel(medId)
    }

    private suspend fun handleSnooze(
        context: Context,
        ep: AlarmEntryPoint,
        intent: Intent
    ) {
        val medId = intent.getLongExtra(AlarmContract.EXTRA_MED_ID, -1L)
        val dateStr = intent.getStringExtra(AlarmContract.EXTRA_DATE) ?: return
        val timeEpoch = intent.getLongExtra(AlarmContract.EXTRA_TIME, 0L)
        if (medId <= 0) return

        val repo = ep.medicationRepository()
        val firedDate = DateUtils.parseDate(dateStr)
        repo.recordIntake(medId, firedDate, timeEpoch, IntakeAction.SNOOZED, snoozeIncrement = true)
        ep.notificationHelper().cancel(medId)

        val snoozeAt = System.currentTimeMillis() + SNOOZE_MINUTES * 60_000L
        ep.alarmManagerHelper().scheduleSnooze(medId, firedDate, snoozeAt)
    }
}
