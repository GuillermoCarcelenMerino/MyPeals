package com.mypeals.core.data.reminders

import android.content.Context
import com.mypeals.core.domain.repository.MedicationRepository
import com.mypeals.core.domain.reminder.ReminderScheduler
import com.mypeals.core.domain.usecase.RegisterIntakeUseCase
import com.mypeals.core.domain.usecase.ScheduleAllRemindersUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/** Acceso a Hilt desde los BroadcastReceiver sin usar @AndroidEntryPoint. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderEntryPoint {
    fun repository(): MedicationRepository
    fun scheduler(): ReminderScheduler
    fun notifier(): ReminderNotifier
    fun registerIntake(): RegisterIntakeUseCase
    fun scheduleAll(): ScheduleAllRemindersUseCase
}

internal fun Context.reminderEntryPoint(): ReminderEntryPoint =
    EntryPointAccessors.fromApplication(applicationContext, ReminderEntryPoint::class.java)
