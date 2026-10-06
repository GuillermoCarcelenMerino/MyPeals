package com.mypeals.core.domain.usecase

import com.mypeals.core.domain.model.HistoryDay
import com.mypeals.core.domain.model.buildIntakeHistory
import com.mypeals.core.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Historial por días: lo que tocaba tomar cada día y si se tomó o no. */
class ObserveIntakeHistoryUseCase @Inject constructor(private val repository: MedicationRepository) {

    operator fun invoke(
        days: Int = DEFAULT_DAYS,
        nowMillis: () -> Long = System::currentTimeMillis
    ): Flow<List<HistoryDay>> {
        // Cota inferior holgada; el cálculo ya recorta a los días pedidos.
        val since = nowMillis() - (days + 1) * MILLIS_PER_DAY
        return combine(
            repository.observeMedications(),
            repository.observeIntakeLog(since)
        ) { medications, logs ->
            buildIntakeHistory(medications, logs, nowMillis(), days)
        }
    }

    companion object {
        const val DEFAULT_DAYS = 14
        private const val MILLIS_PER_DAY = 86_400_000L
    }
}
