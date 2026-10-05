package com.mypeals.core.data.repository

import com.mypeals.core.data.room.AppDataBase
import com.mypeals.core.data.room.entities.Peal
import com.mypeals.core.domain.repository.DatabaseRepository
import javax.inject.Inject

class DatabaseRepositoryImpl @Inject constructor(private val database: AppDataBase) :
    DatabaseRepository {
    override suspend fun getPeals(): List<Peal> =
        database.pealDao().getAll()


    override suspend fun getPeal(id: Int): Peal = database.pealDao().getPeal(id)

    override suspend fun deletePeal(peal: Peal) = database.pealDao().deletePeal(peal)

    override suspend fun updatePeal(peal: Peal) = database.pealDao().updatePeal(peal)
    override suspend fun addPeal(peal: Peal) = database.pealDao().addPeal(peal)
}