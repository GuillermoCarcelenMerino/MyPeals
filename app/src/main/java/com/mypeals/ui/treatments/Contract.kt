package com.mypeals.ui.treatments

import com.mypeals.core.data.room.entities.Medication

data class TreatmentsUiState(
    val medications: List<Medication> = listOf(),
    val loading: Boolean = false,
)
