package com.mypeals.core.domain.usecase

import com.mypeals.core.data.room.entities.Medication
import com.mypeals.core.domain.repository.MedicationRepository
import com.mypeals.core.domain.util.runCatchingCancellable
import javax.inject.Inject

class DeleteMedicationUseCase @Inject constructor(private val repository: MedicationRepository) {

    suspend operator fun invoke(medication: Medication): Result<Unit> =
        runCatchingCancellable { repository.deleteMedication(medication) }
}
