package com.mypeals.ui.addmedication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypeals.core.domain.model.Frequency
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.model.MedicationType
import com.mypeals.core.domain.usecase.AddMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val addMedicationUseCase: AddMedicationUseCase
) : ViewModel() {

    // PROVISIONAL: se sustituirá por el guardado del formulario real en la Fase 3.
    fun saveSample(onSaved: () -> Unit) {
        viewModelScope.launch {
            addMedicationUseCase(
                Medication(
                    name = SAMPLE_NAME,
                    type = MedicationType.PILL,
                    remainingUnits = SAMPLE_UNITS,
                    frequency = Frequency(interval = 8, unit = FrequencyUnit.HOURS),
                    firstIntakeAtMillis = System.currentTimeMillis()
                )
            )
                .onSuccess { onSaved() }
                .onFailure { Log.e(TAG, "Error al añadir la medicación", it) }
        }
    }

    private companion object {
        const val TAG = "AddMedicationViewModel"
        const val SAMPLE_NAME = "Pastilla de ejemplo"
        const val SAMPLE_UNITS = 3
    }
}
