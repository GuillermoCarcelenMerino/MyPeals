package com.mypeals.core.domain.usecase

import com.mypeals.core.domain.reminder.ReminderScheduler
import com.mypeals.core.domain.repository.MedicationRepository
import com.mypeals.core.domain.util.runCatchingCancellable
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Reprograma los avisos de todas las medicaciones. Se usa al abrir la app, tras reiniciar el
 * dispositivo, al cambiar la hora o la zona horaria y al conceder el permiso de alarmas exactas.
 */
class ScheduleAllRemindersUseCase @Inject constructor(
    private val repository: MedicationRepository,
    private val scheduler: ReminderScheduler
) {
    suspend operator fun invoke(): Result<Unit> = runCatchingCancellable {
        repository.observeMedications().first().forEach { scheduler.schedule(it) }
    }
}
