package com.mypeals.core.data.room.mappers

import com.mypeals.core.data.room.entities.IntakeLogEntity
import com.mypeals.core.domain.model.IntakeLog

fun IntakeLogEntity.toDomain(): IntakeLog = IntakeLog(
    id = id,
    medicationId = medicationId,
    scheduledAtMillis = scheduledAt,
    takenAtMillis = takenAt,
    status = status
)
