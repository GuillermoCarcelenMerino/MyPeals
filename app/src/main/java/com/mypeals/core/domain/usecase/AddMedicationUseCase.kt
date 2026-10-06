package com.mypeals.core.domain.usecase

import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.repository.MedicationRepository
import com.mypeals.core.domain.util.runCatchingCancellable
import javax.inject.Inject

class AddMedicationUseCase @Inject constructor(private val repository: MedicationRepository) {

    /** Devuelve el id de la medicación creada. */
    suspend operator fun invoke(medication: Medication): Result<Int> =
        runCatchingCancellable { repository.addMedication(medication) }
}
