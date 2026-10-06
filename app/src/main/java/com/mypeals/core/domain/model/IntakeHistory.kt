package com.mypeals.core.domain.model

import java.util.Calendar
import java.util.TimeZone

/** Estado de una toma programada tal y como se muestra en el historial. */
enum class DoseState {
    /** Tomada. */
    TAKEN,

    /** El usuario decidió omitirla. */
    SKIPPED,

    /** Pospuesta recientemente: aún se puede tomar. */
    SNOOZED,

    /** Ya pasó y no consta que se tomara. */
    MISSED,

    /** Aún no toca, o acaba de tocar y todavía está dentro del margen. */
    PENDING
}

/** Una toma que debía hacerse (o se hizo) en un momento concreto. */
data class HistoryEntry(
    val medicationId: Int,
    val medicationName: String,
    val dose: Int,
    val scheduledAtMillis: Long,
    val takenAtMillis: Long?,
    val state: DoseState
) {
    /** True si se puede marcar a mano como tomada. */
    val canMarkTaken: Boolean get() = state == DoseState.MISSED || state == DoseState.SNOOZED
}

/** Tomas de un día natural, ordenadas por hora. [dayStartMillis] es la medianoche local. */
data class HistoryDay(
    val dayStartMillis: Long,
    val entries: List<HistoryEntry>
)

/** Margen tras la hora programada en el que una toma sin registrar aún se considera pendiente. */
private const val PENDING_GRACE_MILLIS = 30 * 60_000L

/** Una toma pospuesta sigue "pospuesta" durante este tiempo desde su hora programada. */
private const val SNOOZE_WINDOW_MILLIS = 60 * 60_000L

private const val MAX_OCCURRENCES_PER_MEDICATION = 2_000

/**
 * Construye el historial de los últimos [days] días (incluido hoy): para cada medicación activa
 * calcula las tomas que le correspondían según su pauta y las cruza con lo registrado en [logs].
 *
 * Además muestra las anotaciones del historial que no encajan con la pauta actual (por ejemplo
 * si se cambió el horario o el tratamiento está en pausa). Los días sin ninguna toma se omiten
 * y el más reciente va primero.
 */
fun buildIntakeHistory(
    medications: List<Medication>,
    logs: List<IntakeLog>,
    nowMillis: Long,
    days: Int = 14,
    timeZone: TimeZone = TimeZone.getDefault()
): List<HistoryDay> {
    val windowStart = startOfDay(nowMillis, timeZone, -(days - 1))
    val windowEnd = startOfDay(nowMillis, timeZone, 1)
    val medicationsById = medications.associateBy { it.id }

    // Por cada toma programada puede haber varias anotaciones (pospuesta y luego tomada...).
    val resolved: Map<Pair<Int, Long>, IntakeLog> = logs
        .filter { it.scheduledAtMillis in windowStart until windowEnd }
        .groupBy { it.medicationId to it.scheduledAtMillis }
        .mapValues { (_, list) -> list.maxBy { it.status.priority() } }

    val entries = mutableListOf<HistoryEntry>()
    val covered = mutableSetOf<Pair<Int, Long>>()

    medications.filter { it.active }.forEach { medication ->
        var scheduled = medication.nextIntakeAfter(windowStart - 1, timeZone)
        var guard = 0
        while (scheduled != null && scheduled < windowEnd && guard++ < MAX_OCCURRENCES_PER_MEDICATION) {
            val key = medication.id to scheduled
            entries += entryOf(medication, scheduled, resolved[key], nowMillis)
            covered += key
            scheduled = medication.nextIntakeAfter(scheduled, timeZone)
        }
    }

    resolved.forEach { (key, log) ->
        val medication = medicationsById[key.first]
        if (key !in covered && medication != null) {
            entries += entryOf(medication, log.scheduledAtMillis, log, nowMillis)
        }
    }

    return entries
        .groupBy { startOfDay(it.scheduledAtMillis, timeZone, 0) }
        .map { (dayStart, dayEntries) ->
            HistoryDay(
                dayStartMillis = dayStart,
                entries = dayEntries.sortedWith(
                    compareBy<HistoryEntry> { it.scheduledAtMillis }.thenBy { it.medicationName }
                )
            )
        }
        .sortedByDescending { it.dayStartMillis }
}

private fun entryOf(
    medication: Medication,
    scheduledAtMillis: Long,
    log: IntakeLog?,
    nowMillis: Long
) = HistoryEntry(
    medicationId = medication.id,
    medicationName = medication.name,
    dose = medication.dosePerIntake,
    scheduledAtMillis = scheduledAtMillis,
    takenAtMillis = log?.takenAtMillis,
    state = doseState(log?.status, scheduledAtMillis, nowMillis)
)

private fun doseState(status: IntakeStatus?, scheduledAtMillis: Long, nowMillis: Long): DoseState = when {
    status == IntakeStatus.TAKEN -> DoseState.TAKEN
    status == IntakeStatus.SKIPPED -> DoseState.SKIPPED
    nowMillis - scheduledAtMillis < PENDING_GRACE_MILLIS -> DoseState.PENDING
    status == IntakeStatus.SNOOZED && nowMillis - scheduledAtMillis < SNOOZE_WINDOW_MILLIS -> DoseState.SNOOZED
    else -> DoseState.MISSED
}

/** Si hay varias anotaciones de la misma toma, manda la más "definitiva". */
private fun IntakeStatus.priority(): Int = when (this) {
    IntakeStatus.TAKEN -> 3
    IntakeStatus.SKIPPED -> 2
    IntakeStatus.SNOOZED -> 1
}

/** Medianoche local del día de [millis] desplazada [plusDays] días. */
private fun startOfDay(millis: Long, timeZone: TimeZone, plusDays: Int): Long =
    Calendar.getInstance(timeZone).apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, plusDays)
    }.timeInMillis
