package com.mypeals.core.data.room.mappers

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.mypeals.core.data.room.entities.MedicationType

@ProvidedTypeConverter
class MedicationConverters {
    @TypeConverter
    fun fromMedicationType(type: MedicationType): String = type.name

    @TypeConverter
    fun toMedicationType(type: String): MedicationType = MedicationType.valueOf(type)
}
