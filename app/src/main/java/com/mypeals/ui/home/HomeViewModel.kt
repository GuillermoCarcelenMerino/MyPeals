package com.mypeals.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypeals.core.data.room.entities.Medication
import com.mypeals.core.domain.usecase.AddMedicationUseCase
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
class HomeViewModel @Inject constructor(
    observeMedicationsUseCase: ObserveMedicationsUseCase,
    private val addMedicationUseCase: AddMedicationUseCase,
    private val updateMedicationUseCase: UpdateMedicationUseCase
) : ViewModel() {

    // La lista se alimenta del Flow de Room: cualquier alta o cambio se refleja solo.
    val uiState: StateFlow<UIState> = observeMedicationsUseCase()
        .map { UIState(medications = it, loading = false) }
        .catch { error ->
            Log.e(TAG, "Error al observar la medicación", error)
            emit(UIState(loading = false))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UIState(loading = true)
        )

    fun addMedication(medication: Medication) {
        viewModelScope.launch {
            addMedicationUseCase(medication).onFailure {
                Log.e(TAG, "Error al añadir la medicación", it)
            }
        }
    }

    fun updateQuantity(medication: Medication, quantity: Int) {
        viewModelScope.launch {
            updateMedicationUseCase(medication.copy(quantity = quantity.coerceAtLeast(0)))
                .onFailure { Log.e(TAG, "Error al actualizar la cantidad", it) }
        }
    }

    private companion object {
        const val TAG = "HomeViewModel"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
