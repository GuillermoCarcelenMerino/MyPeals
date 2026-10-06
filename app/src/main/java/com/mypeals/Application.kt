package com.mypeals

import android.app.Application
import androidx.appfunctions.service.AppFunctionConfiguration
import com.mypeals.appfunctions.TaskFuctions
import com.mypeals.core.data.reminders.ReminderNotifier
import com.mypeals.core.domain.usecase.ScheduleAllRemindersUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class Application : Application(), AppFunctionConfiguration.Provider {

    @Inject
    lateinit var taskFuctions: TaskFuctions

    @Inject
    lateinit var reminderNotifier: ReminderNotifier

    @Inject
    lateinit var scheduleAllReminders: ScheduleAllRemindersUseCase

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        reminderNotifier.createChannel()
        // Garantiza que los avisos están programados aunque el sistema los haya perdido.
        appScope.launch { scheduleAllReminders() }
    }


    override val appFunctionConfiguration: AppFunctionConfiguration
        get() = AppFunctionConfiguration.Builder()
            .addEnclosingClassFactory(TaskFuctions::class.java) { taskFuctions }.build()
}