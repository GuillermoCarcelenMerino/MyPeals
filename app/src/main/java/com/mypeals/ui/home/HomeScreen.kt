package com.mypeals.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mypeals.R
import com.mypeals.core.data.room.entities.Medication
import com.mypeals.core.data.room.entities.MedicationType
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navigateAccount: () -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    Home(
        navigateAccount,
        state.value,
        { medication -> viewModel.addMedication(medication) },
        { medication, quantity -> viewModel.updateQuantity(medication, quantity) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    navigateAccount: () -> Unit,
    state: UIState,
    onAddMedication: (medication: Medication) -> Unit,
    updateQuantity: (medication: Medication, quantity: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var showBottomSheet by remember { mutableStateOf(false) }

    if (!state.loading) {
        ListMedications(
            state = state,
            onAddClick = {
                showBottomSheet = true
                scope.launch { sheetState.partialExpand() }
            },
            updateQuantity,
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            MedicationBottomSheetContent(onAddMedication)
        }
    }
}

@Composable
fun MedicationBottomSheetContent(onAddMedication: (medication: Medication) -> Unit) {

    Column {
        Button(onClick = {
            // Datos de ejemplo hasta que exista el formulario de alta (Fase 3).
            onAddMedication(Medication(0, "pastilla1", 3, MedicationType.INJECTION))
        }) {
            Text(stringResource(R.string.create_medication_placeholder))
        }
    }
}

@Composable
fun ListMedications(
    state: UIState,
    onAddClick: () -> Unit,
    updateQuantity: (Medication, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(state.medications, key = { it.id }) { medication ->
            Card(onClick = {}) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(medication.name, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primary
                                )

                        ) {
                            Button(shape = MaterialTheme.shapes.extraLarge, onClick = {
                                updateQuantity(medication, medication.quantity - 1)
                            }, enabled = medication.quantity >= 1) {
                                Icon(
                                    painter = painterResource(R.drawable.menos_icon),
                                    tint = MaterialTheme.colorScheme.secondary,
                                    contentDescription = stringResource(R.string.decrease_quantity),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                )
                            }
                            Text(
                                medication.quantity.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )

                            Button(shape = MaterialTheme.shapes.extraLarge, onClick = {
                                updateQuantity(medication, medication.quantity + 1)
                            }) {

                                Icon(
                                    painter = painterResource(R.drawable.sumar_icon),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    contentDescription = stringResource(R.string.increase_quantity)
                                )

                            }

                        }

                    }

                }
            }

        }
        item {
            Button(onClick = { onAddClick() }) {
                Text(stringResource(R.string.add_medication))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHomeScreen() {
    Home(
        {},
        UIState(medications = listOf(Medication(3, "", 3, MedicationType.INJECTION))),
        {},
        { _, _ -> })
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewMedicationBottomSheetContent() {
    MedicationBottomSheetContent({})
}
