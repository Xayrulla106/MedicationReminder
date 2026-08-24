package com.example.medicationreminder.alarm

/**
 * Shared contract for the alarm Intent actions and extras used between
 * [AlarmReceiver], [NotificationHelper] and [AlarmActivity].
 */
object AlarmContract {

    const val ACTION_TRIGGER = "com.example.medicationreminder.action.TRIGGER"
    const val ACTION_MARK_TAKEN = "com.example.medicationreminder.action.MARK_TAKEN"
    const val ACTION_SNOOZE = "com.example.medicationreminder.action.SNOOZE"

    const val EXTRA_MED_ID = "extra_med_id"
    const val EXTRA_DATE = "extra_date"          // yyyy-MM-dd
    const val EXTRA_TIME = "extra_time"          // epoch millis of scheduled fire
    const val EXTRA_IS_SNOOZE = "extra_is_snooze" // true for snooze re-fires

    const val CHANNEL_ID = "medication_alarm_channel"

    /** Notification id == medication id keeps one live notification per med. */
    fun notificationId(medId: Long): Int = medId.toInt()
}
