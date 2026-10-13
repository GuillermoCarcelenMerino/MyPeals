package com.mypeals.core.domain.model

import java.util.TimeZone

/** Status of a medication's reminder as shown on the alerts screen. */
enum class AlertStatus {
    /** A reminder is scheduled for [Alert.nextAtMillis]. */
    SCHEDULED,

    /** The treatment is paused: no reminders until it is resumed. */
    PAUSED,

    /** The treatment is active but its schedule has no further intakes. */
    NO_UPCOMING
}

/** Reminder configured for a medication. */
data class Alert(
    val medicationId: Int,
    val medicationName: String,
    val dose: Int,
    val frequency: Frequency,
    val status: AlertStatus,
    /** Time of the next reminder; null unless [status] is [AlertStatus.SCHEDULED]. */
    val nextAtMillis: Long?,
    /** Time left until [nextAtMillis]; null when there is no next reminder. */
    val remainingMillis: Long?
)

/**
 * Builds the reminders configured for [medications] at [nowMillis]. The next reminder is computed
 * with [nextIntakeAfter], the same rule the scheduler uses to set the alarm.
 *
 * Scheduled reminders come first, soonest first; the rest follow sorted by name.
 */
fun buildAlerts(
    medications: List<Medication>,
    nowMillis: Long,
    timeZone: TimeZone = TimeZone.getDefault()
): List<Alert> {
    val alerts = medications.map { medication ->
        val next = if (medication.active) medication.nextIntakeAfter(nowMillis, timeZone) else null
        Alert(
            medicationId = medication.id,
            medicationName = medication.name,
            dose = medication.dosePerIntake,
            frequency = medication.frequency,
            status = when {
                !medication.active -> AlertStatus.PAUSED
                next == null -> AlertStatus.NO_UPCOMING
                else -> AlertStatus.SCHEDULED
            },
            nextAtMillis = next,
            remainingMillis = next?.minus(nowMillis)
        )
    }
    val (scheduled, others) = alerts.partition { it.status == AlertStatus.SCHEDULED }
    return scheduled.sortedWith(compareBy<Alert> { it.nextAtMillis }.thenBy { it.medicationName }) +
        others.sortedBy { it.medicationName }
}
