package com.mypeals.ui.home

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class HomeNavigation : NavKey {
    @Serializable
    data object Home : HomeNavigation()
}
