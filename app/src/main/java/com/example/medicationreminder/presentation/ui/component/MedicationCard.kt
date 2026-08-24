package com.example.medicationreminder.presentation.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medicationreminder.domain.model.MedicationCategory
import com.example.medicationreminder.domain.model.MedicationItem

@Composable
fun MedicationCard(
    item: MedicationItem,
    onMarkTaken: (Long, Long) -> Unit,
    onSkip: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val med = item.medication
    val canAct = item.status != com.example.medicationreminder.domain.model.MedicationUiStatus.TAKEN
    val isIv = med.category == MedicationCategory.IV_SESSION

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isIv) Icons.Filled.Science else Icons.Filled.Medication,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = med.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${med.dosage} • ${med.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(item.status)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "🕑 ${med.timeLabel}  •  ${med.instructions}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (med.components.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = med.components.joinToString("  •  "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (canAct) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { onSkip(med.id, item.scheduledTime) }) {
                        Text("Skip")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onMarkTaken(med.id, item.scheduledTime) }) {
                        Text("Mark as Taken")
                    }
                }
            } else {
                if (item.snoozeCount > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Snoozed ${item.snoozeCount}×",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
