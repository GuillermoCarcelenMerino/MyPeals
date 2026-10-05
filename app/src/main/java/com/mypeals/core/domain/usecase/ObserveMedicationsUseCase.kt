package com.mypeals.core.domain.usecase

import com.mypeals.core.data.room.entities.Medication
import com.mypeals.core.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMedicationsUseCase @Inject constructor(private val repository: MedicationRepository) {

    operator fun invoke(): Flow<List<Medication>> = repository.observeMedications()
}
