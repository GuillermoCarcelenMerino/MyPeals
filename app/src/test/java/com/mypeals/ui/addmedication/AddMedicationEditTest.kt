package com.mypeals.ui.addmedication

import com.mypeals.core.domain.model.Frequency
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.model.MedicationType
import com.mypeals.core.domain.model.Weekday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class AddMedicationEditTest {

    private val utc = TimeZone.getTimeZone("UTC")

    private val original = Medication(
        id = 7,
        name = "Ibuprofeno",
        type = MedicationType.entries.first(),
        unitsPerBox = 20,
        remainingUnits = 33,
        dosePerIntake = 2,
        frequency = Frequency(2, FrequencyUnit.WEEKS, setOf(Weekday.MONDAY, Weekday.THURSDAY)),
        firstIntakeAtMillis = Calendar.getInstance(utc).apply {
            clear(); set(2026, Calendar.OCTOBER, 12, 9, 30, 0)
        }.timeInMillis,
        active = false
    )

    @Test
    fun `cargar y volver a convertir conserva la medicacion`() {
        val state = AddMedicationUiState.fromMedication(original, utc)

        assertTrue(state.isEditing)
        assertEquals(original, state.toMedication(utc))
    }

    @Test
    fun `al editar no se exige el numero de cajas`() {
        val state = AddMedicationUiState.fromMedication(original, utc).copy(boxes = "")

        assertFalse(state.boxesError)
        assertTrue(state.isValid)
    }

    @Test
    fun `al editar se conserva el stock aunque cambien las unidades por caja`() {
        val edited = AddMedicationUiState.fromMedication(original, utc)
            .copy(unitsPerBox = "30")
            .toMedication(utc)!!

        assertEquals(33, edited.remainingUnits)
        assertEquals(30, edited.unitsPerBox)
    }
}
