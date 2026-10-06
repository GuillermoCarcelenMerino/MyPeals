package com.mypeals.core.data.reminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mypeals.MainActivity
import com.mypeals.R
import com.mypeals.core.domain.model.Medication
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        // El sonido de un canal no se puede cambiar una vez creado: se usa un id nuevo y se
        // elimina el anterior para quien ya tuviera la app instalada.
        manager.deleteNotificationChannel(LEGACY_CHANNEL_ID)

        // Sonido de alarma del sistema, con el volumen de alarma (se oye aunque el móvil
        // esté en silencio de notificaciones). Se puede sustituir por un audio en res/raw.
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.reminder_channel_description)
            setSound(sound, audioAttributes)
            enableVibration(true)
            vibrationPattern = VIBRATION_PATTERN
        }
        manager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission") // Se comprueba justo antes con hasPermission().
    fun show(medication: Medication, scheduledAtMillis: Long) {
        if (!hasPermission()) return
        createChannel()
        val openApp = android.app.PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.reminder_title, medication.name))
            .setContentText(
                context.resources.getQuantityString(
                    R.plurals.reminder_dose, medication.dosePerIntake, medication.dosePerIntake
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setWhen(scheduledAtMillis)
            // Se mantiene hasta que el usuario reaccione o pasen TIMEOUT_MINUTES.
            .setAutoCancel(false)
            .setTimeoutAfter(TIMEOUT_MINUTES * 60_000L)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(VIBRATION_PATTERN)
            .setContentIntent(openApp)
            .addAction(
                0,
                context.getString(R.string.reminder_action_taken),
                ReminderIntents.taken(context, medication.id, scheduledAtMillis)
            )
            .addAction(
                0,
                context.getString(R.string.reminder_action_snooze, ReminderIntents.SNOOZE_MINUTES.toInt()),
                ReminderIntents.snooze(context, medication.id, scheduledAtMillis)
            )
            .build()
            // Repite el sonido en bucle hasta que se descarta o expira la notificación.
            .apply { flags = flags or Notification.FLAG_INSISTENT }
        NotificationManagerCompat.from(context).notify(medication.id, notification)
    }

    fun dismiss(medicationId: Int) {
        NotificationManagerCompat.from(context).cancel(medicationId)
    }

    private fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    companion object {
        const val CHANNEL_ID = "intake_reminders_v2"
        private const val LEGACY_CHANNEL_ID = "intake_reminders"
        private const val TIMEOUT_MINUTES = 5L
        private val VIBRATION_PATTERN = longArrayOf(0, 500, 300, 500, 300, 500, 300, 500)
    }
}
