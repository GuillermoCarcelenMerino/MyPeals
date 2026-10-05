package com.mypeals.ui.Account

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import kotlinx.serialization.Serializable

@Serializable
sealed class AccountNavigation : NavKey {
    @Serializable
    data object Profile : AccountNavigation()
}
