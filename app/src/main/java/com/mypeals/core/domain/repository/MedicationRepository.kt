package com.mypeals.core.domain.repository

import com.mypeals.core.domain.model.Medication
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun observeMedications(): Flow<List<Medication>>
    suspend fun getMedication(id: Int): Medication?

    /** Devuelve el id asignado a la medicación. */
    suspend fun addMedication(medication: Medication): Int
    suspend fun updateMedication(medication: Medication)
    suspend fun deleteMedication(medication: Medication)
}
