package com.mypeals.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.service.AppFunction
import com.mypeals.core.domain.model.Frequency
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.usecase.AddMedicationUseCase
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TaskFuctions @Inject constructor(
    private val addMedicationUseCase: AddMedicationUseCase
) {
    /**
     * Añade un medicamento al tratamiento del usuario en MyPeals (My Peals, "mai pils"), la app
     * de control de medicación: stock de pastillas y avisos de cuándo tomarlas. Úsala SOLO para
     * medicamentos o remedios que el usuario toma (pastillas, cápsulas, jarabe, inyecciones...),
     * y no para notas, listas de la compra, tareas ni recordatorios genéricos. Solo el nombre y
     * la cantidad son obligatorios; si el usuario no dice los demás datos, déjalos vacíos y no
     * los inventes.
     *
     * @param context Contexto de ejecución de la función de la app.
     * @param nombre Nombre del medicamento tal y como lo dice el usuario, por ejemplo
     * "Ibuprofeno" o "Paracetamol 1 g".
     * @param cantidad Número total de unidades que tiene el usuario (pastillas, comprimidos,
     * sobres, inyecciones...), por ejemplo 20. No es la dosis de cada toma.
     * @param tipo Forma del medicamento en palabras del usuario: pastilla, cápsula, comprimido,
     * sobre, inyección, jarabe, gotas, crema, inhalador, parche, supositorio o spray. Si no la
     * dice, déjalo vacío.
     * @param unidadesPorCaja Número de unidades que trae cada caja, por ejemplo 20 o 30. Déjalo
     * vacío si el usuario no lo menciona.
     * @param dosisPorToma Unidades que se toman cada vez, por ejemplo 2 si toma dos pastillas.
     * Si no lo dice, déjalo vacío (se asume 1).
     * @param cadaCuantasHoras Cada cuántas horas toma el medicamento, por ejemplo 8 para "cada
     * 8 horas". Si lo indica se activan los avisos; si no, la medicación se guarda en pausa
     * hasta que configure el horario en la app. Para "una vez al día" usa 24.
     * @return Texto de confirmación que el asistente leerá en voz alta al usuario.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun agregarNuevaMedicacion(
        context: AppFunctionContext,
        nombre: String,
        cantidad: Int,
        tipo: String?,
        unidadesPorCaja: Int?,
        dosisPorToma: Int?,
        cadaCuantasHoras: Int?
    ): String {
        val name = nombre.trim()
        if (name.isEmpty()) return "No he entendido el nombre del medicamento. ¿Cómo se llama?"
        if (cantidad <= 0) return "La cantidad de $name tiene que ser mayor que cero."

        val hours = cadaCuantasHoras?.takeIf { it > 0 }
        val now = System.currentTimeMillis()
        val medication = Medication(
            name = name,
            type = parseMedicationType(tipo),
            unitsPerBox = unidadesPorCaja?.takeIf { it > 0 },
            remainingUnits = cantidad,
            dosePerIntake = dosisPorToma?.takeIf { it > 0 } ?: 1,
            frequency = Frequency(interval = hours ?: 1, unit = if (hours != null) FrequencyUnit.HOURS else FrequencyUnit.DAYS),
            // Con horario, el primer aviso llega pasadas "hours" horas; sin él queda en pausa.
            firstIntakeAtMillis = if (hours != null) now + TimeUnit.HOURS.toMillis(hours.toLong()) else now,
            active = hours != null
        )

        return addMedicationUseCase(medication).fold(
            onSuccess = {
                if (hours != null) {
                    "Listo, he añadido $name con $cantidad unidades y te avisaré cada $hours horas."
                } else {
                    "Listo, he añadido $name con $cantidad unidades. Configura su horario en la " +
                        "aplicación para recibir avisos."
                }
            },
            onFailure = { "No pude guardar $name en este momento. Inténtalo de nuevo más tarde." }
        )
    }
}
