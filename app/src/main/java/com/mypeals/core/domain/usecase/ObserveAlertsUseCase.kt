package com.mypeals.core.domain.usecase

import com.mypeals.core.domain.model.Alert
import com.mypeals.core.domain.model.buildAlerts
import com.mypeals.core.domain.repository.MedicationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Configured reminders and the time left until each one. Emits again whenever the medications
 * change and every [refreshMillis], so the countdown stays up to date.
 */
class ObserveAlertsUseCase @Inject constructor(private val repository: MedicationRepository) {

    operator fun invoke(
        refreshMillis: Long = DEFAULT_REFRESH_MILLIS,
        nowMillis: () -> Long = System::currentTimeMillis
    ): Flow<List<Alert>> = combine(repository.observeMedications(), ticker(refreshMillis)) { medications, _ ->
        buildAlerts(medications, nowMillis())
    }

    private fun ticker(periodMillis: Long): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(periodMillis)
        }
    }

    companion object {
        const val DEFAULT_REFRESH_MILLIS = 30_000L
    }
}
