package com.mypeals.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.mypeals.R
import com.mypeals.ui.history.HistoryNavigation
import com.mypeals.ui.treatments.TreatmentsNavigation

/** Destino de primer nivel: aparece como pestaña en la bottom bar. */
data class TopLevelDestination(
    val key: NavKey,
    @StringRes val label: Int,
    @DrawableRes val icon: Int
)

/**
 * Pestañas de la bottom bar, en orden. La primera es la pantalla de inicio.
 * Para añadir una pantalla nueva basta con definir su NavKey y añadirla aquí
 * y en [appEntryProvider].
 */
val topLevelDestinations: List<TopLevelDestination> = listOf(
    TopLevelDestination(
        key = TreatmentsNavigation.Treatments,
        label = R.string.nav_treatments,
        icon = R.drawable.ic_treatments
    ),
    TopLevelDestination(
        key = HistoryNavigation.History,
        label = R.string.nav_history,
        icon = R.drawable.ic_history
    ),
)
