package com.mypeals.ui.home

import com.mypeals.core.data.room.entities.Peal

data class UIState(
    val peals: List<Peal> = listOf(),
    val loading : Boolean= false,
)