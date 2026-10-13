package com.mypeals.ui.common

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private data class CounterState(val count: Int = 0, val label: String = "")

private class CounterViewModel : BaseViewModel<CounterState>(CounterState(label = "inicial")) {
    fun increment() = publish { it.copy(count = it.count + 1) }
    fun rename(label: String) = publish { it.copy(label = label) }
    fun snapshot(): CounterState = currentState
}

class BaseViewModelTest {

    @Test
    fun `el estado empieza con el valor inicial`() {
        assertEquals(CounterState(label = "inicial"), CounterViewModel().uiState.value)
    }

    @Test
    fun `publish modifica el estado a partir del actual`() {
        val viewModel = CounterViewModel()

        viewModel.increment()
        viewModel.increment()
        viewModel.rename("dos")

        assertEquals(CounterState(count = 2, label = "dos"), viewModel.uiState.value)
        assertEquals(viewModel.uiState.value, viewModel.snapshot())
    }

    @Test
    fun `no recoge el flujo mientras nadie observa el estado`() = runBlocking {
        val subscribers = MutableStateFlow(0)
        val received = mutableListOf<String>()
        val source = flow { emit("dato"); awaitCancellation() }

        val job = launch { source.whileSubscribed(subscribers, 0).collect { received += it } }
        delay(50)
        assertEquals(emptyList<String>(), received)

        subscribers.value = 1
        delay(50)
        assertEquals(listOf("dato"), received)
        job.cancel()
    }

    @Test
    fun `deja de recoger pasado el timeout y vuelve a empezar con un nuevo observador`() = runBlocking {
        val subscribers = MutableStateFlow(1)
        var starts = 0
        var active = false
        val source = flow {
            starts++
            active = true
            try {
                emit(starts)
                awaitCancellation()
            } finally {
                active = false
            }
        }

        val job = launch { source.whileSubscribed(subscribers, 200).collect {} }
        delay(50)
        assertTrue(active)

        subscribers.value = 0
        delay(50)
        assertTrue("sigue activo durante el timeout", active)
        delay(300)
        assertFalse("se detiene tras el timeout", active)

        subscribers.value = 1
        delay(50)
        assertTrue(active)
        assertEquals(2, starts)
        job.cancel()
    }
}
