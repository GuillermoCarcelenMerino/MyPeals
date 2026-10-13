package com.mypeals.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mypeals.R
import com.mypeals.core.domain.model.Alert
import com.mypeals.core.domain.model.AlertStatus
import com.mypeals.core.domain.model.Frequency
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.theme.MyPealsTheme
import com.mypeals.ui.alerts.AlertsContent
import com.mypeals.ui.alerts.AlertsUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertsScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val minute = 60_000L

    private fun alert(id: Int, name: String, status: AlertStatus, remainingMinutes: Long? = null) = Alert(
        medicationId = id,
        medicationName = name,
        dose = 1,
        frequency = Frequency(8, FrequencyUnit.HOURS),
        status = status,
        nextAtMillis = remainingMinutes?.let { System.currentTimeMillis() + it * minute },
        remainingMillis = remainingMinutes?.let { it * minute }
    )

    private fun show(state: AlertsUiState) {
        rule.setContent { MyPealsTheme { AlertsContent(state = state) } }
    }

    @Test
    fun sinAlertasMuestraElEstadoVacio() {
        show(AlertsUiState())

        rule.onNodeWithText(str(R.string.alerts_empty_title)).assertIsDisplayed()
    }

    @Test
    fun mientrasCargaNoMuestraElEstadoVacio() {
        show(AlertsUiState(loading = true))

        rule.onAllNodesWithText(str(R.string.alerts_empty_title)).assertCountEquals(0)
    }

    @Test
    fun muestraElTiempoRestanteDeLasAlertasProgramadas() {
        show(AlertsUiState(alerts = listOf(alert(1, "Ibuprofeno", AlertStatus.SCHEDULED, remainingMinutes = 135))))

        val remaining = "${str(R.string.alerts_unit_hours, 2)} ${str(R.string.alerts_unit_minutes, 15)}"
        rule.onNodeWithText(str(R.string.alerts_section_upcoming)).assertIsDisplayed()
        rule.onNodeWithText("Ibuprofeno").assertIsDisplayed()
        rule.onNodeWithText(str(R.string.alerts_remaining, remaining)).assertIsDisplayed()
        rule.onNodeWithText(
            str(R.string.alerts_schedule, plural(R.plurals.alerts_every_hours, 8, 8), plural(R.plurals.history_dose, 1, 1))
        ).assertIsDisplayed()
    }

    @Test
    fun separaLasAlertasEnPausa() {
        show(
            AlertsUiState(
                alerts = listOf(
                    alert(1, "Ibuprofeno", AlertStatus.SCHEDULED, remainingMinutes = 10),
                    alert(2, "Amoxicilina", AlertStatus.PAUSED)
                )
            )
        )

        rule.onNodeWithText(str(R.string.alerts_section_inactive)).assertIsDisplayed()
        rule.onNodeWithText("Amoxicilina").assertIsDisplayed()
        rule.onNodeWithText(str(R.string.treatment_paused)).assertIsDisplayed()
    }

    @Test
    fun conMenosDeUnSegundoMuestraAhora() {
        show(AlertsUiState(alerts = listOf(alert(1, "Ibuprofeno", AlertStatus.SCHEDULED, remainingMinutes = 0))))

        rule.onNodeWithText(str(R.string.alerts_remaining_now)).assertIsDisplayed()
    }
}
