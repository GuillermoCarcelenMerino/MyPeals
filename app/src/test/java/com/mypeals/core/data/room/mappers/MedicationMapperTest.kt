package com.mypeals.core.data.room.mappers

import com.mypeals.core.domain.model.Frequency
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.model.MedicationType
import com.mypeals.core.domain.model.Weekday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MedicationMapperTest {

    @Test
    fun `entidad y dominio se convierten sin perder datos`() {
        val medication = Medication(
            id = 7,
            name = "Ibuprofeno",
            type = MedicationType.TABLET,
            unitsPerBox = 20,
            remainingUnits = 35,
            dosePerIntake = 2,
            frequency = Frequency(
                interval = 1,
                unit = FrequencyUnit.WEEKS,
                daysOfWeek = setOf(Weekday.MONDAY, Weekday.THURSDAY, Weekday.SUNDAY)
            ),
            firstIntakeAtMillis = 1_700_000_000_000L,
            active = false
        )

        assertEquals(medication, medication.toEntity().toDomain())
    }

    @Test
    fun `sin dias de la semana la mascara es nula`() {
        val entity = Medication(
            name = "Insulina",
            type = MedicationType.INJECTION,
            remainingUnits = 3,
            frequency = Frequency(interval = 8, unit = FrequencyUnit.HOURS),
            firstIntakeAtMillis = 0L
        ).toEntity()

        assertNull(entity.daysOfWeek)
    }

    @Test
    fun `la mascara usa un bit por dia empezando en lunes`() {
        assertEquals(0b0000001, setOf(Weekday.MONDAY).toMask())
        assertEquals(0b1000001, setOf(Weekday.MONDAY, Weekday.SUNDAY).toMask())
        assertEquals(setOf(Weekday.TUESDAY, Weekday.FRIDAY), 0b0010010.toWeekdays())
    }
}
