package com.mypeals.core.domain.repository

import com.mypeals.core.domain.model.IntakeLog
import com.mypeals.core.domain.model.IntakeStatus
import com.mypeals.core.domain.model.Medication
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun observeMedications(): Flow<List<Medication>>

    /** Anotaciones del historial cuya toma estaba programada a partir de [sinceMillis]. */
    fun observeIntakeLog(sinceMillis: Long): Flow<List<IntakeLog>>
    suspend fun getMedication(id: Int): Medication?

    /** Devuelve el id asignado a la medicación. */
    suspend fun addMedication(medication: Medication): Int
    suspend fun updateMedication(medication: Medication)
    suspend fun deleteMedication(medication: Medication)

    /**
     * Anota en el historial lo ocurrido con una toma. Con [IntakeStatus.TAKEN] descuenta además
     * la dosis del stock (sin bajar de cero).
     */
    suspend fun registerIntake(
        medicationId: Int,
        scheduledAtMillis: Long,
        atMillis: Long,
        status: IntakeStatus
    )
}
