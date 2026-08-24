package com.example.medicationreminder.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.medicationreminder.domain.model.MedicationUiStatus

@Composable
fun StatusChip(status: MedicationUiStatus) {
    val (label, containerColor, contentColor) = when (status) {
        MedicationUiStatus.UPCOMING ->
            Triple("Upcoming", Color(0xFF90A4AE), Color.White)
        MedicationUiStatus.PENDING ->
            Triple("Pending", Color(0xFFFFB300), Color.Black)
        MedicationUiStatus.TAKEN ->
            Triple("Taken", Color(0xFF2E7D32), Color.White)
        MedicationUiStatus.SKIPPED ->
            Triple("Skipped", Color(0xFFC62828), Color.White)
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}
