package com.mypeals.ui.alerts

import com.mypeals.core.domain.model.Alert

data class AlertsUiState(
    val alerts: List<Alert> = emptyList(),
    val loading: Boolean = false
)

/** Time left split into units for display. */
data class RemainingTime(val days: Long, val hours: Long, val minutes: Long) {
    /** True when less than a minute is left (or the time has already passed). */
    val isNow: Boolean get() = days == 0L && hours == 0L && minutes == 0L
}

private const val MILLIS_PER_MINUTE = 60_000L

/**
 * Splits [millis] into days, hours and minutes. Rounds up to the minute, so "in 1 min" is shown
 * until the reminder fires. Anything under one second counts as "now".
 */
fun remainingTimeOf(millis: Long): RemainingTime {
    if (millis < 1_000L) return RemainingTime(0, 0, 0)
    val totalMinutes = (millis + MILLIS_PER_MINUTE - 1) / MILLIS_PER_MINUTE
    return RemainingTime(
        days = totalMinutes / (24 * 60),
        hours = totalMinutes / 60 % 24,
        minutes = totalMinutes % 60
    )
}
