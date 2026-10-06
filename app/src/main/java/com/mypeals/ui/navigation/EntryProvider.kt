package com.mypeals.ui.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.mypeals.ui.addmedication.AddMedicationScreen
import com.mypeals.ui.history.HistoryNavigation
import com.mypeals.ui.history.HistoryScreen
import com.mypeals.ui.treatments.TreatmentsNavigation
import com.mypeals.ui.treatments.TreatmentsScreen

fun appEntryProvider(navigator: AppNavigator) = entryProvider<NavKey> {
    entry<TreatmentsNavigation.Treatments> {
        TreatmentsScreen(
            onAddMedication = { navigator.navigate(TreatmentsNavigation.AddMedication) },
            onEditMedication = { navigator.navigate(TreatmentsNavigation.EditMedication(it)) }
        )
    }
    entry<TreatmentsNavigation.AddMedication> {
        AddMedicationScreen(onBack = navigator::goBack)
    }
    entry<TreatmentsNavigation.EditMedication> { key ->
        AddMedicationScreen(medicationId = key.medicationId, onBack = navigator::goBack)
    }
    entry<HistoryNavigation.History> {
        HistoryScreen()
    }
}
