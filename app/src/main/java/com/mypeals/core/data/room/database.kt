package com.mypeals.core.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mypeals.core.data.room.dao.MedicationDao
import com.mypeals.core.data.room.entities.Medication
import com.mypeals.core.data.room.mappers.MedicationConverters

@Database(entities = [Medication::class], version = 1)
@TypeConverters(MedicationConverters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
}
