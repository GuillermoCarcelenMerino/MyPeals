package com.mypeals.ui.Account

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class AccountNavigation : NavKey {
    @Serializable
    data object Profile : AccountNavigation()
}
