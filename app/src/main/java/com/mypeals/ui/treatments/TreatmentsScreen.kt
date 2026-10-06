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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
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
    viewModel: TreatmentsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TreatmentsContent(
        state = state,
        onAddMedication = onAddMedication,
        onQuantityChange = viewModel::updateQuantity,
        banner = { ReminderPermissionsBanner() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatmentsContent(
    state: TreatmentsUiState,
    onAddMedication: () -> Unit,
    onQuantityChange: (Medication, Int) -> Unit,
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
                        onQuantityChange = onQuantityChange
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
    onQuantityChange: (Medication, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // El padding inferior evita que el FAB tape la última tarjeta.
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(medications, key = { it.id }) { medication ->
            MedicationCard(medication = medication, onQuantityChange = onQuantityChange)
        }
    }
}

@Composable
private fun MedicationCard(
    medication: Medication,
    onQuantityChange: (Medication, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val stock = remember(medication) { medication.stockStatus() }
    val lowStock = remember(medication) { medication.isStockLow() }

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
                Spacer(modifier = Modifier.height(8.dp))
                StockInfo(stock)
                if (lowStock) {
                    Text(
                        text = stringResource(R.string.stock_low),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            IconButton(
                onClick = { onQuantityChange(medication, medication.remainingUnits - 1) },
                enabled = medication.remainingUnits > 0
            ) {
                Icon(
                    painter = painterResource(R.drawable.menos_icon),
                    contentDescription = stringResource(R.string.decrease_quantity)
                )
            }
            IconButton(onClick = { onQuantityChange(medication, medication.remainingUnits + 1) }) {
                Icon(
                    painter = painterResource(R.drawable.sumar_icon),
                    contentDescription = stringResource(R.string.increase_quantity)
                )
            }
        }
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
