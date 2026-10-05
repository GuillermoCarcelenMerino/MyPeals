package com.mypeals.ui.addmedication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mypeals.R
import com.mypeals.core.theme.MyPealsTheme

@Composable
fun AddMedicationScreen(
    onBack: () -> Unit,
    viewModel: AddMedicationViewModel = hiltViewModel()
) {
    AddMedicationContent(
        onBack = onBack,
        onCreateSample = { viewModel.saveSample(onSaved = onBack) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationContent(
    onBack: () -> Unit,
    onCreateSample: () -> Unit
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
                    IconButton(onClick = onBack) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.add_medication_coming_soon),
                style = MaterialTheme.typography.bodyMedium
            )
            // PROVISIONAL hasta que exista el formulario real (Fase 3).
            Button(onClick = onCreateSample) {
                Text(stringResource(R.string.create_sample_medication))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewAddMedicationContent() {
    MyPealsTheme {
        AddMedicationContent(onBack = {}, onCreateSample = {})
    }
}
