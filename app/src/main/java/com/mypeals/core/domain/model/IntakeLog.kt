package com.mypeals.core.domain.model

/** Anotación del historial: lo que ocurrió con una toma programada. */
data class IntakeLog(
    val id: Long,
    val medicationId: Int,
    val scheduledAtMillis: Long,
    /** Hora real de la toma; null si se omitió o está pospuesta. */
    val takenAtMillis: Long?,
    val status: IntakeStatus
)
