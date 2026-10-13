package com.mypeals.ui.treatments

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.usecase.DeleteMedicationUseCase
import com.mypeals.core.domain.usecase.ObserveMedicationsUseCase
import com.mypeals.core.domain.usecase.UpdateMedicationUseCase
import com.mypeals.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TreatmentsViewModel @Inject constructor(
    observeMedicationsUseCase: ObserveMedicationsUseCase,
    private val updateMedicationUseCase: UpdateMedicationUseCase,
    private val deleteMedicationUseCase: DeleteMedicationUseCase
) : BaseViewModel<TreatmentsUiState>(TreatmentsUiState(loading = true)) {

    init {
        // La lista se alimenta del Flow de Room: cualquier alta o cambio se refleja solo.
        observe(
            observeMedicationsUseCase().catch { error ->
                Log.e(TAG, "Error al observar la medicación", error)
                publish { it.copy(loading = false) }
            }
        ) { medications ->
            publish { it.copy(medications = medications, loading = false) }
        }
    }

    fun updateQuantity(medication: Medication, remainingUnits: Int) {
        viewModelScope.launch {
            updateMedicationUseCase(medication.copy(remainingUnits = remainingUnits.coerceAtLeast(0)))
                .onFailure { Log.e(TAG, "Error al actualizar la cantidad", it) }
        }
    }

    /** Pausa o reanuda el tratamiento: en pausa no genera avisos. */
    fun setActive(medication: Medication, active: Boolean) {
        viewModelScope.launch {
            updateMedicationUseCase(medication.copy(active = active))
                .onFailure { Log.e(TAG, "Error al cambiar el estado del tratamiento", it) }
        }
    }

    fun delete(medication: Medication) {
        viewModelScope.launch {
            deleteMedicationUseCase(medication)
                .onFailure { Log.e(TAG, "Error al borrar la medicación", it) }
        }
    }

    private companion object {
        const val TAG = "TreatmentsViewModel"
    }
}
