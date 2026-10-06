package com.mypeals.core.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.MedicationType

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: MedicationType,
    @ColumnInfo(name = "units_per_box") val unitsPerBox: Int?,
    @ColumnInfo(name = "remaining_units") val remainingUnits: Int,
    @ColumnInfo(name = "dose_per_intake") val dosePerIntake: Int,
    @ColumnInfo(name = "frequency_interval") val frequencyInterval: Int,
    @ColumnInfo(name = "frequency_unit") val frequencyUnit: FrequencyUnit,
    /** Máscara de bits (bit 0 = lunes ... bit 6 = domingo). Null = sin días concretos. */
    @ColumnInfo(name = "days_of_week") val daysOfWeek: Int?,
    @ColumnInfo(name = "first_intake_at") val firstIntakeAt: Long,
    @ColumnInfo(name = "active") val active: Boolean
)
