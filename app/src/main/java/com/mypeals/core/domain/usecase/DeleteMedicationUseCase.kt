package com.mypeals.core.domain.usecase

import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.reminder.ReminderScheduler
import com.mypeals.core.domain.repository.MedicationRepository
import com.mypeals.core.domain.util.runCatchingCancellable
import javax.inject.Inject

class DeleteMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository,
    private val scheduler: ReminderScheduler
) {

    /** Borra la medicación y cancela sus avisos. */
    suspend operator fun invoke(medication: Medication): Result<Unit> = runCatchingCancellable {
        repository.deleteMedication(medication)
        scheduler.cancel(medication.id)
    }
}
