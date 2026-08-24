package com.example.medicationreminder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.hilt.EntryPointAccessors
import com.example.medicationreminder.di.AlarmEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Re-arms the exact alarm schedule after a reboot or a system time change,
 * so reminders keep firing even if the app process was killed.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in setOf(
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED
            )
        ) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                val ep = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    AlarmEntryPoint::class.java
                )
                ep.notificationHelper().createChannel()
                ep.alarmManagerHelper().scheduleAll()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
