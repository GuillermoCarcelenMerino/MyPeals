package com.mypeals.core.domain.model

/**
 * Frecuencia de toma: cada [interval] [unit].
 *
 * [daysOfWeek] solo se usa con [FrequencyUnit.WEEKS]; si está vacío se toma el mismo
 * día de la semana que la primera toma.
 */
data class Frequency(
    val interval: Int,
    val unit: FrequencyUnit,
    val daysOfWeek: Set<Weekday> = emptySet()
)

/**
 * Medicación del usuario.
 *
 * Stock: [remainingUnits] cuenta las unidades que se consumen en cada toma (pastillas,
 * sobres, inyecciones...). Si [unitsPerBox] es null no se conoce el contenido de la caja y
 * cada unidad equivale a una caja entera.
 */
data class Medication(
    val id: Int = 0,
    val name: String,
    val type: MedicationType,
    val unitsPerBox: Int? = null,
    val remainingUnits: Int,
    val dosePerIntake: Int = 1,
    val frequency: Frequency,
    /** Fecha y hora de la primera toma, en milisegundos desde epoch. */
    val firstIntakeAtMillis: Long,
    /** False = tratamiento en pausa: no genera avisos. */
    val active: Boolean = true
)
