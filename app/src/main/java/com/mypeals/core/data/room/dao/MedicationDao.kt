package com.mypeals.core.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mypeals.core.data.room.entities.Medication
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Query("SELECT * FROM peals ORDER BY id")
    fun observeAll(): Flow<List<Medication>>

    @Query("SELECT * FROM peals WHERE id = :id")
    suspend fun getMedication(id: Int): Medication?

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)

    @Insert
    suspend fun addMedication(medication: Medication)
}
