package com.mypeals.core.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mypeals.core.data.room.entities.Peal


@Dao
interface PealDao {

    @Query("SELECT * FROM peals")
    suspend fun getAll(): List<Peal>

    @Query("SELECT * FROM peals WHERE id = :id")
    suspend fun getPeal(id: Int): Peal

    @Update
    suspend fun updatePeal(peal: Peal)

    @Delete
    suspend fun deletePeal(peal: Peal)

    @Insert
    suspend fun addPeal(peal : Peal)

}