package com.example.medicationreminder

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.example.medicationreminder.presentation.theme.MediRemindTheme
import com.example.medicationreminder.presentation.ui.screen.AlarmScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * Full-screen, lock-screen-overlay activity that rings when a dose is due.
 * Uses the window flags + Compose UI so the user gets a large, interactive alert.
 */
@AndroidEntryPoint
class AlarmActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show on top of the lock screen and wake/turn the screen on.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            MediRemindTheme(darkTheme = true) {
                AlarmScreen(onClose = { finish() })
            }
        }
    }
}
