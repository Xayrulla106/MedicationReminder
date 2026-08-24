package com.example.medicationreminder.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medicationreminder.domain.model.DashboardState
import com.example.medicationreminder.presentation.ui.component.MedicationCard
import com.example.medicationreminder.presentation.ui.component.ProgressHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardState,
    onMarkTaken: (Long, Long) -> Unit,
    onSkip: (Long, Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Today's Schedule", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        if (state.items.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No medications scheduled for today.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { ProgressHeader(state) }
                items(state.items, key = { it.medication.id }) { item ->
                    MedicationCard(
                        item = item,
                        onMarkTaken = onMarkTaken,
                        onSkip = onSkip
                    )
                }
            }
        }
    }
}
