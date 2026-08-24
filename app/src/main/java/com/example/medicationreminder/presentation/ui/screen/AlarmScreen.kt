package com.example.medicationreminder.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.medicationreminder.presentation.viewmodel.AlarmViewModel

@Composable
fun AlarmScreen(
    viewModel: AlarmViewModel = hiltViewModel(),
    onClose: () -> Unit
) {
    val med by viewModel.medication.collectAsState(initial = null)
    val finished by viewModel.finished.collectAsState()

    LaunchedEffect(finished) {
        if (finished) onClose()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Time for your medication",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = med?.name ?: "…",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = med?.let { "${it.dosage} • ${it.quantity}" } ?: "",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = med?.instructions ?: "",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!med?.components.isNullOrEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = med!!.components.joinToString("\n"),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = viewModel::markTaken,
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Text("Mark as Taken", fontSize = 18.sp)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = viewModel::snooze,
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Text("Snooze 10 min", fontSize = 18.sp)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = viewModel::skip,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Skip")
            }
        }
    }
}
