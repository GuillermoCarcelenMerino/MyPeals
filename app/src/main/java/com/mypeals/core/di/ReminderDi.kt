package com.mypeals.core.di

import com.mypeals.core.data.reminders.AlarmReminderScheduler
import com.mypeals.core.domain.reminder.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderDi {

    @Binds
    @Singleton
    abstract fun bindReminderScheduler(impl: AlarmReminderScheduler): ReminderScheduler
}
