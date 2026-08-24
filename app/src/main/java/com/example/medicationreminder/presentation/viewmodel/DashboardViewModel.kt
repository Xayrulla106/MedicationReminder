package com.example.medicationreminder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicationreminder.domain.model.DashboardState
import com.example.medicationreminder.domain.model.IntakeAction
import com.example.medicationreminder.domain.usecase.GetDashboardUseCase
import com.example.medicationreminder.domain.usecase.MarkIntakeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboard: GetDashboardUseCase,
    private val markIntake: MarkIntakeUseCase
) : ViewModel() {

    val state: StateFlow<DashboardState> = getDashboard().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardState(
            treatmentDay = 0,
            treatmentDurationDays = 30,
            items = emptyList(),
            takenCount = 0,
            totalCount = 0
        )
    )

    fun markTaken(medicationId: Long, scheduledTime: Long) {
        act(medicationId, scheduledTime, IntakeAction.TAKEN)
    }

    fun skip(medicationId: Long, scheduledTime: Long) {
        act(medicationId, scheduledTime, IntakeAction.SKIPPED)
    }

    private fun act(medicationId: Long, scheduledTime: Long, action: IntakeAction) {
        viewModelScope.launch {
            markIntake(medicationId, scheduledTime = scheduledTime, action = action)
        }
    }
}
