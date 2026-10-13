## Promps utilizados:

Promp 1 : 
- Tarea : Crear una nueva pantalla para controlar las alertas que estan actualmente configuradas
- Promp: "quiero que generes una nueva pantalla que sea llamada "alertas" donde puedas ver las alertas que hay configuradas y pendientes asi como cuando queda para que se ejecuten"
- Resultado : 
  - Tiempo de 4m 53s 
  - Resultado esperado y pantalla generada --> pasado
  - Comprobaciones de test, lint y compilacción --> pasado
  - Documentación generada--> pasado
  - Solicitud de push --> pasado
  - Pr generada --> pasado
  - 
Promp 2 : 
- Tarea : Refactorizar viewmodels para que utilicen uno padre con un uistate heredado al que se le pasa el tipo
- Promp: "refactoriza los viewmodels y crea un viewmodel padre generico que ya tenga un state y un metodo publish que se hereden por los viewmodels hijos actuales a los cuales se les pasa el tipo de UIState como un generico  │app/src/main/java/com/mypeals/ui/common/BaseViewModel.kt                                 
  <> para que se utilice "
- Resultado : 
  - Tiempo de  5m 10 
  - Resultado esperado y pantalla generada --> pasado
  - Comprobaciones de test, lint y compilacción --> pasado
  - Documentación generada--> pasado
  - Solicitud de push --> pasado

Promp 3 :
- Tarea : Generar tests unitarios para los viewmodels con un 80% de cobertura
- Promp: "quiero que generes test unitarios para las los viewmodels y que cubras un 80% de esas clases"
- Resumen de cambios:
  - Tests nuevos: AddMedicationViewModelTest (12), AlertsViewModelTest (5), HistoryViewModelTest (5) y TreatmentsViewModelTest (9)
  - Fakes compartidos en ui/testing (repositorio y programador de avisos), MainDispatcherRule y un sustituto de android.util.Log para los tests
  - Nueva dependencia de test kotlinx-coroutines-test 1.9.0 (confirmada por el usuario) para sustituir Dispatchers.Main
- Resultado :
  - Tiempo de ~10m
  - Resultado esperado --> pasado
  - Compilación (assembleDebug) --> pasado
  - Tests de unidad (93 tests, 0 fallos) --> pasado
  - Tests instrumentados --> no ejecutados (no se toca UI ni base de datos)
  - Lint --> pasado, 0 errores (1 aviso nuevo: hay una versión más nueva de kotlinx-coroutines-test, se mantiene la 1.9.0 para igualar kotlinx-coroutines-core)
  - Documentación generada (docs/feature_tests_viewmodels.md) --> pasado
- Kover:
  - AddMedicationViewModel, AlertsViewModel, HistoryViewModel, TreatmentsViewModel y BaseViewModel: 100% de líneas
  - Total del proyecto: 62,8% de líneas, 66,8% de instrucciones y 55,3% de ramas
