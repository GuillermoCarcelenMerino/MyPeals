package com.mypeals.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mypeals.R
import com.mypeals.core.domain.model.Frequency
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.model.MedicationType
import com.mypeals.core.theme.MyPealsTheme
import com.mypeals.ui.addmedication.AddMedicationActions
import com.mypeals.ui.addmedication.AddMedicationContent
import com.mypeals.ui.addmedication.AddMedicationUiState
import com.mypeals.ui.common.labelRes
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddMedicationScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private var name: String? = null
    private var type: MedicationType? = null
    private var saves = 0
    private var backs = 0

    private fun show(state: AddMedicationUiState = AddMedicationUiState.initial()) {
        rule.setContent {
            MyPealsTheme {
                AddMedicationContent(
                    state = state,
                    actions = AddMedicationActions(
                        onNameChange = { name = it },
                        onTypeChange = { type = it },
                        onBoxesChange = {},
                        onUnitsPerBoxChange = {},
                        onDoseChange = {},
                        onFrequencyIntervalChange = {},
                        onFrequencyUnitChange = {},
                        onWeekdayToggle = {},
                        onStartDateChange = {},
                        onStartTimeChange = { _, _ -> },
                        onSave = { saves++ },
                        onBack = { backs++ }
                    )
                )
            }
        }
    }

    @Test
    fun escribirElNombreLlamaAlCallback() {
        show()

        rule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("Ibu")

        assertEquals("Ibu", name)
    }

    @Test
    fun elegirUnTipoLlamaAlCallback() {
        show()

        rule.onNodeWithText(str(MedicationType.SYRUP.labelRes())).performScrollTo().performClick()

        assertEquals(MedicationType.SYRUP, type)
    }

    @Test
    fun guardarLlamaAlCallback() {
        show()

        rule.onNodeWithText(str(R.string.form_save)).performScrollTo().performClick()

        assertEquals(1, saves)
    }

    @Test
    fun conErroresActivosMuestraQueElNombreEsObligatorio() {
        show(AddMedicationUiState.initial().copy(showErrors = true, name = ""))

        rule.onNodeWithText(str(R.string.error_required)).assertIsDisplayed()
    }

    @Test
    fun sinErroresActivosNoMuestraElMensaje() {
        show(AddMedicationUiState.initial().copy(showErrors = false, name = ""))

        rule.onAllNodesWithText(str(R.string.error_required)).assertCountEquals(0)
    }

    @Test
    fun enAltaMuestraElCampoDeCajas() {
        show()

        rule.onNodeWithText(str(R.string.add_medication_title)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.form_boxes)).assertExists()
    }

    @Test
    fun enEdicionCambiaElTituloYOcultaLasCajas() {
        val medication = Medication(
            id = 3,
            name = "Ibuprofeno",
            type = MedicationType.PILL,
            unitsPerBox = 20,
            remainingUnits = 40,
            frequency = Frequency(8, FrequencyUnit.HOURS),
            firstIntakeAtMillis = 0L
        )
        show(AddMedicationUiState.fromMedication(medication))

        rule.onNodeWithText(str(R.string.edit_medication_title)).assertIsDisplayed()
        rule.onAllNodesWithText(str(R.string.form_boxes)).assertCountEquals(0)
        rule.onNodeWithText("Ibuprofeno").assertExists()
    }

    @Test
    fun losDiasDeLaSemanaSoloAparecenConFrecuenciaSemanal() {
        show(AddMedicationUiState.initial().copy(frequencyUnit = FrequencyUnit.DAYS))
        rule.onAllNodesWithText(str(R.string.form_weekdays)).assertCountEquals(0)
    }

    @Test
    fun conFrecuenciaSemanalApareceLaSeleccionDeDias() {
        show(AddMedicationUiState.initial().copy(frequencyUnit = FrequencyUnit.WEEKS))
        rule.onNodeWithText(str(R.string.form_weekdays)).assertExists()
    }

    @Test
    fun atrasLlamaAlCallback() {
        show()

        rule.onNodeWithContentDescription(str(R.string.back)).performClick()

        assertEquals(1, backs)
    }
}
