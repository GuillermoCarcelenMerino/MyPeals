package com.mypeals.ui.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypeals.core.domain.model.HistoryDay
import com.mypeals.core.domain.model.HistoryEntry
import com.mypeals.core.domain.model.IntakeStatus
import com.mypeals.core.domain.usecase.ObserveIntakeHistoryUseCase
import com.mypeals.core.domain.usecase.RegisterIntakeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val days: List<HistoryDay> = emptyList(),
    val loading: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    observeIntakeHistory: ObserveIntakeHistoryUseCase,
    private val registerIntake: RegisterIntakeUseCase
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = observeIntakeHistory()
        .map { HistoryUiState(days = it, loading = false) }
        .catch { error ->
            Log.e(TAG, "Error al construir el historial", error)
            emit(HistoryUiState(loading = false))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HistoryUiState(loading = true)
        )

    /** Marca a mano como tomada una toma que quedó sin registrar (descuenta el stock). */
    fun markTaken(entry: HistoryEntry) {
        viewModelScope.launch {
            registerIntake(entry.medicationId, entry.scheduledAtMillis, IntakeStatus.TAKEN)
                .onFailure { Log.e(TAG, "Error al marcar la toma como tomada", it) }
        }
    }

    private companion object {
        const val TAG = "HistoryViewModel"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
