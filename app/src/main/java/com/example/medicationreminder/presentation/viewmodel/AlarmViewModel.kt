package com.example.medicationreminder.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicationreminder.alarm.AlarmContract
import com.example.medicationreminder.alarm.AlarmManagerHelper
import com.example.medicationreminder.alarm.NotificationHelper
import com.example.medicationreminder.domain.model.IntakeAction
import com.example.medicationreminder.domain.model.Medication
import com.example.medicationreminder.domain.repository.MedicationRepository
import com.example.medicationreminder.domain.usecase.MarkIntakeUseCase
import com.example.medicationreminder.util.Constants.SNOOZE_MINUTES
import com.example.medicationreminder.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MedicationRepository,
    private val markIntake: MarkIntakeUseCase,
    private val notificationHelper: NotificationHelper,
    private val alarmManagerHelper: AlarmManagerHelper
) : ViewModel() {

    private val medId: Long = savedStateHandle.get<Long>(AlarmContract.EXTRA_MED_ID) ?: -1L
    private val dateStr: String = savedStateHandle.get<String>(AlarmContract.EXTRA_DATE) ?: ""
    private val scheduledTime: Long = savedStateHandle.get<Long>(AlarmContract.EXTRA_TIME) ?: 0L

    val medication: Flow<Medication?> = kotlinx.coroutines.flow.flow {
        emit(repository.getMedication(medId))
    }

    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    fun markTaken() = act(IntakeAction.TAKEN)
    fun skip() = act(IntakeAction.SKIPPED)

    fun snooze() {
        if (medId <= 0) return
        viewModelScope.launch {
            markIntake(medId, date = DateUtils.parseDate(dateStr), scheduledTime, IntakeAction.SNOOZED)
            notificationHelper.cancel(medId)
            val at = System.currentTimeMillis() + SNOOZE_MINUTES * 60_000L
            alarmManagerHelper.scheduleSnooze(medId, DateUtils.parseDate(dateStr), at)
            _finished.value = true
        }
    }

    private fun act(action: IntakeAction) {
        if (medId <= 0) return
        viewModelScope.launch {
            markIntake(medId, date = DateUtils.parseDate(dateStr), scheduledTime, action)
            notificationHelper.cancel(medId)
            _finished.value = true
        }
    }
}
