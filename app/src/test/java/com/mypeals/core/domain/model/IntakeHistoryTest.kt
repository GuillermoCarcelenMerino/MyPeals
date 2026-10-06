package com.mypeals.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class IntakeHistoryTest {

    private val utc = TimeZone.getTimeZone("UTC")

    private fun at(day: Int, hour: Int = 0, minute: Int = 0): Long =
        Calendar.getInstance(utc).apply { clear(); set(2026, Calendar.OCTOBER, day, hour, minute, 0) }.timeInMillis

    private fun medication(
        id: Int = 1,
        first: Long,
        interval: Int = 1,
        unit: FrequencyUnit = FrequencyUnit.DAYS,
        active: Boolean = true,
        name: String = "Ibuprofeno"
    ) = Medication(
        id = id,
        name = name,
        type = MedicationType.PILL,
        remainingUnits = 10,
        frequency = Frequency(interval, unit),
        firstIntakeAtMillis = first,
        active = active
    )

    private fun log(medicationId: Int, scheduled: Long, status: IntakeStatus, takenAt: Long? = null) =
        IntakeLog(0, medicationId, scheduled, takenAt, status)

    private fun history(
        meds: List<Medication>,
        logs: List<IntakeLog>,
        now: Long,
        days: Int = 14
    ) = buildIntakeHistory(meds, logs, now, days, utc)

    @Test
    fun `cada dia muestra lo que tocaba y su estado, el mas reciente primero`() {
        val med = medication(first = at(10, 9))
        val logs = listOf(
            log(1, at(10, 9), IntakeStatus.TAKEN, takenAt = at(10, 9, 5)),
            log(1, at(12, 9), IntakeStatus.SKIPPED)
        )

        val result = history(listOf(med), logs, now = at(14, 12))

        assertEquals(listOf(14, 13, 12, 11, 10), result.map { Calendar.getInstance(utc).apply { timeInMillis = it.dayStartMillis }.get(Calendar.DAY_OF_MONTH) })
        val states = result.associate { it.dayStartMillis to it.entries.single().state }
        assertEquals(DoseState.TAKEN, states[at(10)])
        assertEquals(DoseState.MISSED, states[at(11)])
        assertEquals(DoseState.SKIPPED, states[at(12)])
        assertEquals(DoseState.MISSED, states[at(13)])
        assertEquals(DoseState.MISSED, states[at(14)])
    }

    @Test
    fun `una toma recien pasada sin registrar sigue pendiente`() {
        val med = medication(first = at(14, 9))

        val today = history(listOf(med), emptyList(), now = at(14, 9, 10)).single()

        assertEquals(DoseState.PENDING, today.entries.single().state)
    }

    @Test
    fun `las tomas de hoy que aun no han llegado aparecen como pendientes`() {
        val med = medication(first = at(14, 0), interval = 8, unit = FrequencyUnit.HOURS)

        val today = history(listOf(med), emptyList(), now = at(14, 12)).single()

        assertEquals(
            listOf(DoseState.MISSED, DoseState.MISSED, DoseState.PENDING),
            today.entries.map { it.state }
        )
        assertEquals(listOf(at(14, 0), at(14, 8), at(14, 16)), today.entries.map { it.scheduledAtMillis })
    }

    @Test
    fun `tomada gana a pospuesta para la misma toma`() {
        val med = medication(first = at(14, 9))
        val logs = listOf(
            log(1, at(14, 9), IntakeStatus.SNOOZED),
            log(1, at(14, 9), IntakeStatus.TAKEN, takenAt = at(14, 9, 20))
        )

        val entry = history(listOf(med), logs, now = at(14, 12)).single().entries.single()

        assertEquals(DoseState.TAKEN, entry.state)
        assertEquals(at(14, 9, 20), entry.takenAtMillis)
    }

    @Test
    fun `una pospuesta reciente sigue pospuesta y se puede marcar como tomada`() {
        val med = medication(first = at(14, 9))
        val logs = listOf(log(1, at(14, 9), IntakeStatus.SNOOZED))

        val entry = history(listOf(med), logs, now = at(14, 9, 45)).single().entries.single()

        assertEquals(DoseState.SNOOZED, entry.state)
        assertTrue(entry.canMarkTaken)
    }

    @Test
    fun `solo las tomas no tomadas permiten marcarlas a mano`() {
        val med = medication(first = at(13, 9))
        val logs = listOf(log(1, at(13, 9), IntakeStatus.TAKEN, takenAt = at(13, 9, 1)))

        val entries = history(listOf(med), logs, now = at(14, 12)).flatMap { it.entries }

        assertFalse(entries.first { it.scheduledAtMillis == at(13, 9) }.canMarkTaken)
        assertTrue(entries.first { it.scheduledAtMillis == at(14, 9) }.canMarkTaken)
    }

    @Test
    fun `un tratamiento en pausa no genera tomas pero conserva su historial`() {
        val med = medication(first = at(10, 9), active = false)
        val logs = listOf(log(1, at(11, 9), IntakeStatus.TAKEN, takenAt = at(11, 9, 3)))

        val result = history(listOf(med), logs, now = at(14, 12))

        assertEquals(1, result.size)
        assertEquals(DoseState.TAKEN, result.single().entries.single().state)
    }

    @Test
    fun `solo se muestran los dias pedidos`() {
        val med = medication(first = at(1, 9))

        val result = history(listOf(med), emptyList(), now = at(14, 12), days = 3)

        assertEquals(3, result.size)
        assertEquals(at(12), result.last().dayStartMillis)
    }

    @Test
    fun `varias medicaciones el mismo dia van ordenadas por hora`() {
        val a = medication(id = 1, first = at(14, 10), name = "A")
        val b = medication(id = 2, first = at(14, 8), name = "B")

        val entries = history(listOf(a, b), emptyList(), now = at(14, 12)).single().entries

        assertEquals(listOf("B", "A"), entries.map { it.medicationName })
    }

    @Test
    fun `sin medicaciones el historial esta vacio`() {
        assertTrue(history(emptyList(), emptyList(), now = at(14, 12)).isEmpty())
    }
}
