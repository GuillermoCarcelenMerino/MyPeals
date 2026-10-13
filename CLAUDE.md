# MyPeals

App Android de registro y control de toma de medicamentos.

## Stack
- Kotlin, Jetpack Compose, Hilt, Room, Navigation 3
- Arquitectura en capas UI / Domain / Data con estado único por pantalla (estilo MVI)

## Comandos
En Windows usar `gradlew.bat`; en macOS/Linux, `./gradlew`.
- Compilar: `gradlew.bat assembleDebug`
- Tests de unidad: `gradlew.bat testDebugUnitTest`
- Tests instrumentados (requiere emulador o dispositivo, comprobar con `adb devices`): `gradlew.bat connectedDebugAndroidTest`
- Lint: `gradlew.bat lint`

## Estructura
- `core/domain/` — Kotlin puro: `model/`, `repository/` (interfaces), `usecase/`, `reminder/` (interfaces)
- `core/data/` — implementaciones: `repository/`, `room/` (`entities/`, `dao/`, `mappers/`, `migrations/`), `reminders/` (alarmas y notificaciones)
- `core/di/` — módulos Hilt
- `ui/<pantalla>/` — `XxxScreen.kt`, `XxxViewModel.kt`, `XxxUiState.kt` (o `Contract.kt`), `NavigationKey.kt`
- `ui/common/` — componentes compartidos; `ui/navigation/` — navegación
- `appfunctions/` — funciones expuestas al asistente de voz

## Convenciones
- Estado de pantalla: un `data class XxxUiState` inmutable por pantalla. El ViewModel hereda de `BaseViewModel<XxxUiState>` (`ui/common`), que expone `uiState: StateFlow<XxxUiState>`; el estado se modifica solo con `publish { it.copy(...) }` y los Flow de los casos de uso se recogen con `observe(flow) { ... }`. La pantalla lo recoge con `collectAsStateWithLifecycle()` y solo llama a funciones `onXxx(...)` del ViewModel; no tiene lógica de negocio
- Dependencias entre capas: domain no importa `android.*`/`androidx.*` ni nada de `core.data` o `ui` (solo se permite `javax.inject`). La UI no importa `core.data`: accede a los datos siempre a través de los casos de uso
- Casos de uso: Kotlin puro, una responsabilidad por clase, con `operator fun invoke(...)`. Solo dependen de interfaces de domain y de otros casos de uso
- Repositorios: interfaz en `core/domain/repository`, implementación en `core/data/repository`; enlazados en `core/di`
- Entidades Room separadas de los modelos de domain; la conversión se hace en `room/mappers`
- Textos: nada de texto visible escrito en el código; todo en `strings.xml`, con su traducción en `values/` (inglés) y `values-es/` (español)
- Tests de unidad: en `app/src/test`, en la misma ruta de paquete que la clase probada. Nombres en español, entre backticks, que describan el comportamiento: ``fun `sin stock`()``
- Tests instrumentados: en `app/src/androidTest`; los tests de UI usan los textos de `TestStrings.kt`

## Reglas
- No cambiar dependencias ni versiones (`gradle/libs.versions.toml`, `build.gradle.kts`) sin preguntar
- Base de datos: cualquier cambio en una entidad exige confirmación, subir la `version` de `@Database`, añadir la migración en `room/migrations/Migrations.kt` y su test en `MigrationTest`. Nunca usar `fallbackToDestructiveMigration`
- No tocar sin permiso: `keystore/`, `local.properties`, base de datos
- Nunca escribir secretos ni claves en el código
- No hacer `git push` sin confirmación
- Cada nuevo desarrollo va en una rama nueva creada desde `master`: `feature/<nombre_del_cambio>` (ej. `feature/nueva_pantalla`) o `bugfix/<arreglo>` (ej. `bugfix/add_pill_fix_button`)
- Mensajes de commit en español, en imperativo y concretos (ej. "Añade historial de tomas")
- Abrir PR contra `master` y añadir como reviewer a `GuillermoCarcelenMerino`
- Todo nuevo desarrollo debe llevar tests unitarios asociados

## Antes de dar algo por terminado
1. Comprobar la compilación (`assembleDebug`)
2. Ejecutar los tests de unidad y, si se ha tocado UI o base de datos, también los instrumentados
3. Pasar `lint` sin errores nuevos
4. Generar documentación del nuevo desarrollo en `docs/<nombre_de_la_rama>.md`: qué hace, archivos principales y cómo probarlo
