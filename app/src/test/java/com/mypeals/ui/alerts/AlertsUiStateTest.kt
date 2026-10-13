package com.mypeals.ui.alerts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertsUiStateTest {

    private val minute = 60_000L

    @Test
    fun `separa el tiempo restante en días, horas y minutos`() {
        assertEquals(RemainingTime(1, 2, 5), remainingTimeOf((24 * 60 + 2 * 60 + 5) * minute))
    }

    @Test
    fun `redondea hacia arriba al minuto`() {
        assertEquals(RemainingTime(0, 0, 1), remainingTimeOf(1_000L))
        assertEquals(RemainingTime(0, 2, 0), remainingTimeOf(119 * minute + 30_000L))
    }

    @Test
    fun `menos de un segundo o tiempo pasado cuenta como ahora`() {
        assertTrue(remainingTimeOf(999L).isNow)
        assertTrue(remainingTimeOf(-5 * minute).isNow)
        assertFalse(remainingTimeOf(minute).isNow)
    }
}
