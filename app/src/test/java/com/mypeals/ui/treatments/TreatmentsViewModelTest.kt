package com.mypeals.ui.treatments

import com.mypeals.core.domain.usecase.DeleteMedicationUseCase
import com.mypeals.core.domain.usecase.ObserveMedicationsUseCase
import com.mypeals.core.domain.usecase.UpdateMedicationUseCase
import com.mypeals.ui.testing.FakeMedicationRepository
import com.mypeals.ui.testing.MainDispatcherRule
import com.mypeals.ui.testing.FakeReminderScheduler
import com.mypeals.ui.testing.awaitUntil
import com.mypeals.ui.testing.close
import com.mypeals.ui.testing.medication
import com.mypeals.ui.testing.subscribe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TreatmentsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMedicationRepository()
    private val scheduler = FakeReminderScheduler()
    private var viewModel: TreatmentsViewModel? = null

    private fun viewModel() = TreatmentsViewModel(
        ObserveMedicationsUseCase(repository),
        UpdateMedicationUseCase(repository, scheduler),
        DeleteMedicationUseCase(repository, scheduler)
    ).also { viewModel = it }

    @After
    fun tearDown() {
        viewModel?.close()
    }

    @Test
    fun `empieza cargando`() {
        assertTrue(viewModel().uiState.value.loading)
    }

    @Test
    fun `publica las medicaciones al observar la pantalla y sigue sus cambios`() = runBlocking {
        val ibuprofeno = medication(id = 1, name = "Ibuprofeno")
        repository.items.value = listOf(ibuprofeno)
        val viewModel = viewModel()
        val screen = subscribe(viewModel.uiState)

        awaitUntil { !viewModel.uiState.value.loading }
        assertEquals(listOf(ibuprofeno), viewModel.uiState.value.medications)

        val omeprazol = medication(id = 2, name = "Omeprazol")
        repository.items.value = listOf(ibuprofeno, omeprazol)
        awaitUntil { viewModel.uiState.value.medications.size == 2 }
        screen.cancel()
    }

    @Test
    fun `si falla la observacion deja de cargar sin medicaciones`() = runBlocking {
        repository.medicationsFlow = flow { throw IllegalStateException("sin base de datos") }
        val viewModel = viewModel()
        val screen = subscribe(viewModel.uiState)

        awaitUntil { !viewModel.uiState.value.loading }

        assertTrue(viewModel.uiState.value.medications.isEmpty())
        screen.cancel()
    }

    @Test
    fun `actualizar la cantidad guarda las unidades restantes`() = runBlocking {
        viewModel().updateQuantity(medication(id = 1, remainingUnits = 10), 4)

        awaitUntil { repository.updated.isNotEmpty() }
        assertEquals(4, repository.updated.single().remainingUnits)
        assertEquals(4, scheduler.scheduled.single().remainingUnits)
    }

    @Test
    fun `la cantidad nunca baja de cero`() = runBlocking {
        viewModel().updateQuantity(medication(id = 1), -3)

        awaitUntil { repository.updated.isNotEmpty() }
        assertEquals(0, repository.updated.single().remainingUnits)
    }

    @Test
    fun `pausar el tratamiento lo guarda como inactivo`() = runBlocking {
        viewModel().setActive(medication(id = 1, active = true), false)

        awaitUntil { repository.updated.isNotEmpty() }
        assertFalse(repository.updated.single().active)
    }

    @Test
    fun `reanudar el tratamiento lo guarda como activo`() = runBlocking {
        viewModel().setActive(medication(id = 1, active = false), true)

        awaitUntil { repository.updated.isNotEmpty() }
        assertTrue(repository.updated.single().active)
    }

    @Test
    fun `borrar elimina la medicacion y cancela sus avisos`() = runBlocking {
        val ibuprofeno = medication(id = 1)
        repository.items.value = listOf(ibuprofeno)

        viewModel().delete(ibuprofeno)

        awaitUntil { repository.deleted.isNotEmpty() }
        assertTrue(repository.items.value.isEmpty())
        assertEquals(listOf(1), scheduler.cancelled)
    }

    @Test
    fun `si fallan las operaciones no cambia nada`() = runBlocking {
        repository.failWith = IllegalStateException("sin base de datos")
        val viewModel = viewModel()
        val ibuprofeno = medication(id = 1)

        viewModel.updateQuantity(ibuprofeno, 2)
        viewModel.setActive(ibuprofeno, false)
        viewModel.delete(ibuprofeno)
        delay(100)

        assertTrue(repository.updated.isEmpty())
        assertTrue(repository.deleted.isEmpty())
        assertTrue(scheduler.scheduled.isEmpty())
        assertTrue(scheduler.cancelled.isEmpty())
    }
}
