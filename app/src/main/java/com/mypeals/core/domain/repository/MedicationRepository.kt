package com.mypeals.core.domain.repository

import com.mypeals.core.data.room.entities.Medication
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun observeMedications(): Flow<List<Medication>>
    suspend fun getMedication(id: Int): Medication?
    suspend fun addMedication(medication: Medication)
    suspend fun updateMedication(medication: Medication)
    suspend fun deleteMedication(medication: Medication)
}
