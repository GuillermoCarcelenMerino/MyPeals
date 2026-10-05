package com.mypeals.core.di

import android.content.Context
import androidx.room.Room
import com.mypeals.core.data.repository.MedicationRepositoryImpl
import com.mypeals.core.data.room.AppDataBase
import com.mypeals.core.data.room.mappers.MedicationConverters
import com.mypeals.core.domain.repository.MedicationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SingletonDi {

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(
            @ApplicationContext context: Context
        ): AppDataBase = Room.databaseBuilder(
            context,
            AppDataBase::class.java, "myPealsDatabase"
        ).addTypeConverter(MedicationConverters()).build()
    }

    @Binds
    @Singleton
    abstract fun bindMedicationRepository(
        medicationRepositoryImpl: MedicationRepositoryImpl
    ): MedicationRepository
}
