package com.mypeals.ui.treatments

import com.mypeals.core.domain.model.Medication

data class TreatmentsUiState(
    val medications: List<Medication> = listOf(),
    val loading: Boolean = false,
)
