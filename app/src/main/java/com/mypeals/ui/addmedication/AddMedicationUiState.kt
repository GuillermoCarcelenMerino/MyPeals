package com.mypeals.ui.addmedication

import com.mypeals.core.domain.model.Frequency
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.model.MedicationType
import com.mypeals.core.domain.model.Weekday
import java.util.Calendar
import java.util.TimeZone

/**
 * Estado del formulario de alta. Los campos numéricos se guardan como texto para poder
 * mostrar exactamente lo que escribe el usuario; se validan al guardar.
 */
data class AddMedicationUiState(
    val name: String = "",
    val type: MedicationType = MedicationType.PILL,
    val boxes: String = "1",
    /** Opcional: vacío = no se conoce el contenido de la caja. */
    val unitsPerBox: String = "",
    val dosePerIntake: String = "1",
    val frequencyInterval: String = "1",
    val frequencyUnit: FrequencyUnit = FrequencyUnit.DAYS,
    val weekdays: Set<Weekday> = emptySet(),
    /** Fecha de la primera toma como medianoche UTC (formato del DatePicker). */
    val startDateUtcMillis: Long,
    val startHour: Int,
    val startMinute: Int,
    /** Los errores solo se muestran tras el primer intento de guardar. */
    val showErrors: Boolean = false,
    val saving: Boolean = false,
    val saveFailed: Boolean = false
) {
    val nameError: Boolean get() = name.isBlank()
    val boxesError: Boolean get() = boxes.toPositiveIntOrNull() == null
    val unitsPerBoxError: Boolean
        get() = unitsPerBox.isNotEmpty() && unitsPerBox.toPositiveIntOrNull() == null
    val doseError: Boolean get() = dosePerIntake.toPositiveIntOrNull() == null
    val frequencyIntervalError: Boolean get() = frequencyInterval.toPositiveIntOrNull() == null

    val isValid: Boolean
        get() = !(nameError || boxesError || unitsPerBoxError || doseError || frequencyIntervalError)

    /** Devuelve la medicación lista para guardar, o null si el formulario no es válido. */
    fun toMedication(timeZone: TimeZone = TimeZone.getDefault()): Medication? {
        if (!isValid) return null
        val boxCount = boxes.toPositiveIntOrNull() ?: return null
        val perBox = unitsPerBox.toPositiveIntOrNull()
        return Medication(
            name = name.trim(),
            type = type,
            unitsPerBox = perBox,
            // Sin unidades por caja, cada unidad equivale a una caja entera.
            remainingUnits = boxCount * (perBox ?: 1),
            dosePerIntake = dosePerIntake.toPositiveIntOrNull() ?: return null,
            frequency = Frequency(
                interval = frequencyInterval.toPositiveIntOrNull() ?: return null,
                unit = frequencyUnit,
                daysOfWeek = if (frequencyUnit == FrequencyUnit.WEEKS) weekdays else emptySet()
            ),
            firstIntakeAtMillis = combineDateAndTime(startDateUtcMillis, startHour, startMinute, timeZone)
        )
    }

    companion object {
        /** Formulario vacío con la primera toma propuesta a la siguiente hora en punto. */
        fun initial(
            nowMillis: Long = System.currentTimeMillis(),
            timeZone: TimeZone = TimeZone.getDefault()
        ): AddMedicationUiState {
            val next = Calendar.getInstance(timeZone).apply {
                timeInMillis = nowMillis
                add(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return AddMedicationUiState(
                startDateUtcMillis = localDateToUtcMillis(next.timeInMillis, timeZone),
                startHour = next.get(Calendar.HOUR_OF_DAY),
                startMinute = 0
            )
        }
    }
}

private fun String.toPositiveIntOrNull(): Int? = toIntOrNull()?.takeIf { it > 0 }
