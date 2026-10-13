package com.mypeals.ui.addmedication

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.MedicationType
import com.mypeals.core.domain.model.Weekday
import com.mypeals.core.domain.usecase.AddMedicationUseCase
import com.mypeals.core.domain.usecase.GetMedicationUseCase
import com.mypeals.core.domain.usecase.UpdateMedicationUseCase
import com.mypeals.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val addMedicationUseCase: AddMedicationUseCase,
    private val updateMedicationUseCase: UpdateMedicationUseCase,
    private val getMedicationUseCase: GetMedicationUseCase
) : BaseViewModel<AddMedicationUiState>(AddMedicationUiState.initial()) {

    private var loadedId: Int? = null


    /** Carga una medicación para editarla. Solo lo hace una vez por id. */
    fun load(id: Int) {
        if (loadedId == id) return
        loadedId = id
        viewModelScope.launch {
            getMedicationUseCase(id)
                .onSuccess { medication ->
                    if (medication != null) {
                        publish { AddMedicationUiState.fromMedication(medication) }
                    } else {
                        publish { it.copy(loadFailed = true) }
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "Error al cargar la medicación", error)
                    publish { it.copy(loadFailed = true) }
                }
        }
    }

    fun onNameChange(name: String) = publish { it.copy(name = name) }

    fun onTypeChange(type: MedicationType) = publish { it.copy(type = type) }

    fun onBoxesChange(boxes: String) = publish { it.copy(boxes = boxes) }

    fun onUnitsPerBoxChange(units: String) = publish { it.copy(unitsPerBox = units) }

    fun onDoseChange(dose: String) = publish { it.copy(dosePerIntake = dose) }

    fun onFrequencyIntervalChange(interval: String) =
        publish { it.copy(frequencyInterval = interval) }

    fun onFrequencyUnitChange(unit: FrequencyUnit) =
        publish { it.copy(frequencyUnit = unit) }

    fun onWeekdayToggle(day: Weekday) = publish {
        it.copy(weekdays = if (day in it.weekdays) it.weekdays - day else it.weekdays + day)
    }

    fun onStartDateChange(dateUtcMillis: Long) =
        publish { it.copy(startDateUtcMillis = dateUtcMillis) }

    fun onStartTimeChange(hour: Int, minute: Int) =
        publish { it.copy(startHour = hour, startMinute = minute) }

    /** Valida y guarda. Si el formulario no es válido solo activa la visualización de errores. */
    fun save(onSaved: () -> Unit) {
        val current = currentState
        if (current.saving) return
        val medication = current.toMedication()
        if (medication == null) {
            publish { it.copy(showErrors = true) }
            return
        }
        publish { it.copy(saving = true, saveFailed = false) }
        viewModelScope.launch {
            val result = if (current.isEditing) {
                updateMedicationUseCase(medication)
            } else {
                addMedicationUseCase(medication).map { }
            }
            result
                .onSuccess { onSaved() }
                .onFailure { error ->
                    Log.e(TAG, "Error al guardar la medicación", error)
                    publish { it.copy(saving = false, saveFailed = true) }
                }
        }
    }

    private companion object {
        const val TAG = "AddMedicationViewModel"
    }
}
