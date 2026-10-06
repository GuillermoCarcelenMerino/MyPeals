package com.mypeals.ui.addmedication

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mypeals.R
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.MedicationType
import com.mypeals.core.domain.model.Weekday
import com.mypeals.core.theme.MyPealsTheme
import com.mypeals.ui.common.labelRes
import com.mypeals.ui.common.shortLabelRes
import java.text.DateFormat as JavaDateFormat
import java.util.Date
import java.util.TimeZone

/** Callbacks del formulario, agrupados para no arrastrar una decena de parámetros. */
class AddMedicationActions(
    val onNameChange: (String) -> Unit,
    val onTypeChange: (MedicationType) -> Unit,
    val onBoxesChange: (String) -> Unit,
    val onUnitsPerBoxChange: (String) -> Unit,
    val onDoseChange: (String) -> Unit,
    val onFrequencyIntervalChange: (String) -> Unit,
    val onFrequencyUnitChange: (FrequencyUnit) -> Unit,
    val onWeekdayToggle: (Weekday) -> Unit,
    val onStartDateChange: (Long) -> Unit,
    val onStartTimeChange: (Int, Int) -> Unit,
    val onSave: () -> Unit,
    val onBack: () -> Unit
)

@Composable
fun AddMedicationScreen(
    onBack: () -> Unit,
    viewModel: AddMedicationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AddMedicationContent(
        state = state,
        actions = AddMedicationActions(
            onNameChange = viewModel::onNameChange,
            onTypeChange = viewModel::onTypeChange,
            onBoxesChange = viewModel::onBoxesChange,
            onUnitsPerBoxChange = viewModel::onUnitsPerBoxChange,
            onDoseChange = viewModel::onDoseChange,
            onFrequencyIntervalChange = viewModel::onFrequencyIntervalChange,
            onFrequencyUnitChange = viewModel::onFrequencyUnitChange,
            onWeekdayToggle = viewModel::onWeekdayToggle,
            onStartDateChange = viewModel::onStartDateChange,
            onStartTimeChange = viewModel::onStartTimeChange,
            onSave = { viewModel.save(onSaved = onBack) },
            onBack = onBack
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicationContent(
    state: AddMedicationUiState,
    actions: AddMedicationActions
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.add_medication_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Nombre ---
            OutlinedTextField(
                value = state.name,
                onValueChange = actions.onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.form_name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                isError = state.showErrors && state.nameError,
                supportingText = errorText(state.showErrors && state.nameError, R.string.error_required)
            )

            // --- Tipo ---
            SectionTitle(R.string.form_type)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MedicationType.entries.forEach { type ->
                    FilterChip(
                        selected = type == state.type,
                        onClick = { actions.onTypeChange(type) },
                        label = { Text(stringResource(type.labelRes())) }
                    )
                }
            }

            // --- Cajas y unidades por caja ---
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(
                    value = state.boxes,
                    onValueChange = actions.onBoxesChange,
                    label = R.string.form_boxes,
                    showError = state.showErrors && state.boxesError,
                    modifier = Modifier.weight(1f)
                )
                NumberField(
                    value = state.unitsPerBox,
                    onValueChange = actions.onUnitsPerBoxChange,
                    label = R.string.form_units_per_box,
                    showError = state.showErrors && state.unitsPerBoxError,
                    modifier = Modifier.weight(1f)
                )
            }
            NumberField(
                value = state.dosePerIntake,
                onValueChange = actions.onDoseChange,
                label = R.string.form_dose,
                showError = state.showErrors && state.doseError,
                modifier = Modifier.fillMaxWidth()
            )

            // --- Frecuencia ---
            SectionTitle(R.string.form_frequency)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = stringResource(R.string.frequency_every),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 18.dp)
                )
                NumberField(
                    value = state.frequencyInterval,
                    onValueChange = actions.onFrequencyIntervalChange,
                    label = null,
                    showError = state.showErrors && state.frequencyIntervalError,
                    modifier = Modifier.width(96.dp)
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FrequencyUnit.entries.forEach { unit ->
                    FilterChip(
                        selected = unit == state.frequencyUnit,
                        onClick = { actions.onFrequencyUnitChange(unit) },
                        label = { Text(stringResource(unit.labelRes())) }
                    )
                }
            }
            if (state.frequencyUnit == FrequencyUnit.WEEKS) {
                SectionTitle(R.string.form_weekdays)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Weekday.entries.forEach { day ->
                        FilterChip(
                            selected = day in state.weekdays,
                            onClick = { actions.onWeekdayToggle(day) },
                            label = { Text(stringResource(day.shortLabelRes())) }
                        )
                    }
                }
            }

            // --- Primera toma ---
            SectionTitle(R.string.form_first_intake)
            FirstIntakePickers(state = state, actions = actions)

            // --- Guardar ---
            if (state.saveFailed) {
                Text(
                    text = stringResource(R.string.error_save),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(
                onClick = actions.onSave,
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.form_save))
            }
        }
    }
}

@Composable
private fun SectionTitle(titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: Int?,
    showError: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        // Solo dígitos y un máximo de 4 para evitar desbordes al multiplicar cajas por unidades.
        onValueChange = { onValueChange(it.filter(Char::isDigit).take(MAX_DIGITS)) },
        modifier = modifier,
        label = label?.let { { Text(stringResource(it)) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = showError,
        supportingText = errorText(showError, R.string.error_positive_number)
    )
}

@Composable
private fun errorText(show: Boolean, messageRes: Int): (@Composable () -> Unit)? =
    if (show) {
        { Text(stringResource(messageRes)) }
    } else {
        null
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FirstIntakePickers(
    state: AddMedicationUiState,
    actions: AddMedicationActions
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateText = JavaDateFormat.getDateInstance(JavaDateFormat.MEDIUM).apply {
        // La fecha viene como medianoche UTC: hay que formatearla también en UTC.
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(state.startDateUtcMillis))
    val timeText = JavaDateFormat.getTimeInstance(JavaDateFormat.SHORT)
        .format(Date(combineDateAndTime(state.startDateUtcMillis, state.startHour, state.startMinute)))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
            Text(dateText)
        }
        OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f)) {
            Text(timeText)
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.startDateUtcMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(actions.onStartDateChange)
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = state.startHour,
            initialMinute = state.startMinute,
            is24Hour = DateFormat.is24HourFormat(LocalContext.current)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    actions.onStartTimeChange(timeState.hour, timeState.minute)
                    showTimePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = { TimePicker(state = timeState) }
        )
    }
}

private const val MAX_DIGITS = 4

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewAddMedicationContent() {
    MyPealsTheme {
        AddMedicationContent(
            state = AddMedicationUiState.initial().copy(name = "Ibuprofeno"),
            actions = AddMedicationActions({}, {}, {}, {}, {}, {}, {}, {}, {}, { _, _ -> }, {}, {})
        )
    }
}
