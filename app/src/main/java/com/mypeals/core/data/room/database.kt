package com.mypeals.core.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mypeals.core.data.room.dao.IntakeLogDao
import com.mypeals.core.data.room.dao.MedicationDao
import com.mypeals.core.data.room.entities.IntakeLogEntity
import com.mypeals.core.data.room.entities.MedicationEntity

@Database(entities = [MedicationEntity::class, IntakeLogEntity::class], version = 2)
abstract class AppDataBase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun intakeLogDao(): IntakeLogDao
}
