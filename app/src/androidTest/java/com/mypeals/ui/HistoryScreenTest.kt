package com.mypeals.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mypeals.R
import com.mypeals.core.domain.model.DoseState
import com.mypeals.core.domain.model.HistoryDay
import com.mypeals.core.domain.model.HistoryEntry
import com.mypeals.core.theme.MyPealsTheme
import com.mypeals.ui.history.HistoryContent
import com.mypeals.ui.history.HistoryUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun entry(name: String, hour: Int, state: DoseState, dose: Int = 1) = HistoryEntry(
        medicationId = name.hashCode(),
        medicationName = name,
        dose = dose,
        scheduledAtMillis = todayStart + hour * 3_600_000L,
        takenAtMillis = if (state == DoseState.TAKEN) todayStart + hour * 3_600_000L + 300_000L else null,
        state = state
    )

    private fun show(state: HistoryUiState, onMarkTaken: (HistoryEntry) -> Unit = {}) {
        rule.setContent { MyPealsTheme { HistoryContent(state = state, onMarkTaken = onMarkTaken) } }
    }

    @Test
    fun sinTomasMuestraElEstadoVacio() {
        show(HistoryUiState())

        rule.onNodeWithText(str(R.string.history_empty_title)).assertIsDisplayed()
    }

    @Test
    fun mientrasCargaNoMuestraElEstadoVacio() {
        show(HistoryUiState(loading = true))

        rule.onAllNodesWithText(str(R.string.history_empty_title)).assertCountEquals(0)
    }

    @Test
    fun agrupaPorDiaYMuestraElEstadoDeCadaToma() {
        show(
            HistoryUiState(
                days = listOf(
                    HistoryDay(
                        todayStart,
                        listOf(
                            entry("Amoxicilina", 8, DoseState.TAKEN, dose = 2),
                            entry("Ibuprofeno", 9, DoseState.MISSED),
                            entry("Omeprazol", 23, DoseState.PENDING)
                        )
                    )
                )
            )
        )

        rule.onNodeWithText(str(R.string.history_today)).assertIsDisplayed()
        rule.onNodeWithText("Amoxicilina").assertIsDisplayed()
        rule.onNodeWithText(str(R.string.history_state_taken)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.history_state_missed)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.history_state_pending)).assertIsDisplayed()
        rule.onNodeWithText(plural(R.plurals.history_dose, 2, 2)).assertIsDisplayed()
    }

    @Test
    fun soloLasNoTomadasSePuedenMarcarComoTomadas() {
        var marked: HistoryEntry? = null
        show(
            HistoryUiState(
                days = listOf(
                    HistoryDay(
                        todayStart,
                        listOf(
                            entry("Amoxicilina", 8, DoseState.TAKEN),
                            entry("Ibuprofeno", 9, DoseState.MISSED)
                        )
                    )
                )
            ),
            onMarkTaken = { marked = it }
        )

        rule.onAllNodesWithText(str(R.string.history_mark_taken)).assertCountEquals(1)
        rule.onNodeWithText(str(R.string.history_mark_taken)).performClick()

        assertEquals("Ibuprofeno", marked?.medicationName)
    }

    @Test
    fun muestraAyerConSuNombre() {
        val yesterday = todayStart - 24 * 3_600_000L
        show(
            HistoryUiState(
                days = listOf(
                    HistoryDay(
                        yesterday,
                        listOf(entry("Ibuprofeno", 9, DoseState.SKIPPED).copy(scheduledAtMillis = yesterday + 9 * 3_600_000L))
                    )
                )
            )
        )

        rule.onNodeWithText(str(R.string.history_yesterday)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.history_state_skipped)).assertIsDisplayed()
    }
}
