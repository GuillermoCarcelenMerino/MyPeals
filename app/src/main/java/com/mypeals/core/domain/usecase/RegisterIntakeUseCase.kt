package com.mypeals.core.domain.usecase

import com.mypeals.core.domain.model.IntakeStatus
import com.mypeals.core.domain.repository.MedicationRepository
import com.mypeals.core.domain.util.runCatchingCancellable
import javax.inject.Inject

/** Registra en el historial lo ocurrido con una toma; si se tomó, descuenta el stock. */
class RegisterIntakeUseCase @Inject constructor(private val repository: MedicationRepository) {

    suspend operator fun invoke(
        medicationId: Int,
        scheduledAtMillis: Long,
        status: IntakeStatus,
        atMillis: Long = System.currentTimeMillis()
    ): Result<Unit> = runCatchingCancellable {
        repository.registerIntake(medicationId, scheduledAtMillis, atMillis, status)
    }
}
