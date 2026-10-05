package com.mypeals

import android.app.Application
import androidx.appfunctions.service.AppFunctionConfiguration
import com.mypeals.appfunctions.TaskFuctions
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Application : Application(), AppFunctionConfiguration.Provider {

    @Inject
    lateinit var taskFuctions: TaskFuctions


    override val appFunctionConfiguration: AppFunctionConfiguration
        get() = AppFunctionConfiguration.Builder()
            .addEnclosingClassFactory(TaskFuctions::class.java) { taskFuctions }.build()
}