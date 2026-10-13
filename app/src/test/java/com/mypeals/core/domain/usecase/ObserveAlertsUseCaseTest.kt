package com.mypeals.core.domain.usecase

import com.mypeals.core.domain.model.Frequency
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.IntakeLog
import com.mypeals.core.domain.model.IntakeStatus
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.model.MedicationType
import com.mypeals.core.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

private class AlertsFakeRepository(medications: List<Medication>) : MedicationRepository {
    val items = MutableStateFlow(medications)

    override fun observeMedications(): Flow<List<Medication>> = items
    override fun observeIntakeLog(sinceMillis: Long): Flow<List<IntakeLog>> = MutableStateFlow(emptyList())
    override suspend fun getMedication(id: Int) = items.value.firstOrNull { it.id == id }
    override suspend fun addMedication(medication: Medication) = error("no usado")
    override suspend fun updateMedication(medication: Medication) = error("no usado")
    override suspend fun deleteMedication(medication: Medication) = error("no usado")
    override suspend fun registerIntake(
        medicationId: Int,
        scheduledAtMillis: Long,
        atMillis: Long,
        status: IntakeStatus
    ) = error("no usado")
}

class ObserveAlertsUseCaseTest {

    private val hour = 3_600_000L

    private val medication = Medication(
        id = 1,
        name = "Ibuprofeno",
        type = MedicationType.PILL,
        remainingUnits = 10,
        frequency = Frequency(8, FrequencyUnit.HOURS),
        firstIntakeAtMillis = 0L
    )

    @Test
    fun `emite las alertas calculadas con la hora actual`() = runBlocking {
        val useCase = ObserveAlertsUseCase(AlertsFakeRepository(listOf(medication)))

        val alert = useCase(nowMillis = { 10 * hour }).first().single()

        assertEquals(16 * hour, alert.nextAtMillis)
        assertEquals(6 * hour, alert.remainingMillis)
    }

    @Test
    fun `se refresca periódicamente para actualizar el tiempo restante`() = runBlocking {
        val clock = ArrayDeque(listOf(10 * hour, 11 * hour))
        val useCase = ObserveAlertsUseCase(AlertsFakeRepository(listOf(medication)))

        val emissions = useCase(refreshMillis = 10L, nowMillis = { clock.removeFirst() }).take(2).toList()

        assertEquals(listOf(6 * hour, 5 * hour), emissions.map { it.single().remainingMillis })
    }
}
