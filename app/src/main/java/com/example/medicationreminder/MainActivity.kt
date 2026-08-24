package com.example.medicationreminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.medicationreminder.alarm.AlarmManagerHelper
import com.example.medicationreminder.presentation.theme.MediRemindTheme
import com.example.medicationreminder.presentation.ui.screen.DashboardScreen
import com.example.medicationreminder.presentation.ui.screen.HistoryScreen
import com.example.medicationreminder.presentation.viewmodel.DashboardViewModel
import com.example.medicationreminder.presentation.viewmodel.HistoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.collectAsState
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Today", Icons.Filled.Home)
    data object History : Screen("history", "History", Icons.Filled.History)
}

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var alarmManagerHelper: AlarmManagerHelper

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MediRemindTheme {
                val navController = rememberNavController()
                var showExactDialog by remember { mutableStateOf(false) }

                // 1) Notification permission (Android 13+)
                val notifPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { /* granted or not – alarms still work via channel */ }

                // 2) Exact-alarm permission (Android 12+). Re-schedule after the user returns.
                val exactSettingsLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) {
                    rescheduleAlarms()
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        !alarmManagerHelper.canScheduleExact()
                    ) {
                        showExactDialog = true
                    }
                }

                if (showExactDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showExactDialog = false },
                        confirmButton = {
                            androidx.compose.material3.TextButton(onClick = {
                                showExactDialog = false
                                val intent = Intent(
                                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:$packageName")
                                )
                                exactSettingsLauncher.launch(intent)
                            }) { Text("Open Settings") }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { showExactDialog = false }) {
                                Text("Later")
                            }
                        },
                        title = { Text(getString(R.string.perm_exact_title)) },
                        text = { Text(getString(R.string.perm_exact_body)) }
                    )
                }

                AppNavHost(navController = navController)
            }
        }
    }

    private fun rescheduleAlarms() {
        androidx.lifecycle.lifecycleScope.launch {
            alarmManagerHelper.scheduleAll()
            Toast.makeText(this@MainActivity, "Alarms scheduled", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun AppNavHost(navController: NavHostController) {
    val items = listOf(Screen.Dashboard, Screen.History)
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val vm: DashboardViewModel = hiltViewModel()
                val state by vm.state.collectAsState()
                DashboardScreen(state, vm::markTaken, vm::skip)
            }
            composable(Screen.History.route) {
                val vm: HistoryViewModel = hiltViewModel()
                val history by vm.history.collectAsState()
                HistoryScreen(history)
            }
        }
    }
}
