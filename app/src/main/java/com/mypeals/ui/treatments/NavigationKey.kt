package com.mypeals.ui.treatments

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class TreatmentsNavigation : NavKey {
    /** Pantalla principal con el listado de tratamientos (destino de la bottom bar). */
    @Serializable
    data object Treatments : TreatmentsNavigation()

    /** Pantalla secundaria para añadir una medicación. */
    @Serializable
    data object AddMedication : TreatmentsNavigation()
}
