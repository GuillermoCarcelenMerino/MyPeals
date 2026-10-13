package com.mypeals.ui.alerts

import android.util.Log
import com.mypeals.core.domain.usecase.ObserveAlertsUseCase
import com.mypeals.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    observeAlerts: ObserveAlertsUseCase
) : BaseViewModel<AlertsUiState>(AlertsUiState(loading = true)) {

    init {
        // observe stops the countdown refresh while the screen is not visible.
        observe(
            observeAlerts().catch { error ->
                Log.e(TAG, "Error al observar las alertas", error)
                publish { it.copy(loading = false) }
            }
        ) { alerts ->
            publish { it.copy(alerts = alerts, loading = false) }
        }
    }

    private companion object {
        const val TAG = "AlertsViewModel"
    }
}
