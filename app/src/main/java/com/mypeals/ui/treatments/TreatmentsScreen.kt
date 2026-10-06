package com.mypeals.ui.treatments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import android.text.format.DateUtils
import com.mypeals.core.domain.model.nextIntakeAfter
import com.mypeals.ui.common.StatusBadge
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mypeals.R
import com.mypeals.core.domain.model.Frequency
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.model.MedicationType
import com.mypeals.core.domain.model.StockStatus
import com.mypeals.core.domain.model.isStockLow
import com.mypeals.core.domain.model.stockStatus
import com.mypeals.core.theme.MyPealsTheme
import com.mypeals.ui.common.labelRes
import com.mypeals.ui.common.quantityStringResource

@Composable
fun TreatmentsScreen(
    onAddMedication: () -> Unit,
    onEditMedication: (Int) -> Unit,
    viewModel: TreatmentsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TreatmentsContent(
        state = state,
        onAddMedication = onAddMedication,
        onQuantityChange = viewModel::updateQuantity,
        onEdit = { onEditMedication(it.id) },
        onToggleActive = { medication -> viewModel.setActive(medication, !medication.active) },
        onDelete = viewModel::delete,
        banner = { ReminderPermissionsBanner() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatmentsContent(
    state: TreatmentsUiState,
    onAddMedication: () -> Unit,
    onQuantityChange: (Medication, Int) -> Unit,
    onEdit: (Medication) -> Unit = {},
    onToggleActive: (Medication) -> Unit = {},
    onDelete: (Medication) -> Unit = {},
    banner: @Composable () -> Unit = {}
) {
    Scaffold(
        // La bottom bar y el resto de insets los gestiona el Scaffold de MyPealsApp.
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.treatments_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        },
        floatingActionButton = { AddMedicationFab(onClick = onAddMedication) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            banner()
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    state.medications.isEmpty() -> EmptyTreatments(Modifier.align(Alignment.Center))
                    else -> MedicationList(
                        medications = state.medications,
                        onQuantityChange = onQuantityChange,
                        onEdit = onEdit,
                        onToggleActive = onToggleActive,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun AddMedicationFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            painter = painterResource(R.drawable.sumar_icon),
            contentDescription = stringResource(R.string.add_medication)
        )
    }
}

@Composable
private fun MedicationList(
    medications: List<Medication>,
    onQuantityChange: (Medication, Int) -> Unit,
    onEdit: (Medication) -> Unit,
    onToggleActive: (Medication) -> Unit,
    onDelete: (Medication) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // El padding inferior evita que el FAB tape la última tarjeta.
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(medications, key = { it.id }) { medication ->
            MedicationCard(
                medication = medication,
                onQuantityChange = onQuantityChange,
                onEdit = { onEdit(medication) },
                onToggleActive = { onToggleActive(medication) },
                onDelete = { onDelete(medication) }
            )
        }
    }
}

@Composable
private fun MedicationCard(
    medication: Medication,
    onQuantityChange: (Medication, Int) -> Unit,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val stock = remember(medication) { medication.stockStatus() }
    val lowStock = remember(medication) { medication.isStockLow() }
    val context = LocalContext.current
    val nextIntake = remember(medication) {
        if (!medication.active) {
            null
        } else {
            medication.nextIntakeAfter(System.currentTimeMillis())?.let {
                DateUtils.getRelativeDateTimeString(
                    context, it, DateUtils.DAY_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0
                ).toString()
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = medication.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(medication.type.labelRes()),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (nextIntake != null) {
                    Text(
                        text = stringResource(R.string.next_intake, nextIntake),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                StockInfo(stock)
                if (!medication.active || lowStock) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!medication.active) {
                            StatusBadge(
                                text = stringResource(R.string.treatment_paused),
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (lowStock) {
                            StatusBadge(
                                text = stringResource(R.string.stock_low),
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            IconButton(
                onClick = { onQuantityChange(medication, medication.remainingUnits - 1) },
                enabled = medication.remainingUnits > 0
            ) {
                Icon(
                    painter = painterResource(R.drawable.menos_icon),
                    contentDescription = stringResource(R.string.decrease_quantity, medication.name)
                )
            }
            IconButton(onClick = { onQuantityChange(medication, medication.remainingUnits + 1) }) {
                Icon(
                    painter = painterResource(R.drawable.sumar_icon),
                    contentDescription = stringResource(R.string.increase_quantity, medication.name)
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = stringResource(R.string.more_options, medication.name)
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_edit)) },
                        onClick = { menuOpen = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(
                                    if (medication.active) R.string.menu_pause else R.string.menu_resume
                                )
                            )
                        },
                        onClick = { menuOpen = false; onToggleActive() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_delete)) },
                        onClick = { menuOpen = false; confirmDelete = true }
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_message, medication.name)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text(stringResource(R.string.menu_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/** Cajas y unidades restantes; en la última caja solo se muestran las unidades. */
@Composable
private fun StockInfo(status: StockStatus) {
    when (status) {
        StockStatus.Empty -> Text(
            text = stringResource(R.string.stock_empty),
            style = MaterialTheme.typography.titleMedium
        )

        is StockStatus.BoxesOnly -> Text(
            text = quantityStringResource(R.plurals.stock_boxes, status.boxes, status.boxes),
            style = MaterialTheme.typography.titleMedium
        )

        is StockStatus.LastBox -> Text(
            text = quantityStringResource(R.plurals.stock_last_box, status.units, status.units),
            style = MaterialTheme.typography.titleMedium
        )

        is StockStatus.MultipleBoxes -> {
            Text(
                text = quantityStringResource(R.plurals.stock_boxes, status.boxes, status.boxes),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = quantityStringResource(
                    R.plurals.stock_units_in_current_box,
                    status.unitsInCurrentBox,
                    status.unitsInCurrentBox
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EmptyTreatments(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.empty_treatments_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.empty_treatments_hint),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTreatmentsContent() {
    MyPealsTheme {
        TreatmentsContent(
            state = TreatmentsUiState(
                medications = listOf(
                    previewMedication(1, "Ibuprofeno", 45, MedicationType.PILL, unitsPerBox = 20),
                    previewMedication(2, "Insulina", 3, MedicationType.INJECTION),
                    previewMedication(3, "Amoxicilina", 8, MedicationType.CAPSULE, unitsPerBox = 30),
                    previewMedication(4, "Omeprazol", 0, MedicationType.CAPSULE, unitsPerBox = 14)
                )
            ),
            onAddMedication = {},
            onQuantityChange = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTreatmentsEmpty() {
    MyPealsTheme {
        TreatmentsContent(
            state = TreatmentsUiState(),
            onAddMedication = {},
            onQuantityChange = { _, _ -> }
        )
    }
}

private fun previewMedication(
    id: Int,
    name: String,
    units: Int,
    type: MedicationType,
    unitsPerBox: Int? = null
) = Medication(
    id = id,
    name = name,
    type = type,
    unitsPerBox = unitsPerBox,
    remainingUnits = units,
    frequency = Frequency(interval = 8, unit = FrequencyUnit.HOURS),
    firstIntakeAtMillis = 0L
)
