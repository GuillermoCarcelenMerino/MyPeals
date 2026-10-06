package com.mypeals.core.data.room.mappers

import com.mypeals.core.data.room.entities.MedicationEntity
import com.mypeals.core.domain.model.Frequency
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.model.Weekday

fun MedicationEntity.toDomain(): Medication = Medication(
    id = id,
    name = name,
    type = type,
    unitsPerBox = unitsPerBox,
    remainingUnits = remainingUnits,
    dosePerIntake = dosePerIntake,
    frequency = Frequency(
        interval = frequencyInterval,
        unit = frequencyUnit,
        daysOfWeek = daysOfWeek.toWeekdays()
    ),
    firstIntakeAtMillis = firstIntakeAt,
    active = active
)

fun Medication.toEntity(): MedicationEntity = MedicationEntity(
    id = id,
    name = name,
    type = type,
    unitsPerBox = unitsPerBox,
    remainingUnits = remainingUnits,
    dosePerIntake = dosePerIntake,
    frequencyInterval = frequency.interval,
    frequencyUnit = frequency.unit,
    daysOfWeek = frequency.daysOfWeek.toMask(),
    firstIntakeAt = firstIntakeAtMillis,
    active = active
)

/** Bit 0 = lunes ... bit 6 = domingo. Null si no hay ningún día. */
internal fun Set<Weekday>.toMask(): Int? =
    if (isEmpty()) null else fold(0) { mask, day -> mask or (1 shl day.ordinal) }

internal fun Int?.toWeekdays(): Set<Weekday> =
    if (this == null) emptySet() else Weekday.entries.filter { this and (1 shl it.ordinal) != 0 }.toSet()
