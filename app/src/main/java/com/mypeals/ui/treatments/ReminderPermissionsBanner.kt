package com.mypeals.ui.treatments

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.mypeals.R

private fun Context.notificationsGranted(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

private fun Context.exactAlarmsGranted(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        (getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()

/**
 * Avisa de que faltan permisos para que los recordatorios funcionen y permite concederlos.
 * No muestra nada si ya está todo concedido. Se vuelve a comprobar al volver a la app.
 */
@Composable
fun ReminderPermissionsBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var notifications by remember { mutableStateOf(context.notificationsGranted()) }
    var exactAlarms by remember { mutableStateOf(context.exactAlarmsGranted()) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        notifications = context.notificationsGranted()
        exactAlarms = context.exactAlarmsGranted()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { notifications = it }

    if (notifications && exactAlarms) return

    Card(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!notifications) {
                Text(
                    text = stringResource(R.string.reminder_permission_notifications),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                TextButton(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) { Text(stringResource(R.string.reminder_permission_allow)) }
            }
            if (!exactAlarms) {
                Text(
                    text = stringResource(R.string.reminder_permission_exact_alarms),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                TextButton(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    }
                }) { Text(stringResource(R.string.reminder_permission_allow)) }
            }
        }
    }
}
