package com.mypeals.core.domain.usecase

import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.repository.MedicationRepository
import com.mypeals.core.domain.util.runCatchingCancellable
import javax.inject.Inject

class UpdateMedicationUseCase @Inject constructor(private val repository: MedicationRepository) {

    suspend operator fun invoke(medication: Medication): Result<Unit> =
        runCatchingCancellable { repository.updateMedication(medication) }
}
