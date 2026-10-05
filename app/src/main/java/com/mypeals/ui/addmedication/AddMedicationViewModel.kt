package com.mypeals.ui.addmedication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypeals.core.data.room.entities.Medication
import com.mypeals.core.data.room.entities.MedicationType
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
            addMedicationUseCase(Medication(0, SAMPLE_NAME, SAMPLE_QUANTITY, MedicationType.INJECTION))
                .onSuccess { onSaved() }
                .onFailure { Log.e(TAG, "Error al añadir la medicación", it) }
        }
    }

    private companion object {
        const val TAG = "AddMedicationViewModel"
        const val SAMPLE_NAME = "Pastilla de ejemplo"
        const val SAMPLE_QUANTITY = 3
    }
}
