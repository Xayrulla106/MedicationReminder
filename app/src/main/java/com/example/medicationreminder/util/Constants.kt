package com.example.medicationreminder.util

/**
 * Cross-cutting constants. Alarm intent actions/extras live in [com.example.medicationreminder.alarm.AlarmContract];
 * this object holds general, layer-agnostic values.
 */
object Constants {
    /** Local date format used in the DB and alarm intents (yyyy-MM-dd). */
    const val DATE_PATTERN = "yyyy-MM-dd"

    /** Default total length of the treatment program, in days (Grandaxin = 30d). */
    const val DEFAULT_TREATMENT_DURATION_DAYS = 30

    /** Snooze increment used by the alarm actions. */
    const val SNOOZE_MINUTES = 10L
}
