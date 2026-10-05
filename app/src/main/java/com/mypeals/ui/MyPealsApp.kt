package com.mypeals.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.mypeals.ui.navigation.appEntryProvider
import com.mypeals.ui.navigation.rememberAppNavigator

@Composable
fun MyPealsApp() {
    val navigator = rememberAppNavigator()

    // En la raíz de una pestaña distinta de la de inicio, "atrás" vuelve a la de inicio.
    // En pantallas secundarias, "atrás" lo gestiona el propio NavDisplay.
    BackHandler(enabled = navigator.canReturnToStartTab) { navigator.goBack() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Cada pantalla gestiona sus propios insets; este Scaffold solo reserva la bottom bar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(visible = navigator.isAtTabRoot) {
                AppBottomBar(
                    destinations = navigator.destinations,
                    selectedIndex = navigator.selectedIndex,
                    onDestinationSelected = navigator::selectTab
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = navigator.currentBackStack,
            modifier = Modifier.padding(innerPadding),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = appEntryProvider(navigator)
        )
    }
}
