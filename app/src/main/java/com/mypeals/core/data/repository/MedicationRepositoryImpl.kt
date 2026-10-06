package com.mypeals.core.data.repository

import com.mypeals.core.data.room.AppDataBase
import com.mypeals.core.data.room.mappers.toDomain
import com.mypeals.core.data.room.mappers.toEntity
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MedicationRepositoryImpl @Inject constructor(private val database: AppDataBase) :
    MedicationRepository {

    override fun observeMedications(): Flow<List<Medication>> =
        database.medicationDao().observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getMedication(id: Int): Medication? =
        database.medicationDao().getMedication(id)?.toDomain()

    override suspend fun addMedication(medication: Medication): Int =
        database.medicationDao().insert(medication.toEntity()).toInt()

    override suspend fun updateMedication(medication: Medication) =
        database.medicationDao().update(medication.toEntity())

    override suspend fun deleteMedication(medication: Medication) =
        database.medicationDao().delete(medication.toEntity())
}
