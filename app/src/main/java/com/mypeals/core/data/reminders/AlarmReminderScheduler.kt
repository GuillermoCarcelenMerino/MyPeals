package com.mypeals.core.data.reminders

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.model.nextIntakeAfter
import com.mypeals.core.domain.reminder.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación con [AlarmManager]. Usa alarma exacta si el usuario ha concedido el permiso
 * (API 31+); si no, cae a una alarma inexacta que sigue despertando el dispositivo.
 */
@Singleton
class AlarmReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : ReminderScheduler {

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(medication: Medication, afterMillis: Long) {
        val next = if (medication.active) medication.nextIntakeAfter(afterMillis) else null
        if (next == null) {
            alarmManager.cancel(ReminderIntents.regularAlarmForCancel(context, medication.id))
            return
        }
        setAlarm(next, ReminderIntents.regularAlarm(context, medication.id, next))
    }

    override fun scheduleSnooze(medicationId: Int, scheduledAtMillis: Long, triggerAtMillis: Long) {
        setAlarm(triggerAtMillis, ReminderIntents.snoozeAlarm(context, medicationId, scheduledAtMillis))
    }

    override fun cancel(medicationId: Int) {
        alarmManager.cancel(ReminderIntents.regularAlarmForCancel(context, medicationId))
        alarmManager.cancel(ReminderIntents.snoozeAlarmForCancel(context, medicationId))
    }

    private fun setAlarm(triggerAtMillis: Long, pendingIntent: android.app.PendingIntent) {
        val manager = alarmManager
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()
        try {
            if (canExact) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            // El permiso se revocó entre la comprobación y la llamada.
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }
}
