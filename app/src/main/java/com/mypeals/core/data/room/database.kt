package com.mypeals.core.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mypeals.core.data.room.dao.PealDao
import com.mypeals.core.data.room.entities.Peal
import com.mypeals.core.data.room.mappers.PealConverters

@Database(entities = [Peal::class], version = 1)
@TypeConverters(PealConverters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun pealDao(): PealDao
}