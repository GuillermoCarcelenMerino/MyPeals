package com.mypeals.ui.alerts

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class AlertsNavigation : NavKey {
    /** Configured reminders and time left until each one (bottom bar destination). */
    @Serializable
    data object Alerts : AlertsNavigation()
}
