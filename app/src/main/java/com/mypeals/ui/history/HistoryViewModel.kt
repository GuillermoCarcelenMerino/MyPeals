package com.mypeals.ui.history

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.mypeals.core.domain.model.HistoryDay
import com.mypeals.core.domain.model.HistoryEntry
import com.mypeals.core.domain.model.IntakeStatus
import com.mypeals.core.domain.usecase.ObserveIntakeHistoryUseCase
import com.mypeals.core.domain.usecase.RegisterIntakeUseCase
import com.mypeals.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
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
) : BaseViewModel<HistoryUiState>(HistoryUiState(loading = true)) {

    init {
        observe(
            observeIntakeHistory().catch { error ->
                Log.e(TAG, "Error al construir el historial", error)
                publish { it.copy(loading = false) }
            }
        ) { days ->
            publish { it.copy(days = days, loading = false) }
        }
    }

    /** Marca a mano como tomada una toma que quedó sin registrar (descuenta el stock). */
    fun markTaken(entry: HistoryEntry) {
        viewModelScope.launch {
            registerIntake(entry.medicationId, entry.scheduledAtMillis, IntakeStatus.TAKEN)
                .onFailure { Log.e(TAG, "Error al marcar la toma como tomada", it) }
        }
    }

    private companion object {
        const val TAG = "HistoryViewModel"
    }
}
