package com.example.medicationreminder.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.medicationreminder.R
import com.example.medicationreminder.domain.model.Medication
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Creates the high-importance alarm channel and builds the heads-up /
 * full-screen notification shown when a dose is due.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val manager = NotificationManagerCompat.from(context)

    private val vibrationPattern = longArrayOf(0, 600, 300, 600, 300, 600)

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneUri
            val channel = NotificationChannel(
                AlarmContract.CHANNEL_ID,
                context.getString(R.string.channel_alarm_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_alarm_desc)
                setSound(
                    soundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                enableVibration(true)
                vibrationPattern = this@NotificationHelper.vibrationPattern
                setBypassDnd(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Build (and post) the alarm notification.
     *
     * @param med the medication that is due
     * @param date local date key (yyyy-MM-dd)
     * @param scheduledTime epoch millis of the scheduled fire time
     * @param isSnooze true for snooze re-fires (same behaviour, distinct id-safe)
     */
    fun notifyMedicationDue(
        med: Medication,
        date: String,
        scheduledTime: Long,
        isSnooze: Boolean
    ) {
        createChannel()
        val notification = buildNotification(med, date, scheduledTime, isSnooze)
        manager.notify(AlarmContract.notificationId(med.id), notification)
    }

    fun cancel(medId: Long) {
        manager.cancel(AlarmContract.notificationId(medId))
    }

    private fun buildNotification(
        med: Medication,
        date: String,
        scheduledTime: Long,
        isSnooze: Boolean
    ): Notification {
        val fullScreenIntent = buildActivityIntent(med.id, date, scheduledTime, isSnooze)
        val fsPending = PendingIntent.getActivity(
            context,
            (med.id + 2_000_000).toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takenAction = NotificationCompat.Action.Builder(
            R.drawable.ic_notification,
            context.getString(R.string.notification_taken),
            buildActionIntent(AlarmContract.ACTION_MARK_TAKEN, med.id, date, scheduledTime, isSnooze)
        ).build()

        val snoozeAction = NotificationCompat.Action.Builder(
            R.drawable.ic_notification,
            context.getString(R.string.notification_snooze),
            buildActionIntent(AlarmContract.ACTION_SNOOZE, med.id, date, scheduledTime, isSnooze)
        ).build()

        val componentsText = if (med.components.isNotEmpty()) {
            "\n• " + med.components.joinToString("\n• ")
        } else ""

        val bigText = "${med.dosage}  •  ${med.quantity}\n${med.instructions}$componentsText"

        return NotificationCompat.Builder(context, AlarmContract.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText("${med.name} • ${med.dosage} ${med.quantity}")
            .setStyle(NotificationCompat.BigTextStyle().bigText("${med.name}\n$bigText"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(RingtoneUri)
            .setVibrate(vibrationPattern)
            .setContentIntent(fsPending)
            .setFullScreenIntent(fsPending, true)
            .addAction(takenAction)
            .addAction(snoozeAction)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
    }

    private fun buildActivityIntent(
        medId: Long,
        date: String,
        scheduledTime: Long,
        isSnooze: Boolean
    ): Intent = Intent(context, com.example.medicationreminder.AlarmActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
        putExtra(AlarmContract.EXTRA_MED_ID, medId)
        putExtra(AlarmContract.EXTRA_DATE, date)
        putExtra(AlarmContract.EXTRA_TIME, scheduledTime)
        putExtra(AlarmContract.EXTRA_IS_SNOOZE, isSnooze)
    }

    private fun buildActionIntent(
        action: String,
        medId: Long,
        date: String,
        scheduledTime: Long,
        isSnooze: Boolean
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra(AlarmContract.EXTRA_MED_ID, medId)
            putExtra(AlarmContract.EXTRA_DATE, date)
            putExtra(AlarmContract.EXTRA_TIME, scheduledTime)
            putExtra(AlarmContract.EXTRA_IS_SNOOZE, isSnooze)
        }
        // Unique request code per (action, med) so multiple meds don't collide.
        val requestCode = (medId + when (action) {
            AlarmContract.ACTION_MARK_TAKEN -> 3_000_000
            AlarmContract.ACTION_SNOOZE -> 4_000_000
            else -> 5_000_000
        }).toInt()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private val RingtoneUri: Uri
        get() = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
            ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
}
