package com.mypeals.core.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "peals")
data class Peal(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "peal_name") val name: String,
    @ColumnInfo(name = "quantity") val quantity: Int,
    @ColumnInfo(name = "type") val type: PealType
)

 enum class PealType{
     INJECTION,
     PEAL
 }