package com.mypeals.ui.treatments

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
import com.mypeals.core.data.room.entities.Medication
import com.mypeals.core.data.room.entities.MedicationType
import com.mypeals.core.theme.MyPealsTheme
import com.mypeals.ui.common.labelRes

@Composable
fun TreatmentsScreen(
    onAddMedication: () -> Unit,
    viewModel: TreatmentsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TreatmentsContent(
        state = state,
        onAddMedication = onAddMedication,
        onQuantityChange = viewModel::updateQuantity
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatmentsContent(
    state: TreatmentsUiState,
    onAddMedication: () -> Unit,
    onQuantityChange: (Medication, Int) -> Unit
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
            }
            IconButton(
                onClick = { onQuantityChange(medication, medication.quantity - 1) },
                enabled = medication.quantity > 0
            ) {
                Icon(
                    painter = painterResource(R.drawable.menos_icon),
                    contentDescription = stringResource(R.string.decrease_quantity)
                )
            }
            Text(
                text = medication.quantity.toString(),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = { onQuantityChange(medication, medication.quantity + 1) }) {
                Icon(
                    painter = painterResource(R.drawable.sumar_icon),
                    contentDescription = stringResource(R.string.increase_quantity)
                )
            }
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
                    Medication(1, "Ibuprofeno", 20, MedicationType.PEAL),
                    Medication(2, "Insulina", 3, MedicationType.INJECTION)
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
