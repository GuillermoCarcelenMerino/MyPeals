package com.mypeals.core.data.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mypeals.core.domain.model.IntakeStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

private fun BroadcastReceiver.runAsync(block: suspend () -> Unit) {
    val pending = goAsync()
    receiverScope.launch {
        try {
            block()
        } finally {
            pending.finish()
        }
    }
}

/** Salta cuando llega la hora de una toma: muestra la notificación y programa la siguiente. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ReminderIntents.EXTRA_MEDICATION_ID, -1)
        val scheduledAt = intent.getLongExtra(ReminderIntents.EXTRA_SCHEDULED_AT, System.currentTimeMillis())
        if (id < 0) return
        val entry = context.reminderEntryPoint()
        runAsync {
            val medication = entry.repository().getMedication(id) ?: return@runAsync
            if (!medication.active) return@runAsync
            entry.notifier().show(medication, scheduledAt)
            entry.scheduler().schedule(medication, maxOf(scheduledAt, System.currentTimeMillis()))
        }
    }
}

/** Botones de la notificación: "Tomada" y "Posponer". */
class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ReminderIntents.EXTRA_MEDICATION_ID, -1)
        val scheduledAt = intent.getLongExtra(ReminderIntents.EXTRA_SCHEDULED_AT, System.currentTimeMillis())
        if (id < 0) return
        val entry = context.reminderEntryPoint()
        val action = intent.action
        runAsync {
            entry.notifier().dismiss(id)
            when (action) {
                ReminderIntents.ACTION_TAKEN ->
                    entry.registerIntake()(id, scheduledAt, IntakeStatus.TAKEN)

                ReminderIntents.ACTION_SNOOZE -> {
                    entry.registerIntake()(id, scheduledAt, IntakeStatus.SNOOZED)
                    val triggerAt = System.currentTimeMillis() + ReminderIntents.SNOOZE_MINUTES * 60_000L
                    entry.scheduler().scheduleSnooze(id, scheduledAt, triggerAt)
                }
            }
        }
    }
}

/** Reprograma los avisos tras reinicio, cambio de hora/zona o actualización de la app. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val entry = context.reminderEntryPoint()
        runAsync { entry.scheduleAll()() }
    }
}
