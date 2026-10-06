package com.mypeals.core.domain.model

import java.util.Calendar
import java.util.TimeZone

private const val MILLIS_PER_HOUR = 3_600_000L
private const val MILLIS_PER_DAY = 86_400_000L
private val UTC: TimeZone = TimeZone.getTimeZone("UTC")

/**
 * Primera toma programada estrictamente posterior a [afterMillis], o null si no hay ninguna.
 *
 * - **Horas:** intervalos absolutos desde la primera toma.
 * - **Días y semanas:** se mantiene la hora local de la primera toma, incluso al cambiar
 *   el horario de verano (las 09:00 siguen siendo las 09:00).
 * - **Semanas con días:** solo los días marcados, en las semanas que toquen según el intervalo
 *   (las semanas empiezan en lunes). Sin días marcados se usa el día de la primera toma.
 */
fun Medication.nextIntakeAfter(
    afterMillis: Long,
    timeZone: TimeZone = TimeZone.getDefault()
): Long? {
    val interval = frequency.interval.coerceAtLeast(1)
    // Nunca se devuelve nada anterior a la primera toma.
    val from = maxOf(afterMillis, firstIntakeAtMillis - 1)

    if (frequency.unit == FrequencyUnit.HOURS) {
        val step = interval * MILLIS_PER_HOUR
        val elapsed = from - firstIntakeAtMillis
        val k = if (elapsed < 0) 0 else elapsed / step + 1
        return firstIntakeAtMillis + k * step
    }

    val firstDay = localEpochDay(firstIntakeAtMillis, timeZone)
    val time = Calendar.getInstance(timeZone).apply { timeInMillis = firstIntakeAtMillis }
    val hour = time.get(Calendar.HOUR_OF_DAY)
    val minute = time.get(Calendar.MINUTE)

    val firstMonday = firstDay - weekdayIndex(firstDay)
    val weekdays = frequency.daysOfWeek.ifEmpty { setOf(Weekday.entries[weekdayIndex(firstDay)]) }

    fun matches(day: Long): Boolean = day >= firstDay && when (frequency.unit) {
        FrequencyUnit.DAYS -> Math.floorMod(day - firstDay, interval.toLong()) == 0L
        FrequencyUnit.WEEKS ->
            Weekday.entries[weekdayIndex(day)] in weekdays &&
                Math.floorMod(Math.floorDiv(day - firstMonday, 7L), interval.toLong()) == 0L
        FrequencyUnit.HOURS -> false
    }

    val startDay = maxOf(firstDay, localEpochDay(from, timeZone))
    val horizon = if (frequency.unit == FrequencyUnit.WEEKS) 7L * interval + 7 else interval.toLong() + 1
    var day = startDay
    while (day <= startDay + horizon) {
        if (matches(day)) {
            val instant = instantAt(day, hour, minute, timeZone)
            if (instant > from) return instant
        }
        day++
    }
    return null
}

/** Día local (días desde 1970-01-01) al que pertenece un instante. */
private fun localEpochDay(millis: Long, timeZone: TimeZone): Long =
    Math.floorDiv(millis + timeZone.getOffset(millis), MILLIS_PER_DAY)

/** 0 = lunes ... 6 = domingo. El 1970-01-01 fue jueves. */
private fun weekdayIndex(epochDay: Long): Int = Math.floorMod(epochDay + 3, 7L).toInt()

private fun instantAt(epochDay: Long, hour: Int, minute: Int, timeZone: TimeZone): Long {
    val date = Calendar.getInstance(UTC).apply { timeInMillis = epochDay * MILLIS_PER_DAY }
    return Calendar.getInstance(timeZone).apply {
        clear()
        set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), hour, minute, 0)
    }.timeInMillis
}
