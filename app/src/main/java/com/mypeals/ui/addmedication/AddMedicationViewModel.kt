package com.mypeals.ui.addmedication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.MedicationType
import com.mypeals.core.domain.model.Weekday
import com.mypeals.core.domain.usecase.AddMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val addMedicationUseCase: AddMedicationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicationUiState.initial())
    val uiState: StateFlow<AddMedicationUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }

    fun onTypeChange(type: MedicationType) = _uiState.update { it.copy(type = type) }

    fun onBoxesChange(boxes: String) = _uiState.update { it.copy(boxes = boxes) }

    fun onUnitsPerBoxChange(units: String) = _uiState.update { it.copy(unitsPerBox = units) }

    fun onDoseChange(dose: String) = _uiState.update { it.copy(dosePerIntake = dose) }

    fun onFrequencyIntervalChange(interval: String) =
        _uiState.update { it.copy(frequencyInterval = interval) }

    fun onFrequencyUnitChange(unit: FrequencyUnit) =
        _uiState.update { it.copy(frequencyUnit = unit) }

    fun onWeekdayToggle(day: Weekday) = _uiState.update {
        it.copy(weekdays = if (day in it.weekdays) it.weekdays - day else it.weekdays + day)
    }

    fun onStartDateChange(dateUtcMillis: Long) =
        _uiState.update { it.copy(startDateUtcMillis = dateUtcMillis) }

    fun onStartTimeChange(hour: Int, minute: Int) =
        _uiState.update { it.copy(startHour = hour, startMinute = minute) }

    /** Valida y guarda. Si el formulario no es válido solo activa la visualización de errores. */
    fun save(onSaved: () -> Unit) {
        val current = _uiState.value
        if (current.saving) return
        val medication = current.toMedication()
        if (medication == null) {
            _uiState.update { it.copy(showErrors = true) }
            return
        }
        _uiState.update { it.copy(saving = true, saveFailed = false) }
        viewModelScope.launch {
            addMedicationUseCase(medication)
                .onSuccess { onSaved() }
                .onFailure { error ->
                    Log.e(TAG, "Error al añadir la medicación", error)
                    _uiState.update { it.copy(saving = false, saveFailed = true) }
                }
        }
    }

    private companion object {
        const val TAG = "AddMedicationViewModel"
    }
}
