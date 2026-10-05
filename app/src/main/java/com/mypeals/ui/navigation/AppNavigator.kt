package com.mypeals.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

/**
 * Estado de navegación de la app: una pila por pestaña de la bottom bar, de modo que
 * cada pestaña conserva su propia posición al cambiar entre ellas.
 */
@Stable
class AppNavigator(
    val destinations: List<TopLevelDestination>,
    private val selectedIndexState: MutableState<Int>,
    private val backStacks: List<NavBackStack<NavKey>>
) {
    val selectedIndex: Int get() = selectedIndexState.value

    /** Pila de la pestaña seleccionada, la que muestra el NavDisplay. */
    val currentBackStack: NavBackStack<NavKey> get() = backStacks[selectedIndex]

    /** True cuando se está en la pantalla raíz de la pestaña (ahí se muestra la bottom bar). */
    val isAtTabRoot: Boolean get() = currentBackStack.size <= 1

    /** True si el botón atrás debe devolver a la pestaña de inicio. */
    val canReturnToStartTab: Boolean get() = isAtTabRoot && selectedIndex != 0

    fun selectTab(index: Int) {
        selectedIndexState.value = index
    }

    /** Abre una pantalla secundaria dentro de la pestaña actual. */
    fun navigate(key: NavKey) {
        currentBackStack.add(key)
    }

    fun goBack() {
        if (currentBackStack.size > 1) {
            currentBackStack.removeLastOrNull()
        } else if (canReturnToStartTab) {
            selectedIndexState.value = 0
        }
    }
}

@Composable
fun rememberAppNavigator(
    destinations: List<TopLevelDestination> = topLevelDestinations
): AppNavigator {
    val selectedIndex = rememberSaveable { mutableStateOf(0) }
    val backStacks = destinations.map { rememberNavBackStack(it.key) }
    return remember { AppNavigator(destinations, selectedIndex, backStacks) }
}
