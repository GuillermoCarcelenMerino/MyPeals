package com.mypeals.core.domain.repository

import com.mypeals.core.data.room.entities.Peal

interface DatabaseRepository {
    suspend fun getPeals(): List<Peal>
    suspend fun getPeal(id: Int): Peal
    suspend fun deletePeal(peal: Peal)
    suspend fun updatePeal(peal: Peal)
    suspend fun addPeal(peal: Peal)
}