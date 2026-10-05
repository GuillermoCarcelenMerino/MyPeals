package com.mypeals.core.data.repository

import com.mypeals.core.data.room.AppDataBase
import com.mypeals.core.data.room.entities.Medication
import com.mypeals.core.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MedicationRepositoryImpl @Inject constructor(private val database: AppDataBase) :
    MedicationRepository {

    override fun observeMedications(): Flow<List<Medication>> =
        database.medicationDao().observeAll()

    override suspend fun getMedication(id: Int): Medication? =
        database.medicationDao().getMedication(id)

    override suspend fun addMedication(medication: Medication) =
        database.medicationDao().addMedication(medication)

    override suspend fun updateMedication(medication: Medication) =
        database.medicationDao().updateMedication(medication)

    override suspend fun deleteMedication(medication: Medication) =
        database.medicationDao().deleteMedication(medication)
}
