package com.mypeals.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StockTest {

    private fun medication(
        remaining: Int,
        unitsPerBox: Int? = null,
        dose: Int = 1,
        frequency: Frequency = Frequency(interval = 1, unit = FrequencyUnit.DAYS),
        active: Boolean = true
    ) = Medication(
        name = "Test",
        type = MedicationType.PILL,
        unitsPerBox = unitsPerBox,
        remainingUnits = remaining,
        dosePerIntake = dose,
        frequency = frequency,
        firstIntakeAtMillis = 0L,
        active = active
    )

    // ---------- estado del stock ----------

    @Test
    fun `sin stock`() {
        assertEquals(StockStatus.Empty, medication(remaining = 0, unitsPerBox = 20).stockStatus())
        assertEquals(StockStatus.Empty, medication(remaining = 0).stockStatus())
    }

    @Test
    fun `sin unidades por caja solo se cuentan cajas`() {
        assertEquals(StockStatus.BoxesOnly(3), medication(remaining = 3).stockStatus())
    }

    @Test
    fun `en la ultima caja solo se muestran las unidades`() {
        assertEquals(StockStatus.LastBox(20), medication(remaining = 20, unitsPerBox = 20).stockStatus())
        assertEquals(StockStatus.LastBox(5), medication(remaining = 5, unitsPerBox = 20).stockStatus())
    }

    @Test
    fun `con varias cajas se muestran cajas y unidades de la actual`() {
        assertEquals(StockStatus.MultipleBoxes(3, 20), medication(remaining = 60, unitsPerBox = 20).stockStatus())
        assertEquals(StockStatus.MultipleBoxes(3, 5), medication(remaining = 45, unitsPerBox = 20).stockStatus())
        assertEquals(StockStatus.MultipleBoxes(2, 1), medication(remaining = 21, unitsPerBox = 20).stockStatus())
    }

    // ---------- frecuencia y dias de stock ----------

    @Test
    fun `tomas al dia segun la frecuencia`() {
        assertEquals(3.0, Frequency(8, FrequencyUnit.HOURS).intakesPerDay(), 0.0001)
        assertEquals(1.0, Frequency(1, FrequencyUnit.DAYS).intakesPerDay(), 0.0001)
        assertEquals(0.5, Frequency(2, FrequencyUnit.DAYS).intakesPerDay(), 0.0001)
        assertEquals(
            2.0 / 7.0,
            Frequency(1, FrequencyUnit.WEEKS, setOf(Weekday.MONDAY, Weekday.THURSDAY)).intakesPerDay(),
            0.0001
        )
        // Semanal sin dias concretos: una toma por semana
        assertEquals(1.0 / 7.0, Frequency(1, FrequencyUnit.WEEKS).intakesPerDay(), 0.0001)
    }

    @Test
    fun `dias de stock estimados`() {
        val every8h = Frequency(8, FrequencyUnit.HOURS)
        val days = medication(remaining = 14, frequency = every8h).estimatedDaysLeft()

        assertNotNull(days)
        assertEquals(14.0 / 3.0, days!!, 0.0001)
        // La dosis multiplica el consumo
        assertEquals(7.0, medication(remaining = 14, dose = 2).estimatedDaysLeft()!!, 0.0001)
    }

    // ---------- stock bajo ----------

    @Test
    fun `stock bajo cuando cubre menos de siete dias`() {
        assertTrue(medication(remaining = 13, dose = 2).isStockLow()) // 6,5 dias
        assertFalse(medication(remaining = 14, dose = 2).isStockLow()) // 7 dias exactos
        assertFalse(medication(remaining = 30).isStockLow())
    }

    @Test
    fun `las medicaciones en pausa o sin stock no se marcan como stock bajo`() {
        assertFalse(medication(remaining = 2, active = false).isStockLow())
        assertFalse(medication(remaining = 0).isStockLow())
    }
}
