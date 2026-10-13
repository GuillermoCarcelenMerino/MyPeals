package com.mypeals.ui.alerts

import com.mypeals.core.domain.usecase.ObserveAlertsUseCase
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

class AlertsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMedicationRepository()
    private var viewModel: AlertsViewModel? = null

    private fun viewModel() = AlertsViewModel(ObserveAlertsUseCase(repository)).also { viewModel = it }

    @After
    fun tearDown() {
        viewModel?.close()
    }

    @Test
    fun `empieza cargando`() {
        assertTrue(viewModel().uiState.value.loading)
    }

    @Test
    fun `no observa las alertas mientras la pantalla no esta visible`() = runBlocking {
        repository.items.value = listOf(medication(id = 1))
        val viewModel = viewModel()

        delay(100)

        assertTrue(viewModel.uiState.value.loading)
        assertTrue(viewModel.uiState.value.alerts.isEmpty())
    }

    @Test
    fun `publica una alerta por medicacion al observar la pantalla`() = runBlocking {
        repository.items.value = listOf(medication(id = 1, name = "A"), medication(id = 2, name = "B"))
        val viewModel = viewModel()
        val screen = subscribe(viewModel.uiState)

        awaitUntil { !viewModel.uiState.value.loading }

        assertEquals(setOf(1, 2), viewModel.uiState.value.alerts.map { it.medicationId }.toSet())
        screen.cancel()
    }

    @Test
    fun `se actualiza cuando cambian las medicaciones`() = runBlocking {
        val viewModel = viewModel()
        val screen = subscribe(viewModel.uiState)
        awaitUntil { !viewModel.uiState.value.loading }
        assertTrue(viewModel.uiState.value.alerts.isEmpty())

        repository.items.value = listOf(medication(id = 3))

        awaitUntil { viewModel.uiState.value.alerts.size == 1 }
        screen.cancel()
    }

    @Test
    fun `si falla la observacion deja de cargar sin alertas`() = runBlocking {
        repository.medicationsFlow = flow { throw IllegalStateException("sin base de datos") }
        val viewModel = viewModel()
        val screen = subscribe(viewModel.uiState)

        awaitUntil { !viewModel.uiState.value.loading }

        assertTrue(viewModel.uiState.value.alerts.isEmpty())
        screen.cancel()
    }
}
