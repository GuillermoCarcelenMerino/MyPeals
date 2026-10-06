package com.mypeals.core.domain.reminder

import com.mypeals.core.domain.model.Medication

/** Programa y cancela los avisos de toma. La implementación usa el sistema de alarmas. */
interface ReminderScheduler {

    /**
     * Programa el aviso de la próxima toma posterior a [afterMillis]. Si la medicación está en
     * pausa o no tiene próxima toma, cancela el aviso que hubiera.
     */
    fun schedule(medication: Medication, afterMillis: Long = System.currentTimeMillis())

    /** Programa un aviso puntual (posponer) para [triggerAtMillis]. */
    fun scheduleSnooze(medicationId: Int, scheduledAtMillis: Long, triggerAtMillis: Long)

    /** Cancela todos los avisos de la medicación, incluido el de "posponer". */
    fun cancel(medicationId: Int)
}
