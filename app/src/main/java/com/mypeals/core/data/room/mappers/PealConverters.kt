package com.mypeals.core.data.room.mappers

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.mypeals.core.data.room.entities.PealType

@ProvidedTypeConverter
class PealConverters {
    @TypeConverter
    fun fromPealType(type: PealType) = type.name

    @TypeConverter
    fun toPealType(type: String): PealType = PealType.valueOf(type)
}