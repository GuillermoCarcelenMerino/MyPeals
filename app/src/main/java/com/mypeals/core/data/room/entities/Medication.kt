package com.mypeals.core.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// El nombre de la tabla y de las columnas se mantiene ("peals", "peal_name") para
// no romper la base de datos ya existente. El modelo se rediseñará en la Fase 2.
@Entity(tableName = "peals")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "peal_name") val name: String,
    @ColumnInfo(name = "quantity") val quantity: Int,
    @ColumnInfo(name = "type") val type: MedicationType
)

// Los valores se guardan por nombre en la base de datos: no renombrarlos sin migración.
enum class MedicationType {
    INJECTION,
    PEAL
}
