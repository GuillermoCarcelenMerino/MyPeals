package com.mypeals.core.data.reminders

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/** Acciones, extras y PendingIntents compartidos por el planificador y los receivers. */
internal object ReminderIntents {
    const val ACTION_REMIND = "com.mypeals.action.REMIND"
    const val ACTION_TAKEN = "com.mypeals.action.TAKEN"
    const val ACTION_SNOOZE = "com.mypeals.action.SNOOZE"

    const val EXTRA_MEDICATION_ID = "medication_id"
    const val EXTRA_SCHEDULED_AT = "scheduled_at"

    const val SNOOZE_MINUTES = 10L

    private const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    // Dos códigos distintos por medicación: aviso regular y aviso pospuesto.
    private fun regularCode(id: Int) = id * 4
    private fun snoozeCode(id: Int) = id * 4 + 1
    private fun takenCode(id: Int) = id * 4 + 2
    private fun snoozeActionCode(id: Int) = id * 4 + 3

    private fun remindIntent(context: Context, id: Int, scheduledAt: Long) =
        Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMIND
            putExtra(EXTRA_MEDICATION_ID, id)
            putExtra(EXTRA_SCHEDULED_AT, scheduledAt)
        }

    fun regularAlarm(context: Context, id: Int, scheduledAt: Long): PendingIntent =
        PendingIntent.getBroadcast(context, regularCode(id), remindIntent(context, id, scheduledAt), FLAGS)

    fun snoozeAlarm(context: Context, id: Int, scheduledAt: Long): PendingIntent =
        PendingIntent.getBroadcast(context, snoozeCode(id), remindIntent(context, id, scheduledAt), FLAGS)

    /** Para cancelar: mismo requestCode, el contenido de los extras da igual. */
    fun regularAlarmForCancel(context: Context, id: Int): PendingIntent =
        regularAlarm(context, id, 0L)

    fun snoozeAlarmForCancel(context: Context, id: Int): PendingIntent =
        snoozeAlarm(context, id, 0L)

    private fun actionIntent(context: Context, action: String, id: Int, scheduledAt: Long) =
        Intent(context, ReminderActionReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_MEDICATION_ID, id)
            putExtra(EXTRA_SCHEDULED_AT, scheduledAt)
        }

    fun taken(context: Context, id: Int, scheduledAt: Long): PendingIntent =
        PendingIntent.getBroadcast(
            context, takenCode(id), actionIntent(context, ACTION_TAKEN, id, scheduledAt), FLAGS
        )

    fun snooze(context: Context, id: Int, scheduledAt: Long): PendingIntent =
        PendingIntent.getBroadcast(
            context, snoozeActionCode(id), actionIntent(context, ACTION_SNOOZE, id, scheduledAt), FLAGS
        )
}
