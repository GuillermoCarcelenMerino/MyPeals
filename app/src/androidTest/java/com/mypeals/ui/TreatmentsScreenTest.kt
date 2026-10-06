package com.mypeals.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mypeals.R
import com.mypeals.core.domain.model.Frequency
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.model.MedicationType
import com.mypeals.core.theme.MyPealsTheme
import com.mypeals.ui.treatments.TreatmentsContent
import com.mypeals.ui.treatments.TreatmentsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TreatmentsScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private fun medication(
        id: Int = 1,
        name: String = "Ibuprofeno",
        units: Int = 40,
        active: Boolean = true
    ) = Medication(
        id = id,
        name = name,
        type = MedicationType.PILL,
        unitsPerBox = 20,
        remainingUnits = units,
        frequency = Frequency(8, FrequencyUnit.HOURS),
        firstIntakeAtMillis = 0L,
        active = active
    )

    private fun show(
        state: TreatmentsUiState,
        onAdd: () -> Unit = {},
        onQuantityChange: (Medication, Int) -> Unit = { _, _ -> },
        onEdit: (Medication) -> Unit = {},
        onToggleActive: (Medication) -> Unit = {},
        onDelete: (Medication) -> Unit = {},
        banner: @androidx.compose.runtime.Composable () -> Unit = {}
    ) {
        rule.setContent {
            MyPealsTheme {
                TreatmentsContent(
                    state = state,
                    onAddMedication = onAdd,
                    onQuantityChange = onQuantityChange,
                    onEdit = onEdit,
                    onToggleActive = onToggleActive,
                    onDelete = onDelete,
                    banner = banner
                )
            }
        }
    }

    @Test
    fun muestraLaMedicacion() {
        show(TreatmentsUiState(medications = listOf(medication(), medication(2, "Amoxicilina"))))

        rule.onNodeWithText("Ibuprofeno").assertIsDisplayed()
        rule.onNodeWithText("Amoxicilina").assertIsDisplayed()
    }

    @Test
    fun sinMedicacionMuestraElEstadoVacio() {
        show(TreatmentsUiState())

        rule.onNodeWithText(str(R.string.empty_treatments_title)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.empty_treatments_hint)).assertIsDisplayed()
    }

    @Test
    fun mientrasCargaNoMuestraElEstadoVacio() {
        show(TreatmentsUiState(loading = true))

        rule.onAllNodesWithText(str(R.string.empty_treatments_title)).assertCountEquals(0)
    }

    @Test
    fun elBotonAnadirLlamaAlCallback() {
        var clicks = 0
        show(TreatmentsUiState(), onAdd = { clicks++ })

        rule.onNodeWithContentDescription(str(R.string.add_medication)).performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun sumarYRestarCambianLaCantidad() {
        val changes = mutableListOf<Int>()
        show(
            TreatmentsUiState(medications = listOf(medication(units = 10))),
            onQuantityChange = { _, units -> changes += units }
        )

        rule.onNodeWithContentDescription(str(R.string.increase_quantity, "Ibuprofeno")).performClick()
        rule.onNodeWithContentDescription(str(R.string.decrease_quantity, "Ibuprofeno")).performClick()

        assertEquals(listOf(11, 9), changes)
    }

    @Test
    fun noSePuedeRestarConStockCero() {
        show(TreatmentsUiState(medications = listOf(medication(units = 0))))

        rule.onNodeWithContentDescription(str(R.string.decrease_quantity, "Ibuprofeno")).assertIsNotEnabled()
        rule.onNodeWithText(str(R.string.stock_empty)).assertIsDisplayed()
    }

    @Test
    fun avisaDeStockBajo() {
        // 3 unidades a razón de una cada 8 horas: menos de un día de tratamiento.
        show(TreatmentsUiState(medications = listOf(medication(units = 3))))

        rule.onNodeWithText(str(R.string.stock_low)).assertIsDisplayed()
    }

    @Test
    fun marcaLosTratamientosEnPausa() {
        show(TreatmentsUiState(medications = listOf(medication(active = false))))

        rule.onNodeWithText(str(R.string.treatment_paused)).assertIsDisplayed()
    }

    @Test
    fun muestraElBannerQueSeLePasa() {
        show(TreatmentsUiState(), banner = { Text("BANNER") })

        rule.onNodeWithText("BANNER").assertIsDisplayed()
    }

    @Test
    fun borrarPideConfirmacionYBorraAlConfirmar() {
        var deleted: Medication? = null
        show(TreatmentsUiState(medications = listOf(medication())), onDelete = { deleted = it })

        rule.onNodeWithContentDescription(str(R.string.more_options, "Ibuprofeno")).performClick()
        rule.onNodeWithText(str(R.string.menu_delete)).performClick()
        rule.onNodeWithText(str(R.string.delete_confirm_title)).assertIsDisplayed()
        assertNull(deleted)

        rule.onNode(hasText(str(R.string.menu_delete)) and hasAnyAncestor(isDialog())).performClick()

        assertEquals(1, deleted?.id)
    }

    @Test
    fun cancelarElBorradoNoBorra() {
        var deleted: Medication? = null
        show(TreatmentsUiState(medications = listOf(medication())), onDelete = { deleted = it })

        rule.onNodeWithContentDescription(str(R.string.more_options, "Ibuprofeno")).performClick()
        rule.onNodeWithText(str(R.string.menu_delete)).performClick()
        rule.onNodeWithText(str(R.string.cancel)).performClick()

        assertNull(deleted)
        rule.onAllNodesWithText(str(R.string.delete_confirm_title)).assertCountEquals(0)
    }

    @Test
    fun editarYPausarLlamanASusCallbacks() {
        var edited: Medication? = null
        var toggled: Medication? = null
        show(
            TreatmentsUiState(medications = listOf(medication())),
            onEdit = { edited = it },
            onToggleActive = { toggled = it }
        )

        rule.onNodeWithContentDescription(str(R.string.more_options, "Ibuprofeno")).performClick()
        rule.onNodeWithText(str(R.string.menu_edit)).performClick()
        rule.onNodeWithContentDescription(str(R.string.more_options, "Ibuprofeno")).performClick()
        rule.onNodeWithText(str(R.string.menu_pause)).performClick()

        assertEquals(1, edited?.id)
        assertEquals(1, toggled?.id)
    }
}
