# feature/tests_viewmodels

## Qué hace
Añade tests de unidad a los ViewModels de pantalla, que hasta ahora no tenían ninguno. Kover les da un 100 % de cobertura de líneas:

| Clase | Líneas | Ramas |
|---|---|---|
| `AddMedicationViewModel` | 100 % | 23/24 |
| `AlertsViewModel` | 100 % | — |
| `HistoryViewModel` | 100 % | 100 % |
| `TreatmentsViewModel` | 100 % | 100 % |
| `BaseViewModel` | 100 % | 100 % |

Qué se prueba:
- **AddMedicationViewModel**: cada `onXxx` del formulario, añadir y quitar días de la semana, guardar con el formulario inválido (solo muestra errores), guardar una medicación nueva, ignorar una segunda pulsación mientras se guarda, el fallo al guardar, cargar para editar (ok, no existe, error, mismo id dos veces) y guardar en modo edición conservando el stock.
- **AlertsViewModel**: empieza cargando, no recoge el flujo mientras nadie observa la pantalla, publica una alerta por medicación, se actualiza cuando cambian las medicaciones y el error del flujo.
- **HistoryViewModel**: estado inicial, publicación de los días, error del flujo y `markTaken` (correcto y con fallo).
- **TreatmentsViewModel**: estado inicial, publicación y seguimiento de la lista, error del flujo, `updateQuantity` (incluido no bajar de 0), pausar y reanudar, borrar, y que los fallos no cambian nada.

Los ViewModels se crean con los casos de uso reales sobre un repositorio y un programador de avisos falsos, sin mocks.

### Dependencia nueva (solo tests)
`viewModelScope` usa `Dispatchers.Main`, que no existe en la JVM. Se añade `org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0` como `testImplementation`. Es la misma versión de `kotlinx-coroutines-core` que ya resuelve el proyecto. `MainDispatcherRule` lo sustituye por un `UnconfinedTestDispatcher`.

## Archivos principales
- `app/src/test/java/com/mypeals/ui/addmedication/AddMedicationViewModelTest.kt`
- `app/src/test/java/com/mypeals/ui/alerts/AlertsViewModelTest.kt`
- `app/src/test/java/com/mypeals/ui/history/HistoryViewModelTest.kt`
- `app/src/test/java/com/mypeals/ui/treatments/TreatmentsViewModelTest.kt`
- `app/src/test/java/com/mypeals/ui/testing/ViewModelFakes.kt`: `FakeMedicationRepository` (puede forzar fallos con `failWith`), `FakeReminderScheduler`, `medication(...)` y los helpers `awaitUntil`, `subscribe` (simula a la pantalla observando el estado) y `close`.
- `app/src/test/java/com/mypeals/ui/testing/MainDispatcherRule.kt`: regla JUnit que sustituye `Dispatchers.Main`.
- `gradle/libs.versions.toml`, `app/build.gradle.kts`: la dependencia `kotlinx-coroutines-test`.
- `app/build.gradle.kts`: `testOptions { unitTests.isReturnDefaultValues = true }`. El `android.jar` de los tests no implementa `android.util.Log`; con esta opción `Log.e` devuelve un valor por defecto y las ramas de error, que registran con `Log.e`, no fallan.

## Cómo probarlo
- Tests de unidad: `gradlew.bat testDebugUnitTest`
- Cobertura: `gradlew.bat koverHtmlReportDebug` y abrir `app/build/reports/kover/htmlDebug/index.html` (paquetes `com.mypeals.ui.*`).
