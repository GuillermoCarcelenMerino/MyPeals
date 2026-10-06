package com.mypeals.core.domain.usecase

import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.repository.MedicationRepository
import com.mypeals.core.domain.util.runCatchingCancellable
import javax.inject.Inject

class GetMedicationUseCase @Inject constructor(private val repository: MedicationRepository) {

    /** Devuelve la medicación, o null dentro del Result si no existe. */
    suspend operator fun invoke(id: Int): Result<Medication?> = runCatchingCancellable {
        repository.getMedication(id)
    }
}
