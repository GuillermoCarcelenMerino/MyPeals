package com.mypeals.ui.history

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.mypeals.core.domain.model.DoseState
import com.mypeals.core.domain.model.HistoryDay
import com.mypeals.core.domain.model.HistoryEntry
import com.mypeals.core.theme.MyPealsTheme
import com.mypeals.ui.common.StatusBadge
import com.mypeals.ui.common.quantityStringResource
import java.util.Date

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryContent(state = state, onMarkTaken = viewModel::markTaken)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
    state: HistoryUiState,
    onMarkTaken: (HistoryEntry) -> Unit
) {
    Scaffold(
        // La bottom bar y el resto de insets los gestiona el Scaffold de MyPealsApp.
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.history_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.days.isEmpty() -> EmptyHistory(Modifier.align(Alignment.Center))
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.days, key = { it.dayStartMillis }) { day ->
                        DaySection(day = day, onMarkTaken = onMarkTaken)
                    }
                }
            }
        }
    }
}

@Composable
private fun DaySection(day: HistoryDay, onMarkTaken: (HistoryEntry) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = dayTitle(day.dayStartMillis),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            day.entries.forEachIndexed { index, entry ->
                if (index > 0) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                EntryRow(entry = entry, onMarkTaken = { onMarkTaken(entry) })
            }
        }
    }
}

@Composable
private fun EntryRow(entry: HistoryEntry, onMarkTaken: () -> Unit) {
    val context = LocalContext.current
    val timeFormat = DateFormat.getTimeFormat(context)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = timeFormat.format(Date(entry.scheduledAtMillis)),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.widthIn(min = 56.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = entry.medicationName, style = MaterialTheme.typography.titleSmall)
            Text(
                text = quantityStringResource(R.plurals.history_dose, entry.dose, entry.dose),
                style = MaterialTheme.typography.bodySmall
            )
            val takenAt = entry.takenAtMillis
            if (entry.state == DoseState.TAKEN && takenAt != null) {
                Text(
                    text = stringResource(R.string.history_taken_at, timeFormat.format(Date(takenAt))),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (entry.canMarkTaken) {
                TextButton(
                    onClick = onMarkTaken,
                    contentPadding = PaddingValues(0.dp)
                ) { Text(stringResource(R.string.history_mark_taken)) }
            }
        }
        DoseStateBadge(entry.state)
    }
}

@Composable
private fun DoseStateBadge(state: DoseState) {
    val colors = MaterialTheme.colorScheme
    val (label, container, content) = when (state) {
        DoseState.TAKEN -> Triple(R.string.history_state_taken, colors.primaryContainer, colors.onPrimaryContainer)
        DoseState.MISSED -> Triple(R.string.history_state_missed, colors.errorContainer, colors.onErrorContainer)
        DoseState.SNOOZED -> Triple(R.string.history_state_snoozed, colors.tertiaryContainer, colors.onTertiaryContainer)
        DoseState.SKIPPED -> Triple(R.string.history_state_skipped, colors.surface, colors.onSurface)
        DoseState.PENDING -> Triple(R.string.history_state_pending, colors.secondaryContainer, colors.onSecondaryContainer)
    }
    StatusBadge(text = stringResource(label), containerColor = container, contentColor = content)
}

@Composable
private fun dayTitle(dayStartMillis: Long): String {
    val context = LocalContext.current
    return when {
        DateUtils.isToday(dayStartMillis) -> stringResource(R.string.history_today)
        // Mediodía del día siguiente: evita fallos en los días de cambio de hora.
        DateUtils.isToday(dayStartMillis + 36 * DateUtils.HOUR_IN_MILLIS) -> stringResource(R.string.history_yesterday)
        else -> DateUtils.formatDateTime(
            context,
            dayStartMillis,
            DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_SHOW_DATE
        )
    }
}

@Composable
private fun EmptyHistory(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.history_empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.history_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewHistory() {
    val now = System.currentTimeMillis()
    fun entry(minutesAgo: Long, name: String, state: DoseState, taken: Boolean = false) = HistoryEntry(
        medicationId = name.hashCode(),
        medicationName = name,
        dose = 2,
        scheduledAtMillis = now - minutesAgo * 60_000L,
        takenAtMillis = if (taken) now - minutesAgo * 60_000L + 300_000L else null,
        state = state
    )
    MyPealsTheme {
        HistoryContent(
            state = HistoryUiState(
                days = listOf(
                    HistoryDay(
                        dayStartMillis = now - now % 86_400_000L,
                        entries = listOf(
                            entry(600, "Amoxicilina", DoseState.TAKEN, taken = true),
                            entry(240, "Ibuprofeno", DoseState.MISSED),
                            entry(10, "Omeprazol", DoseState.PENDING)
                        )
                    )
                )
            ),
            onMarkTaken = {}
        )
    }
}
