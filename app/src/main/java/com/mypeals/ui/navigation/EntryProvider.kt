package com.mypeals.ui.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.mypeals.ui.Account.AccountNavigation
import com.mypeals.ui.Account.AccountScreen
import com.mypeals.ui.addmedication.AddMedicationScreen
import com.mypeals.ui.treatments.TreatmentsNavigation
import com.mypeals.ui.treatments.TreatmentsScreen

fun appEntryProvider(navigator: AppNavigator) = entryProvider<NavKey> {
    entry<TreatmentsNavigation.Treatments> {
        TreatmentsScreen(
            onAddMedication = { navigator.navigate(TreatmentsNavigation.AddMedication) }
        )
    }
    entry<TreatmentsNavigation.AddMedication> {
        AddMedicationScreen(onBack = navigator::goBack)
    }
    entry<AccountNavigation.Profile> {
        AccountScreen()
    }
}
