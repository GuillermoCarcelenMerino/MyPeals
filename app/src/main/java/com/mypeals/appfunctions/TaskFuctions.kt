package com.mypeals.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.service.AppFunction
import com.mypeals.core.data.room.entities.Peal
import com.mypeals.core.data.room.entities.PealType
import com.mypeals.core.domain.usercase.AddPealsUseCase
import javax.inject.Inject

class TaskFuctions @Inject constructor(
    private val addPealsUseCase: AddPealsUseCase
) {
    /**
     * Añade o registra una nueva medicación, medicamento o peal en la base de datos de la aplicación.
     * Esta función se activa cuando el usuario pide agregar un remedio, pastillas o dosis.
     *
     * @param context El contexto de ejecución de la función de la app en Android.adb shell cmd app_function list-app-functions
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
            val nuevaMedication = Peal(
                id = 0,
                name = nombre,
                quantity = quantity,
                type = PealType.PEAL
            )

            val resultadoAsistente = addPealsUseCase(nuevaMedication).fold(
                onSuccess = {
                    "Entendido, he añadido $nombre con $quantity pastillas a tus registros."
                },
                onFailure = { error ->
                    "No pude guardar la medicación en este momento: ${error.localizedMessage}"
                }
            )

            resultadoAsistente
        } catch (e: Exception) {
            "No pude añadir la medicación debido a un error interno."
        }
    }
}