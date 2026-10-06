package com.mypeals.core.domain.usecase

import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.reminder.ReminderScheduler
import com.mypeals.core.domain.repository.MedicationRepository
import com.mypeals.core.domain.util.runCatchingCancellable
import javax.inject.Inject

class AddMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository,
    private val scheduler: ReminderScheduler
) {

    /** Guarda la medicación, programa su aviso y devuelve el id asignado. */
    suspend operator fun invoke(medication: Medication): Result<Int> = runCatchingCancellable {
        val id = repository.addMedication(medication)
        scheduler.schedule(medication.copy(id = id))
        id
    }
}
