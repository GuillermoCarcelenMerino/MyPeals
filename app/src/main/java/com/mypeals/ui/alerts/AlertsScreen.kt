package com.mypeals.ui.alerts

import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mypeals.R
import com.mypeals.core.domain.model.Alert
import com.mypeals.core.domain.model.AlertStatus
import com.mypeals.core.domain.model.Frequency
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.theme.MyPealsTheme
import com.mypeals.ui.common.StatusBadge
import com.mypeals.ui.common.quantityStringResource
import com.mypeals.ui.treatments.ReminderPermissionsBanner
import java.util.Date

@Composable
fun AlertsScreen(viewModel: AlertsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AlertsContent(state = state, banner = { ReminderPermissionsBanner() })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsContent(
    state: AlertsUiState,
    banner: @Composable () -> Unit = {}
) {
    Scaffold(
        // The bottom bar and the remaining insets are handled by the MyPealsApp Scaffold.
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.alerts_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            banner()
            Box(modifier = Modifier.fillMaxSize()) {
                val (scheduled, others) = state.alerts.partition { it.status == AlertStatus.SCHEDULED }
                when {
                    state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    state.alerts.isEmpty() -> EmptyAlerts(Modifier.align(Alignment.Center))
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (scheduled.isNotEmpty()) {
                            item { SectionTitle(stringResource(R.string.alerts_section_upcoming)) }
                            items(scheduled, key = { it.medicationId }) { AlertCard(it) }
                        }
                        if (others.isNotEmpty()) {
                            item { SectionTitle(stringResource(R.string.alerts_section_inactive)) }
                            items(others, key = { it.medicationId }) { AlertCard(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, end = 4.dp)
    )
}

@Composable
private fun AlertCard(alert: Alert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = alert.medicationName, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(
                        R.string.alerts_schedule,
                        frequencyText(alert.frequency),
                        quantityStringResource(R.plurals.history_dose, alert.dose, alert.dose)
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
                val next = alert.nextAtMillis
                if (next != null) {
                    Text(
                        text = stringResource(R.string.alerts_next_at, nextTimeText(next)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            AlertBadge(alert)
        }
    }
}

@Composable
private fun AlertBadge(alert: Alert) {
    val colors = MaterialTheme.colorScheme
    when (alert.status) {
        AlertStatus.SCHEDULED -> StatusBadge(
            text = remainingText(alert.remainingMillis ?: 0L),
            containerColor = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer
        )

        AlertStatus.PAUSED -> StatusBadge(
            text = stringResource(R.string.treatment_paused),
            containerColor = colors.surface,
            contentColor = colors.onSurface
        )

        AlertStatus.NO_UPCOMING -> StatusBadge(
            text = stringResource(R.string.alerts_no_upcoming),
            containerColor = colors.surface,
            contentColor = colors.onSurface
        )
    }
}

/** "In 2 h 15 min": shows at most the two largest units. */
@Composable
private fun remainingText(millis: Long): String {
    val time = remainingTimeOf(millis)
    if (time.isNow) return stringResource(R.string.alerts_remaining_now)
    val parts = listOfNotNull(
        if (time.days > 0) stringResource(R.string.alerts_unit_days, time.days) else null,
        if (time.hours > 0) stringResource(R.string.alerts_unit_hours, time.hours) else null,
        if (time.minutes > 0) stringResource(R.string.alerts_unit_minutes, time.minutes) else null
    ).take(2)
    return stringResource(R.string.alerts_remaining, parts.joinToString(" "))
}

@Composable
private fun nextTimeText(millis: Long): String {
    val context = LocalContext.current
    val time = DateFormat.getTimeFormat(context).format(Date(millis))
    return when {
        DateUtils.isToday(millis) -> stringResource(R.string.alerts_today_at, time)
        DateUtils.isToday(millis - DateUtils.DAY_IN_MILLIS) -> stringResource(R.string.alerts_tomorrow_at, time)
        else -> DateUtils.formatDateTime(
            context,
            millis,
            DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_ABBREV_WEEKDAY or
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_SHOW_TIME
        )
    }
}

@Composable
private fun frequencyText(frequency: Frequency): String {
    val id = when (frequency.unit) {
        FrequencyUnit.HOURS -> R.plurals.alerts_every_hours
        FrequencyUnit.DAYS -> R.plurals.alerts_every_days
        FrequencyUnit.WEEKS -> R.plurals.alerts_every_weeks
    }
    return quantityStringResource(id, frequency.interval, frequency.interval)
}

@Composable
private fun EmptyAlerts(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.alerts_empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.alerts_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewAlerts() {
    val now = System.currentTimeMillis()
    fun alert(id: Int, name: String, status: AlertStatus, inMinutes: Long? = null) = Alert(
        medicationId = id,
        medicationName = name,
        dose = 1,
        frequency = Frequency(8, FrequencyUnit.HOURS),
        status = status,
        nextAtMillis = inMinutes?.let { now + it * 60_000L },
        remainingMillis = inMinutes?.let { it * 60_000L }
    )
    MyPealsTheme {
        AlertsContent(
            state = AlertsUiState(
                alerts = listOf(
                    alert(1, "Omeprazol", AlertStatus.SCHEDULED, inMinutes = 25),
                    alert(2, "Ibuprofeno", AlertStatus.SCHEDULED, inMinutes = 135),
                    alert(3, "Amoxicilina", AlertStatus.PAUSED)
                )
            )
        )
    }
}
