package com.mypeals.core.data.repository

import com.mypeals.core.data.room.AppDataBase
import com.mypeals.core.data.room.entities.IntakeLogEntity
import com.mypeals.core.data.room.mappers.toDomain
import com.mypeals.core.data.room.mappers.toEntity
import com.mypeals.core.domain.model.IntakeLog
import com.mypeals.core.domain.model.IntakeStatus
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MedicationRepositoryImpl @Inject constructor(private val database: AppDataBase) :
    MedicationRepository {

    override fun observeMedications(): Flow<List<Medication>> =
        database.medicationDao().observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeIntakeLog(sinceMillis: Long): Flow<List<IntakeLog>> =
        database.intakeLogDao().observeSince(sinceMillis).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getMedication(id: Int): Medication? =
        database.medicationDao().getMedication(id)?.toDomain()

    override suspend fun addMedication(medication: Medication): Int =
        database.medicationDao().insert(medication.toEntity()).toInt()

    override suspend fun updateMedication(medication: Medication) =
        database.medicationDao().update(medication.toEntity())

    override suspend fun deleteMedication(medication: Medication) =
        database.medicationDao().delete(medication.toEntity())

    override suspend fun registerIntake(
        medicationId: Int,
        scheduledAtMillis: Long,
        atMillis: Long,
        status: IntakeStatus
    ) {
        // El historial va primero: su clave foránea falla si la medicación ya no existe.
        database.intakeLogDao().insert(
            IntakeLogEntity(
                medicationId = medicationId,
                scheduledAt = scheduledAtMillis,
                takenAt = if (status == IntakeStatus.TAKEN) atMillis else null,
                status = status
            )
        )
        if (status == IntakeStatus.TAKEN) {
            val dose = database.medicationDao().getMedication(medicationId)?.dosePerIntake ?: 1
            database.medicationDao().consumeUnits(medicationId, dose)
        }
    }
}
