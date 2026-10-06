package com.mypeals.core.domain.usecase

import com.mypeals.core.domain.model.Frequency
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.IntakeStatus
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.model.MedicationType
import com.mypeals.core.domain.reminder.ReminderScheduler
import com.mypeals.core.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeRepository : MedicationRepository {
    val items = MutableStateFlow<List<Medication>>(emptyList())
    val intakes = mutableListOf<Triple<Int, Long, IntakeStatus>>()
    private var nextId = 1

    override fun observeMedications(): Flow<List<Medication>> = items
    override suspend fun getMedication(id: Int) = items.value.firstOrNull { it.id == id }
    override suspend fun addMedication(medication: Medication): Int {
        val id = nextId++
        items.value = items.value + medication.copy(id = id)
        return id
    }

    override suspend fun updateMedication(medication: Medication) {
        items.value = items.value.map { if (it.id == medication.id) medication else it }
    }

    override suspend fun deleteMedication(medication: Medication) {
        items.value = items.value.filterNot { it.id == medication.id }
    }

    override suspend fun registerIntake(
        medicationId: Int,
        scheduledAtMillis: Long,
        atMillis: Long,
        status: IntakeStatus
    ) {
        intakes += Triple(medicationId, scheduledAtMillis, status)
    }
}

private class FakeScheduler : ReminderScheduler {
    val scheduled = mutableListOf<Medication>()
    val cancelled = mutableListOf<Int>()

    override fun schedule(medication: Medication, afterMillis: Long) {
        scheduled += medication
    }

    override fun scheduleSnooze(medicationId: Int, scheduledAtMillis: Long, triggerAtMillis: Long) = Unit
    override fun cancel(medicationId: Int) {
        cancelled += medicationId
    }
}

class MedicationUseCasesTest {

    private val repository = FakeRepository()
    private val scheduler = FakeScheduler()

    private fun medication(name: String = "Ibuprofeno", active: Boolean = true) = Medication(
        name = name,
        type = MedicationType.entries.first(),
        remainingUnits = 10,
        frequency = Frequency(8, FrequencyUnit.HOURS),
        firstIntakeAtMillis = 0L,
        active = active
    )

    @Test
    fun `añadir programa el aviso con el id asignado`() = runBlocking {
        val id = AddMedicationUseCase(repository, scheduler)(medication()).getOrThrow()

        assertEquals(1, id)
        assertEquals(id, scheduler.scheduled.single().id)
    }

    @Test
    fun `editar guarda y reprograma`() = runBlocking {
        val id = AddMedicationUseCase(repository, scheduler)(medication()).getOrThrow()
        val edited = repository.getMedication(id)!!.copy(name = "Paracetamol")

        UpdateMedicationUseCase(repository, scheduler)(edited).getOrThrow()

        assertEquals("Paracetamol", repository.getMedication(id)!!.name)
        assertEquals("Paracetamol", scheduler.scheduled.last().name)
    }

    @Test
    fun `borrar elimina la medicacion y cancela sus avisos`() = runBlocking {
        val id = AddMedicationUseCase(repository, scheduler)(medication()).getOrThrow()
        val saved = repository.getMedication(id)!!

        DeleteMedicationUseCase(repository, scheduler)(saved).getOrThrow()

        assertTrue(repository.items.value.isEmpty())
        assertEquals(listOf(id), scheduler.cancelled)
    }

    @Test
    fun `reprogramar todo programa cada medicacion`() = runBlocking {
        AddMedicationUseCase(repository, scheduler)(medication("A")).getOrThrow()
        AddMedicationUseCase(repository, scheduler)(medication("B")).getOrThrow()
        scheduler.scheduled.clear()

        ScheduleAllRemindersUseCase(repository, scheduler)().getOrThrow()

        assertEquals(listOf("A", "B"), scheduler.scheduled.map { it.name })
    }

    @Test
    fun `registrar una toma la anota en el repositorio`() = runBlocking {
        RegisterIntakeUseCase(repository)(1, 1000L, IntakeStatus.TAKEN).getOrThrow()

        assertEquals(Triple(1, 1000L, IntakeStatus.TAKEN), repository.intakes.single())
    }
}
