package com.mypeals.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.TimeZone

class AlertsTest {

    private val utc = TimeZone.getTimeZone("UTC")
    private val hour = 3_600_000L

    private fun medication(
        id: Int,
        name: String,
        firstIntake: Long = 0L,
        everyHours: Int = 8,
        active: Boolean = true
    ) = Medication(
        id = id,
        name = name,
        type = MedicationType.PILL,
        remainingUnits = 10,
        dosePerIntake = 2,
        frequency = Frequency(everyHours, FrequencyUnit.HOURS),
        firstIntakeAtMillis = firstIntake,
        active = active
    )

    @Test
    fun `la próxima alerta y el tiempo restante salen de la pauta`() {
        val now = 10 * hour
        val alert = buildAlerts(listOf(medication(1, "Ibuprofeno")), now, utc).single()

        assertEquals(AlertStatus.SCHEDULED, alert.status)
        assertEquals(16 * hour, alert.nextAtMillis)
        assertEquals(6 * hour, alert.remainingMillis)
        assertEquals(2, alert.dose)
    }

    @Test
    fun `si la primera toma es futura la próxima alerta es la primera toma`() {
        val alert = buildAlerts(listOf(medication(1, "Ibuprofeno", firstIntake = 50 * hour)), 10 * hour, utc).single()

        assertEquals(50 * hour, alert.nextAtMillis)
        assertEquals(40 * hour, alert.remainingMillis)
    }

    @Test
    fun `en la hora exacta de una toma la próxima alerta es la siguiente`() {
        val alert = buildAlerts(listOf(medication(1, "Ibuprofeno")), 8 * hour, utc).single()

        assertEquals(16 * hour, alert.nextAtMillis)
    }

    @Test
    fun `un tratamiento en pausa no tiene alerta pendiente`() {
        val alert = buildAlerts(listOf(medication(1, "Ibuprofeno", active = false)), 10 * hour, utc).single()

        assertEquals(AlertStatus.PAUSED, alert.status)
        assertNull(alert.nextAtMillis)
        assertNull(alert.remainingMillis)
    }

    @Test
    fun `las programadas van primero por cercanía y el resto por nombre`() {
        val alerts = buildAlerts(
            listOf(
                medication(1, "Zinc", active = false),
                medication(2, "Omeprazol", everyHours = 24),
                medication(3, "Amoxicilina", active = false),
                medication(4, "Ibuprofeno", everyHours = 4)
            ),
            nowMillis = 10 * hour,
            timeZone = utc
        )

        assertEquals(listOf("Ibuprofeno", "Omeprazol", "Amoxicilina", "Zinc"), alerts.map { it.medicationName })
    }

    @Test
    fun `sin medicación no hay alertas`() {
        assertEquals(emptyList<Alert>(), buildAlerts(emptyList(), 0L, utc))
    }
}
