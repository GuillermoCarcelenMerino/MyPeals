package com.mypeals.core.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mypeals.core.data.room.entities.IntakeLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeLogDao {

    @Query("SELECT * FROM intake_log WHERE medication_id = :medicationId ORDER BY scheduled_at DESC")
    fun observeByMedication(medicationId: Int): Flow<List<IntakeLogEntity>>

    @Insert
    suspend fun insert(entry: IntakeLogEntity): Long
}
