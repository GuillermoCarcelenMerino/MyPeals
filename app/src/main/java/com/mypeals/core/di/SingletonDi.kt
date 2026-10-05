package com.mypeals.core.di

import android.content.Context
import androidx.room.Room
import com.mypeals.core.data.repository.DatabaseRepositoryImpl
import com.mypeals.core.data.room.AppDataBase
import com.mypeals.core.data.room.mappers.PealConverters
import com.mypeals.core.domain.repository.DatabaseRepository
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
        ).addTypeConverter(PealConverters()).build()
    }


    @Binds
    @Singleton
    abstract fun bindDatabaseRepository(
        databaseRepositoryImpl: DatabaseRepositoryImpl
    ): DatabaseRepository
}