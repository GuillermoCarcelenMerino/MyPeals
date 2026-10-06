package com.mypeals.core.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mypeals.core.domain.model.IntakeStatus

/** Historial de tomas. Se borra en cascada junto con su medicación. */
@Entity(
    tableName = "intake_log",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medication_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medication_id"])]
)
data class IntakeLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "medication_id") val medicationId: Int,
    /** Hora a la que estaba programada la toma (epoch millis). */
    @ColumnInfo(name = "scheduled_at") val scheduledAt: Long,
    /** Hora real de la toma (epoch millis); null si se omitió o está pospuesta. */
    @ColumnInfo(name = "taken_at") val takenAt: Long?,
    @ColumnInfo(name = "status") val status: IntakeStatus
)
