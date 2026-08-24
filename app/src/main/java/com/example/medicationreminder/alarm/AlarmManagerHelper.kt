package com.example.medicationreminder.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.medicationreminder.domain.model.Medication
import com.example.medicationreminder.domain.repository.MedicationRepository
import com.example.medicationreminder.util.DateUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper over [AlarmManager.setExactAndAllowWhileIdle] that keeps the
 * daily medication schedule pinned to the correct wall-clock time even in
 * Doze mode. Each medication owns exactly one repeating "exact" alarm that is
 * re-armed for the following day every time it fires.
 */
@Singleton
class AlarmManagerHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MedicationRepository
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** True when the app is allowed to schedule exact alarms (Android 12+). */
    fun canScheduleExact(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

    /**
     * (Re)schedule every enabled medication's next occurrence. Safe to call
     * repeatedly (e.g. on boot, on app launch, after permission grant).
     */
    suspend fun scheduleAll() {
        val treatmentStart = repository.ensureTreatmentStart()
        val meds = repository.getMedications().first()
        meds.filter { it.isEnabled }.forEach { scheduleMedication(it, treatmentStart) }
    }

    /** Schedule the next firing for a single medication. */
    fun scheduleMedication(med: Medication, treatmentStart: LocalDate) {
        val window = activeWindow(med, treatmentStart) ?: return // not in program
        val (activeStart, activeEnd) = window

        var candidate = if (LocalDate.now() < activeStart) activeStart else LocalDate.now()
        var trigger = candidate.atTime(med.hour, med.minute)
        if (trigger <= LocalDateTime.now()) {
            candidate = candidate.plusDays(1)
            trigger = candidate.atTime(med.hour, med.minute)
        }
        if (activeEnd != null && candidate > activeEnd) return // past the window
        if (candidate < activeStart) candidate = activeStart

        val epoch = candidate.toEpochMillisAt(med.hour, med.minute)
        if (epoch <= System.currentTimeMillis()) return // would fire immediately; skip
        scheduleExact(med.id, candidate, epoch, isSnooze = false)
    }

    /** Re-arm the day after a normal firing. */
    fun scheduleNextDay(med: Medication, firedDate: LocalDate, treatmentStart: LocalDate) {
        val window = activeWindow(med, treatmentStart) ?: return
        val (_, activeEnd) = window
        val next = firedDate.plusDays(1)
        if (activeEnd != null && next > activeEnd) return // program finished for this med
        scheduleExact(med.id, next, next.toEpochMillisAt(med.hour, med.minute), isSnooze = false)
    }

    /** Schedule a one-shot snooze (10 min from now). Does not re-arm the chain. */
    fun scheduleSnooze(medId: Long, date: LocalDate, atEpochMillis: Long) {
        scheduleExact(medId, date, atEpochMillis, isSnooze = true, requestOffset = 1_000_000)
    }

    fun cancelMedication(medId: Long) {
        alarmManager.cancel(buildPendingIntent(medId, LocalDate.now(), System.currentTimeMillis(), false))
        alarmManager.cancel(buildPendingIntent(medId, LocalDate.now(), System.currentTimeMillis(), false, 1_000_000))
    }

    // ------------------------------------------------------------------

    private fun scheduleExact(
        medId: Long,
        date: LocalDate,
        epochMillis: Long,
        isSnooze: Boolean,
        requestOffset: Int = 0
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return // permission not granted yet; UI will prompt and re-call scheduleAll()
        }
        val pi = buildPendingIntent(medId, date, epochMillis, isSnooze, requestOffset)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pi)
    }

    private fun buildPendingIntent(
        medId: Long,
        date: LocalDate,
        epochMillis: Long,
        isSnooze: Boolean,
        requestOffset: Int = 0
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmContract.ACTION_TRIGGER
            putExtra(AlarmContract.EXTRA_MED_ID, medId)
            putExtra(AlarmContract.EXTRA_DATE, DateUtils.formatDate(date))
            putExtra(AlarmContract.EXTRA_TIME, epochMillis)
            putExtra(AlarmContract.EXTRA_IS_SNOOZE, isSnooze)
        }
        val requestCode = medId.toInt() + requestOffset
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * @return Pair(activeStart, activeEnd?) for the medication, or null if it
     * is entirely in the past.
     */
    private fun activeWindow(
        med: Medication,
        treatmentStart: LocalDate
    ): Pair<LocalDate, LocalDate?>? {
        val activeStart = treatmentStart.plusDays((med.startDay - 1).toLong())
        val activeEnd = if (med.durationDays != null) {
            treatmentStart.plusDays((med.endDay - 1).toLong())
        } else null

        if (activeEnd != null && activeEnd < LocalDate.now()) return null
        return activeStart to activeEnd
    }
}
