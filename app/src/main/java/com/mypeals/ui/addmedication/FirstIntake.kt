package com.mypeals.ui.addmedication

import java.util.Calendar
import java.util.TimeZone

private val UTC: TimeZone = TimeZone.getTimeZone("UTC")

/**
 * El DatePicker de Material devuelve la fecha elegida como medianoche UTC. Esta función
 * la combina con una hora local y devuelve el instante resultante (epoch millis).
 */
fun combineDateAndTime(
    dateUtcMillis: Long,
    hour: Int,
    minute: Int,
    timeZone: TimeZone = TimeZone.getDefault()
): Long {
    val date = Calendar.getInstance(UTC).apply { timeInMillis = dateUtcMillis }
    return Calendar.getInstance(timeZone).apply {
        clear()
        set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), hour, minute, 0)
    }.timeInMillis
}

/** Operación inversa: fecha local de un instante, expresada como medianoche UTC. */
fun localDateToUtcMillis(
    timeMillis: Long,
    timeZone: TimeZone = TimeZone.getDefault()
): Long {
    val local = Calendar.getInstance(timeZone).apply { timeInMillis = timeMillis }
    return Calendar.getInstance(UTC).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}
