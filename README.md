# MyPeals

App Android de registro y control de toma de medicamentos.

## Testing

El proyecto tiene dos niveles de tests y una regla de cobertura mínima que hace fallar la verificación si se incumple.

| Tipo | Carpeta | Se ejecuta en | Qué cubre |
|---|---|---|---|
| Unidad | `app/src/test` | JVM (sin dispositivo) | Modelos y casos de uso de domain, mappers, UiState y ViewModels |
| Instrumentados | `app/src/androidTest` | Emulador o dispositivo | Pantallas Compose, repositorio con Room y migraciones de base de datos |

### Comandos

En Windows usar `gradlew.bat`; en macOS/Linux, `./gradlew`.

```bash
# Tests de unidad
./gradlew testDebugUnitTest

# Tests instrumentados (requiere un emulador o dispositivo conectado: comprobar con `adb devices`)
./gradlew connectedDebugAndroidTest

# Informe de cobertura (HTML en app/build/reports/kover/htmlDebug/index.html)
./gradlew koverHtmlReportDebug

# Verificación de cobertura mínima: falla si no se alcanza
./gradlew koverVerifyDebug
```

### Tests de unidad

- **Herramientas:** JUnit 4 y `kotlinx-coroutines-test`.
- **ViewModels:** se prueban con dobles en memoria, sin mocks:
  - `ui/testing/ViewModelFakes.kt` incluye `FakeMedicationRepository`, que puede forzar errores con `failWith`.
  - `ui/testing/MainDispatcherRule.kt` sustituye `Dispatchers.Main` para que `viewModelScope` funcione en la JVM.
- **Clases de Android:** `unitTests.isReturnDefaultValues = true` (en `app/build.gradle.kts`) hace que llamadas como `android.util.Log` devuelvan valores por defecto en lugar de fallar.

### Tests instrumentados

- **Pantallas:** se prueban con `createComposeRule()` (Compose UI Test). Los textos esperados se centralizan en `TestStrings.kt`.
- **`MigrationTest`:** valida las migraciones de Room contra los esquemas exportados en `app/schemas/`. Toda migración nueva debe llevar su test.
- **`MedicationRepositoryTest`:** prueba el repositorio contra una base de datos Room real en memoria.

### Cobertura (Kover)

- **Qué mide:** solo los tests de unidad.
- **Mínimo:** está definido en `app/build.gradle.kts` (`verify { rule { minBound(60) } }`, 60 % de líneas). No se baja sin acuerdo del equipo.
- **Qué se excluye** del cálculo, para medir solo la lógica propia:
  - código generado por Room, Hilt y appfunctions;
  - tema, módulos de DI, DAO, `MainActivity` y `Application`;
  - funciones `@Composable`, porque la UI se cubre con los tests instrumentados.

### Convenciones

- **Ubicación:** cada test va en la misma ruta de paquete que la clase que prueba.
- **Nombres:** en español, entre backticks, describiendo el comportamiento: ``fun `sin stock`()``.
- **Nuevo desarrollo:** todo desarrollo nuevo incluye tests de unidad. Antes de abrir una PR deben pasar `testDebugUnitTest` y `koverVerifyDebug`, y también `connectedDebugAndroidTest` si se ha tocado UI o base de datos.
