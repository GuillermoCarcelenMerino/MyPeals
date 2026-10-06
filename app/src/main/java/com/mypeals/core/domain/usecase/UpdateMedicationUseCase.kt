package com.mypeals.core.domain.usecase

import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.reminder.ReminderScheduler
import com.mypeals.core.domain.repository.MedicationRepository
import com.mypeals.core.domain.util.runCatchingCancellable
import javax.inject.Inject

class UpdateMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository,
    private val scheduler: ReminderScheduler
) {

    /** Guarda los cambios y reprograma el aviso (o lo cancela si la medicación está en pausa). */
    suspend operator fun invoke(medication: Medication): Result<Unit> = runCatchingCancellable {
        repository.updateMedication(medication)
        scheduler.schedule(medication)
    }
}
