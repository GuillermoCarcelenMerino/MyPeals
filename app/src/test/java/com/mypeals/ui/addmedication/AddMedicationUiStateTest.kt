package com.mypeals.ui.addmedication

import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.Weekday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class AddMedicationUiStateTest {

    private val utc = TimeZone.getTimeZone("UTC")

    private fun state() = AddMedicationUiState.initial(nowMillis = 0L, timeZone = utc)
        .copy(name = "Ibuprofeno")

    @Test
    fun `sin nombre el formulario no es valido`() {
        val empty = state().copy(name = "  ")
        assertTrue(empty.nameError)
        assertNull(empty.toMedication(utc))
    }

    @Test
    fun `con unidades por caja las unidades restantes son cajas por unidades`() {
        val medication = state().copy(boxes = "3", unitsPerBox = "20").toMedication(utc)

        assertNotNull(medication)
        assertEquals(20, medication!!.unitsPerBox)
        assertEquals(60, medication.remainingUnits)
    }

    @Test
    fun `sin unidades por caja cada unidad es una caja`() {
        val medication = state().copy(boxes = "4", unitsPerBox = "").toMedication(utc)

        assertNotNull(medication)
        assertNull(medication!!.unitsPerBox)
        assertEquals(4, medication.remainingUnits)
    }

    @Test
    fun `los numeros deben ser mayores que cero`() {
        assertTrue(state().copy(boxes = "0").boxesError)
        assertTrue(state().copy(boxes = "").boxesError)
        assertTrue(state().copy(unitsPerBox = "0").unitsPerBoxError)
        assertFalse(state().copy(unitsPerBox = "").unitsPerBoxError)
        assertTrue(state().copy(dosePerIntake = "0").doseError)
        assertTrue(state().copy(frequencyInterval = "0").frequencyIntervalError)
    }

    @Test
    fun `los dias de la semana solo se guardan con frecuencia semanal`() {
        val days = setOf(Weekday.MONDAY, Weekday.FRIDAY)

        val daily = state().copy(frequencyUnit = FrequencyUnit.DAYS, weekdays = days).toMedication(utc)
        val weekly = state().copy(frequencyUnit = FrequencyUnit.WEEKS, weekdays = days).toMedication(utc)

        assertTrue(daily!!.frequency.daysOfWeek.isEmpty())
        assertEquals(days, weekly!!.frequency.daysOfWeek)
    }

    @Test
    fun `el nombre se guarda sin espacios sobrantes`() {
        assertEquals("Ibuprofeno", state().copy(name = "  Ibuprofeno ").toMedication(utc)!!.name)
    }

    @Test
    fun `la fecha del picker se combina con la hora local`() {
        // 2026-10-06 00:00 UTC (lo que devuelve el DatePicker) + 09:30 en Madrid (UTC+2)
        val madrid = TimeZone.getTimeZone("Europe/Madrid")
        val result = combineDateAndTime(1791244800000L, 9, 30, madrid)

        assertEquals(1791271800000L, result) // 2026-10-06 07:30 UTC
    }

    @Test
    fun `la fecha local se convierte a medianoche UTC`() {
        val madrid = TimeZone.getTimeZone("Europe/Madrid")

        // 2026-10-06 07:30 UTC = 09:30 en Madrid, el mismo día
        assertEquals(1791244800000L, localDateToUtcMillis(1791271800000L, madrid))
    }

    @Test
    fun `la primera toma propuesta es la siguiente hora en punto`() {
        // 2026-10-06 07:30 UTC -> propuesta 08:00 UTC del mismo día
        val initial = AddMedicationUiState.initial(nowMillis = 1791271800000L, timeZone = utc)

        assertEquals(8, initial.startHour)
        assertEquals(0, initial.startMinute)
        assertEquals(1791244800000L, initial.startDateUtcMillis)
    }
}
