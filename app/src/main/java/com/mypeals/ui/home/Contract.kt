package com.mypeals.ui.home

import com.mypeals.core.data.room.entities.Medication

data class UIState(
    val medications: List<Medication> = listOf(),
    val loading: Boolean = false,
)
