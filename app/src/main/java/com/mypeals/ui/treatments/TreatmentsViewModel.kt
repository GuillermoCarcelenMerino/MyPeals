package com.mypeals.ui.treatments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.usecase.DeleteMedicationUseCase
import com.mypeals.core.domain.usecase.ObserveMedicationsUseCase
import com.mypeals.core.domain.usecase.UpdateMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TreatmentsViewModel @Inject constructor(
    observeMedicationsUseCase: ObserveMedicationsUseCase,
    private val updateMedicationUseCase: UpdateMedicationUseCase,
    private val deleteMedicationUseCase: DeleteMedicationUseCase
) : ViewModel() {

    // La lista se alimenta del Flow de Room: cualquier alta o cambio se refleja solo.
    val uiState: StateFlow<TreatmentsUiState> = observeMedicationsUseCase()
        .map { TreatmentsUiState(medications = it, loading = false) }
        .catch { error ->
            Log.e(TAG, "Error al observar la medicación", error)
            emit(TreatmentsUiState(loading = false))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = TreatmentsUiState(loading = true)
        )

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
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
