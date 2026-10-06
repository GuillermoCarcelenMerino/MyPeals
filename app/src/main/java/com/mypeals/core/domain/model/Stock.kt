package com.mypeals.core.domain.model

/** Estado del stock de una medicación, listo para mostrarse al usuario. */
sealed interface StockStatus {
    /** No queda nada. */
    data object Empty : StockStatus

    /** No se conoce el contenido de la caja: solo se cuentan cajas. */
    data class BoxesOnly(val boxes: Int) : StockStatus

    /** Solo queda la última caja: se muestran únicamente sus unidades. */
    data class LastBox(val units: Int) : StockStatus

    /** Quedan varias cajas: [unitsInCurrentBox] son las unidades de la que está en uso. */
    data class MultipleBoxes(val boxes: Int, val unitsInCurrentBox: Int) : StockStatus
}

/** Por debajo de estos días de tratamiento se considera que el stock es bajo. */
const val LOW_STOCK_DAYS = 7.0

fun Medication.stockStatus(): StockStatus {
    if (remainingUnits <= 0) return StockStatus.Empty
    val perBox = unitsPerBox?.takeIf { it > 0 }
        ?: return StockStatus.BoxesOnly(remainingUnits)

    val boxes = (remainingUnits + perBox - 1) / perBox
    val unitsInCurrentBox = remainingUnits - (boxes - 1) * perBox
    return if (boxes == 1) {
        StockStatus.LastBox(unitsInCurrentBox)
    } else {
        StockStatus.MultipleBoxes(boxes, unitsInCurrentBox)
    }
}

/** Número medio de tomas al día según la frecuencia. */
fun Frequency.intakesPerDay(): Double {
    val every = interval.coerceAtLeast(1)
    return when (unit) {
        FrequencyUnit.HOURS -> 24.0 / every
        FrequencyUnit.DAYS -> 1.0 / every
        FrequencyUnit.WEEKS -> daysOfWeek.size.coerceAtLeast(1) / (7.0 * every)
    }
}

/** Días de tratamiento que cubre el stock actual, o null si no se puede estimar. */
fun Medication.estimatedDaysLeft(): Double? {
    val unitsPerDay = dosePerIntake * frequency.intakesPerDay()
    return if (unitsPerDay > 0) remainingUnits / unitsPerDay else null
}

/**
 * True si el tratamiento está activo, aún queda stock y este cubre menos de [thresholdDays].
 * Las medicaciones en pausa o sin stock no se marcan: para esas hay otro estado.
 */
fun Medication.isStockLow(thresholdDays: Double = LOW_STOCK_DAYS): Boolean {
    if (!active || remainingUnits <= 0) return false
    val days = estimatedDaysLeft() ?: return false
    return days < thresholdDays
}
