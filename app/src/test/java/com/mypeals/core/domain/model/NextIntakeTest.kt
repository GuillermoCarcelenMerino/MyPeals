package com.mypeals.core.domain.model

import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class NextIntakeTest {

    private val utc = TimeZone.getTimeZone("UTC")
    private val madrid = TimeZone.getTimeZone("Europe/Madrid")

    private fun millis(
        y: Int, m: Int, d: Int, h: Int = 0, min: Int = 0, tz: TimeZone = utc
    ): Long = Calendar.getInstance(tz).apply {
        clear(); set(y, m - 1, d, h, min, 0)
    }.timeInMillis

    private fun med(
        first: Long,
        interval: Int,
        unit: FrequencyUnit,
        days: Set<Weekday> = emptySet(),
        active: Boolean = true
    ) = Medication(
        name = "Test",
        type = MedicationType.entries.first(),
        remainingUnits = 10,
        frequency = Frequency(interval, unit, days),
        firstIntakeAtMillis = first,
        active = active
    )

    @Test
    fun hours_beforeFirstIntake_returnsFirst() {
        val first = millis(2026, 10, 10, 8)
        val m = med(first, 8, FrequencyUnit.HOURS)
        assertEquals(first, m.nextIntakeAfter(millis(2026, 10, 1), utc))
    }

    @Test
    fun hours_afterFirst_returnsNextStep() {
        val first = millis(2026, 10, 10, 8)
        val m = med(first, 8, FrequencyUnit.HOURS)
        assertEquals(millis(2026, 10, 10, 16), m.nextIntakeAfter(first, utc))
        assertEquals(millis(2026, 10, 11, 0), m.nextIntakeAfter(millis(2026, 10, 10, 16), utc))
        assertEquals(millis(2026, 10, 10, 16), m.nextIntakeAfter(millis(2026, 10, 10, 9), utc))
    }

    @Test
    fun days_everyTwoDays() {
        val first = millis(2026, 10, 10, 9)
        val m = med(first, 2, FrequencyUnit.DAYS)
        assertEquals(millis(2026, 10, 12, 9), m.nextIntakeAfter(first, utc))
        assertEquals(millis(2026, 10, 12, 9), m.nextIntakeAfter(millis(2026, 10, 11, 12), utc))
    }

    @Test
    fun days_keepsLocalTimeAcrossDst() {
        // El cambio de hora en Madrid fue el 25/10/2026: las 09:00 siguen siendo las 09:00.
        val first = millis(2026, 10, 24, 9, tz = madrid)
        val m = med(first, 1, FrequencyUnit.DAYS)
        assertEquals(millis(2026, 10, 26, 9, tz = madrid), m.nextIntakeAfter(millis(2026, 10, 25, 9, tz = madrid), madrid))
    }

    @Test
    fun weeks_selectedDays() {
        // 12/10/2026 es lunes.
        val first = millis(2026, 10, 12, 10)
        val m = med(first, 1, FrequencyUnit.WEEKS, setOf(Weekday.MONDAY, Weekday.THURSDAY))
        assertEquals(millis(2026, 10, 15, 10), m.nextIntakeAfter(first, utc))
        assertEquals(millis(2026, 10, 19, 10), m.nextIntakeAfter(millis(2026, 10, 15, 10), utc))
    }

    @Test
    fun weeks_everyTwoWeeks() {
        val first = millis(2026, 10, 12, 10)
        val m = med(first, 2, FrequencyUnit.WEEKS, setOf(Weekday.MONDAY))
        assertEquals(millis(2026, 10, 26, 10), m.nextIntakeAfter(first, utc))
    }

    @Test
    fun weeks_withoutDays_usesFirstWeekday() {
        val first = millis(2026, 10, 14, 10) // miércoles
        val m = med(first, 1, FrequencyUnit.WEEKS)
        assertEquals(millis(2026, 10, 21, 10), m.nextIntakeAfter(first, utc))
    }
}
