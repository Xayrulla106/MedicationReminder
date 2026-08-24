package com.example.medicationreminder.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import com.example.medicationreminder.domain.model.HistoryDay
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(history: List<HistoryDay>) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Adherence History", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        if (history.isEmpty()) {
            BoxCentered(padding) { Text("No history yet.") }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history, key = { it.date.toString() }) { day -> HistoryDayCard(day) }
            }
        }
    }
}

@Composable
private fun HistoryDayCard(day: HistoryDay) {
    val pct = (day.adherenceRate * 100).toInt()
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = day.date.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${day.taken}/${day.total} • $pct%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            day.items.forEach { item ->
                val statusText = when (item.status) {
                    com.example.medicationreminder.domain.model.MedicationUiStatus.TAKEN -> "✅ Taken"
                    com.example.medicationreminder.domain.model.MedicationUiStatus.SKIPPED -> "⛔ Skipped"
                    else -> "⏳ Pending"
                }
                Text(
                    text = "• ${item.medication.name} ($statusText)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun BoxCentered(
    padding: androidx.compose.foundation.layout.PaddingValues,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        Modifier.fillMaxSize().fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center
    ) { content() }
}
