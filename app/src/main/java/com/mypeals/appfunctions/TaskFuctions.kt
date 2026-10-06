package com.mypeals.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.service.AppFunction
import com.mypeals.core.domain.model.Frequency
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.model.MedicationType
import com.mypeals.core.domain.usecase.AddMedicationUseCase
import javax.inject.Inject

class TaskFuctions @Inject constructor(
    private val addMedicationUseCase: AddMedicationUseCase
) {
    /**
     * Añade o registra una nueva medicación, medicamento o peal en la base de datos de la aplicación.
     * Esta función se activa cuando el usuario pide agregar un remedio, pastillas o dosis.
     *
     * @param context El contexto de ejecución de la función de la app en Android.
     * @param nombre El nombre de la medicación o medicina que se va a registrar (ej. "Ibuprofeno", "Paracetamol").
     * @param quantity El número de pastillas, comprimidos, unidades o cantidad de la medicación.
     * @return Un texto de confirmación que el asistente Gemini leerá en voz alta al usuario.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun agregarNuevaMedicacion(
        context: AppFunctionContext,
        nombre: String,
        quantity: Int
    ): String {
        return try {
            // Por voz solo se indican nombre y cantidad: la medicación queda en pausa
            // (sin avisos) hasta que el usuario configure su frecuencia en la app.
            val nuevaMedication = Medication(
                name = nombre,
                type = MedicationType.PILL,
                remainingUnits = quantity,
                frequency = Frequency(interval = 1, unit = FrequencyUnit.DAYS),
                firstIntakeAtMillis = System.currentTimeMillis(),
                active = false
            )

            addMedicationUseCase(nuevaMedication).fold(
                onSuccess = {
                    "Entendido, he añadido $nombre con $quantity unidades a tus registros. " +
                        "Configura su horario en la aplicación para recibir avisos."
                },
                onFailure = { error ->
                    "No pude guardar la medicación en este momento: ${error.localizedMessage}"
                }
            )
        } catch (e: Exception) {
            "No pude añadir la medicación debido a un error interno."
        }
    }
}
