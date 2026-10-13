package com.mypeals.ui.addmedication

import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.MedicationType
import com.mypeals.core.domain.model.Weekday
import com.mypeals.core.domain.usecase.AddMedicationUseCase
import com.mypeals.core.domain.usecase.GetMedicationUseCase
import com.mypeals.core.domain.usecase.UpdateMedicationUseCase
import com.mypeals.ui.testing.FakeMedicationRepository
import com.mypeals.ui.testing.MainDispatcherRule
import com.mypeals.ui.testing.FakeReminderScheduler
import com.mypeals.ui.testing.awaitUntil
import com.mypeals.ui.testing.close
import com.mypeals.ui.testing.medication
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class AddMedicationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMedicationRepository()
    private val scheduler = FakeReminderScheduler()
    private val viewModel = AddMedicationViewModel(
        AddMedicationUseCase(repository, scheduler),
        UpdateMedicationUseCase(repository, scheduler),
        GetMedicationUseCase(repository)
    )
    private val savedCount = AtomicInteger()
    private val onSaved: () -> Unit = { savedCount.incrementAndGet() }

    private val state get() = viewModel.uiState.value

    @After
    fun tearDown() = viewModel.close()

    private fun fillValidForm() {
        viewModel.onNameChange("Paracetamol")
        viewModel.onBoxesChange("2")
        viewModel.onUnitsPerBoxChange("10")
        viewModel.onDoseChange("1")
        viewModel.onFrequencyIntervalChange("8")
        viewModel.onFrequencyUnitChange(FrequencyUnit.HOURS)
    }

    @Test
    fun `empieza con el formulario vacio y sin editar`() {
        assertEquals("", state.name)
        assertFalse(state.isEditing)
        assertFalse(state.showErrors)
        assertFalse(state.saving)
    }

    @Test
    fun `cada campo del formulario actualiza el estado`() {
        viewModel.onNameChange("Ibuprofeno")
        viewModel.onTypeChange(MedicationType.entries.last())
        viewModel.onBoxesChange("3")
        viewModel.onUnitsPerBoxChange("20")
        viewModel.onDoseChange("2")
        viewModel.onFrequencyIntervalChange("12")
        viewModel.onFrequencyUnitChange(FrequencyUnit.WEEKS)
        viewModel.onStartDateChange(1_000L)
        viewModel.onStartTimeChange(9, 45)

        assertEquals("Ibuprofeno", state.name)
        assertEquals(MedicationType.entries.last(), state.type)
        assertEquals("3", state.boxes)
        assertEquals("20", state.unitsPerBox)
        assertEquals("2", state.dosePerIntake)
        assertEquals("12", state.frequencyInterval)
        assertEquals(FrequencyUnit.WEEKS, state.frequencyUnit)
        assertEquals(1_000L, state.startDateUtcMillis)
        assertEquals(9, state.startHour)
        assertEquals(45, state.startMinute)
    }

    @Test
    fun `marcar un dia de la semana lo añade y volver a marcarlo lo quita`() {
        viewModel.onWeekdayToggle(Weekday.MONDAY)
        viewModel.onWeekdayToggle(Weekday.FRIDAY)
        assertEquals(setOf(Weekday.MONDAY, Weekday.FRIDAY), state.weekdays)

        viewModel.onWeekdayToggle(Weekday.MONDAY)
        assertEquals(setOf(Weekday.FRIDAY), state.weekdays)
    }

    @Test
    fun `guardar un formulario invalido solo muestra los errores`() {
        viewModel.save(onSaved)

        assertTrue(state.showErrors)
        assertFalse(state.saving)
        assertEquals(0, savedCount.get())
        assertTrue(repository.items.value.isEmpty())
    }

    @Test
    fun `guardar una medicacion nueva la añade, programa el aviso y avisa a la pantalla`() = runBlocking {
        fillValidForm()

        viewModel.save(onSaved)
        assertTrue(state.saving)
        awaitUntil { savedCount.get() == 1 }

        val saved = repository.items.value.single()
        assertEquals("Paracetamol", saved.name)
        assertEquals(20, saved.remainingUnits)
        assertEquals(saved.id, scheduler.scheduled.single().id)
    }

    @Test
    fun `mientras se guarda ignora nuevas pulsaciones`() = runBlocking {
        fillValidForm()

        viewModel.save(onSaved)
        viewModel.save(onSaved)
        awaitUntil { savedCount.get() >= 1 }

        assertEquals(1, savedCount.get())
        assertEquals(1, repository.items.value.size)
    }

    @Test
    fun `si falla el guardado deja de guardar y marca el error`() = runBlocking {
        fillValidForm()
        repository.failWith = IllegalStateException("disco lleno")

        viewModel.save(onSaved)
        awaitUntil { state.saveFailed }

        assertFalse(state.saving)
        assertEquals(0, savedCount.get())
    }

    @Test
    fun `cargar una medicacion rellena el formulario en modo edicion`() = runBlocking {
        repository.items.value = listOf(medication(id = 4, name = "Omeprazol", remainingUnits = 7))

        viewModel.load(4)
        awaitUntil { state.isEditing }

        assertEquals(4, state.editingId)
        assertEquals("Omeprazol", state.name)
        assertEquals(7, state.editingRemainingUnits)
        assertFalse(state.loadFailed)
    }

    @Test
    fun `cargar el mismo id dos veces solo consulta una vez`() = runBlocking {
        repository.items.value = listOf(medication(id = 4))

        viewModel.load(4)
        awaitUntil { state.isEditing }
        viewModel.onNameChange("Cambiado")
        viewModel.load(4)

        assertEquals(1, repository.getCalls)
        assertEquals("Cambiado", state.name)
    }

    @Test
    fun `cargar una medicacion que no existe marca el fallo de carga`() = runBlocking {
        viewModel.load(99)
        awaitUntil { state.loadFailed }

        assertFalse(state.isEditing)
    }

    @Test
    fun `si falla la carga marca el fallo de carga`() = runBlocking {
        repository.failWith = IllegalStateException("sin base de datos")

        viewModel.load(4)
        awaitUntil { state.loadFailed }

        assertFalse(state.isEditing)
    }

    @Test
    fun `guardar en modo edicion actualiza la medicacion conservando su stock`() = runBlocking {
        repository.items.value = listOf(medication(id = 4, name = "Omeprazol", remainingUnits = 7))
        viewModel.load(4)
        awaitUntil { state.isEditing }

        viewModel.onNameChange("Omeprazol 20mg")
        viewModel.save(onSaved)
        awaitUntil { savedCount.get() == 1 }

        val updated = repository.updated.single()
        assertEquals(4, updated.id)
        assertEquals("Omeprazol 20mg", updated.name)
        assertEquals(7, updated.remainingUnits)
        assertEquals(updated, scheduler.scheduled.single())
    }
}
