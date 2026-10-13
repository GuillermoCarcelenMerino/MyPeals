package com.mypeals.ui.alerts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypeals.core.domain.usecase.ObserveAlertsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    observeAlerts: ObserveAlertsUseCase
) : ViewModel() {

    // WhileSubscribed stops the countdown refresh while the screen is not visible.
    val uiState: StateFlow<AlertsUiState> = observeAlerts()
        .map { AlertsUiState(alerts = it, loading = false) }
        .catch { error ->
            Log.e(TAG, "Error al observar las alertas", error)
            emit(AlertsUiState(loading = false))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = AlertsUiState(loading = true)
        )

    private companion object {
        const val TAG = "AlertsViewModel"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
