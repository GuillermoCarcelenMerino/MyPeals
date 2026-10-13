# feature/base_viewmodel

## Qué hace
Crea un ViewModel padre genérico, `BaseViewModel<S>`, del que heredan todos los ViewModels de pantalla. El tipo de `UiState` de cada pantalla se le pasa como genérico:

```kotlin
class HistoryViewModel @Inject constructor(...) :
    BaseViewModel<HistoryUiState>(HistoryUiState(loading = true))
```

El padre ofrece:
- `uiState: StateFlow<S>`: el estado único de la pantalla, que la UI recoge con `collectAsStateWithLifecycle()`.
- `publish { it.copy(...) }`: la única forma de modificar el estado, a partir del estado actual.
- `currentState`: lectura del estado actual (por ejemplo, para validar antes de guardar).
- `observe(flow) { ... }`: recoge un `Flow` de un caso de uso solo mientras alguien observa `uiState`, y lo mantiene 5 s más para que una rotación no lo reinicie. Es el mismo comportamiento que antes daba `stateIn(WhileSubscribed(5_000))` en Tratamientos e Historial: al salir de la pantalla se deja de escuchar Room.

ViewModels migrados:
- `TreatmentsViewModel` y `HistoryViewModel`: dejan `stateIn` y pasan a `observe` + `publish`.
- `AddMedicationViewModel`: deja su `MutableStateFlow` propio y usa `publish`/`currentState`.

Las pantallas no cambian: siguen leyendo `viewModel.uiState`. La convención de `CLAUDE.md` se ha actualizado.

## Archivos principales
- `ui/common/BaseViewModel.kt`: el padre genérico y la extensión `whileSubscribed`.
- `ui/treatments/TreatmentsViewModel.kt`, `ui/history/HistoryViewModel.kt`, `ui/addmedication/AddMedicationViewModel.kt`.
- `app/src/test/java/com/mypeals/ui/common/BaseViewModelTest.kt`.

## Cómo probarlo
- Tests de unidad: `gradlew.bat testDebugUnitTest`. `BaseViewModelTest` comprueba el estado inicial, `publish`/`currentState`, que el flujo no se recoge sin observadores y que se detiene tras el timeout y vuelve a empezar con un nuevo observador.
- Tests instrumentados: `gradlew.bat connectedDebugAndroidTest`. Las pantallas se comportan igual que antes.
- Manual: añade, edita, pausa y borra una medicación y marca una toma en el Historial. Las listas se actualizan solas igual que antes.

## Integración con la pantalla de Alertas
Tras fusionar `master` (que ya incluye `feature/pantalla_alertas`), `AlertsViewModel` también hereda de `BaseViewModel<AlertsUiState>` y usa `observe` + `publish`. Así el refresco de la cuenta atrás se sigue parando cuando la pantalla no está visible.
