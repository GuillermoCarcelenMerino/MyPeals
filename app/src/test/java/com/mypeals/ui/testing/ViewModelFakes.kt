package com.mypeals.ui.testing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypeals.core.domain.model.Frequency
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.IntakeLog
import com.mypeals.core.domain.model.IntakeStatus
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.model.MedicationType
import com.mypeals.core.domain.reminder.ReminderScheduler
import com.mypeals.core.domain.repository.MedicationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.concurrent.CopyOnWriteArrayList

/** Repositorio en memoria. Cada operación puede forzarse a fallar con [failWith]. */
class FakeMedicationRepository : MedicationRepository {
    val items = MutableStateFlow<List<Medication>>(emptyList())
    var medicationsFlow: Flow<List<Medication>> = items
    var intakeLogFlow: Flow<List<IntakeLog>> = MutableStateFlow(emptyList())
    var failWith: Exception? = null

    val updated = CopyOnWriteArrayList<Medication>()
    val deleted = CopyOnWriteArrayList<Medication>()
    val intakes = CopyOnWriteArrayList<Triple<Int, Long, IntakeStatus>>()
    @Volatile
    var getCalls = 0
        private set
    private var nextId = 1

    override fun observeMedications(): Flow<List<Medication>> = medicationsFlow
    override fun observeIntakeLog(sinceMillis: Long): Flow<List<IntakeLog>> = intakeLogFlow

    override suspend fun getMedication(id: Int): Medication? {
        getCalls++
        failWith?.let { throw it }
        return items.value.firstOrNull { it.id == id }
    }

    override suspend fun addMedication(medication: Medication): Int {
        failWith?.let { throw it }
        val id = nextId++
        items.value = items.value + medication.copy(id = id)
        return id
    }

    override suspend fun updateMedication(medication: Medication) {
        failWith?.let { throw it }
        updated += medication
        items.value = items.value.map { if (it.id == medication.id) medication else it }
    }

    override suspend fun deleteMedication(medication: Medication) {
        failWith?.let { throw it }
        deleted += medication
        items.value = items.value.filterNot { it.id == medication.id }
    }

    override suspend fun registerIntake(
        medicationId: Int,
        scheduledAtMillis: Long,
        atMillis: Long,
        status: IntakeStatus
    ) {
        failWith?.let { throw it }
        intakes += Triple(medicationId, scheduledAtMillis, status)
    }
}

class FakeReminderScheduler : ReminderScheduler {
    val scheduled = CopyOnWriteArrayList<Medication>()
    val cancelled = CopyOnWriteArrayList<Int>()

    override fun schedule(medication: Medication, afterMillis: Long) {
        scheduled += medication
    }

    override fun scheduleSnooze(medicationId: Int, scheduledAtMillis: Long, triggerAtMillis: Long) = Unit

    override fun cancel(medicationId: Int) {
        cancelled += medicationId
    }
}

fun medication(
    id: Int = 0,
    name: String = "Ibuprofeno",
    remainingUnits: Int = 10,
    active: Boolean = true
) = Medication(
    id = id,
    name = name,
    type = MedicationType.PILL,
    remainingUnits = remainingUnits,
    frequency = Frequency(8, FrequencyUnit.HOURS),
    firstIntakeAtMillis = 0L,
    active = active
)

/**
 * Espera a que se cumpla [condition], por si alguna parte del flujo termina en otro hilo.
 */
suspend fun awaitUntil(timeoutMillis: Long = 2_000, condition: () -> Boolean) {
    withTimeout(timeoutMillis) {
        while (!condition()) delay(5)
    }
}

/** Simula a la pantalla observando el estado, para que `observe` empiece a recoger. */
fun <S> CoroutineScope.subscribe(state: StateFlow<S>): Job = launch { state.collect {} }

/** Cancela las corrutinas del ViewModel al terminar el test. */
fun ViewModel.close() = viewModelScope.cancel()
