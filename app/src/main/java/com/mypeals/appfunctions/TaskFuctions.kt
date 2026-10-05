package com.mypeals.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.service.AppFunction
import com.mypeals.core.data.room.entities.Medication
import com.mypeals.core.data.room.entities.MedicationType
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
            val nuevaMedication = Medication(
                id = 0,
                name = nombre,
                quantity = quantity,
                type = MedicationType.PEAL
            )

            addMedicationUseCase(nuevaMedication).fold(
                onSuccess = {
                    "Entendido, he añadido $nombre con $quantity pastillas a tus registros."
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
