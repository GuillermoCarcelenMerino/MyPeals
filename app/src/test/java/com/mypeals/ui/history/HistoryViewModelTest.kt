package com.mypeals.ui.history

import com.mypeals.core.domain.model.DoseState
import com.mypeals.core.domain.model.HistoryEntry
import com.mypeals.core.domain.model.IntakeStatus
import com.mypeals.core.domain.usecase.ObserveIntakeHistoryUseCase
import com.mypeals.core.domain.usecase.RegisterIntakeUseCase
import com.mypeals.ui.testing.FakeMedicationRepository
import com.mypeals.ui.testing.MainDispatcherRule
import com.mypeals.ui.testing.awaitUntil
import com.mypeals.ui.testing.close
import com.mypeals.ui.testing.medication
import com.mypeals.ui.testing.subscribe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMedicationRepository()
    private var viewModel: HistoryViewModel? = null

    private fun viewModel() = HistoryViewModel(
        ObserveIntakeHistoryUseCase(repository),
        RegisterIntakeUseCase(repository)
    ).also { viewModel = it }

    private val entry = HistoryEntry(
        medicationId = 5,
        medicationName = "Ibuprofeno",
        dose = 1,
        scheduledAtMillis = 1_000L,
        takenAtMillis = null,
        state = DoseState.MISSED
    )

    @After
    fun tearDown() {
        viewModel?.close()
    }

    @Test
    fun `empieza cargando`() {
        assertTrue(viewModel().uiState.value.loading)
    }

    @Test
    fun `publica los dias del historial al observar la pantalla`() = runBlocking {
        // Una toma cada 8 horas desde epoch: hoy siempre tiene alguna.
        repository.items.value = listOf(medication(id = 1))
        val viewModel = viewModel()
        val screen = subscribe(viewModel.uiState)

        awaitUntil { !viewModel.uiState.value.loading }

        val days = viewModel.uiState.value.days
        assertTrue(days.isNotEmpty())
        assertTrue(days.flatMap { it.entries }.all { it.medicationId == 1 })
        screen.cancel()
    }

    @Test
    fun `si falla el historial deja de cargar sin dias`() = runBlocking {
        repository.intakeLogFlow = flow { throw IllegalStateException("sin base de datos") }
        val viewModel = viewModel()
        val screen = subscribe(viewModel.uiState)

        awaitUntil { !viewModel.uiState.value.loading }

        assertTrue(viewModel.uiState.value.days.isEmpty())
        screen.cancel()
    }

    @Test
    fun `marcar como tomada registra la toma programada`() = runBlocking {
        val viewModel = viewModel()

        viewModel.markTaken(entry)
        awaitUntil { repository.intakes.isNotEmpty() }

        assertEquals(Triple(5, 1_000L, IntakeStatus.TAKEN), repository.intakes.single())
    }

    @Test
    fun `si falla marcar como tomada no cambia el estado`() = runBlocking {
        repository.failWith = IllegalStateException("sin base de datos")
        val viewModel = viewModel()
        val before = viewModel.uiState.value

        viewModel.markTaken(entry)
        delay(100)

        assertTrue(repository.intakes.isEmpty())
        assertEquals(before, viewModel.uiState.value)
    }
}
