package com.mypeals.ui.history

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class HistoryNavigation : NavKey {
    /** Historial de tomas (destino de la bottom bar). */
    @Serializable
    data object History : HistoryNavigation()
}
