# feature/pantalla_alertas

## Qué hace
Añade una pestaña **Alertas** a la barra inferior. Muestra las alertas configuradas para cada medicación y cuánto falta para la siguiente:

- **Próximas**: tratamientos activos con un aviso pendiente, ordenados del más cercano al más lejano. Cada tarjeta muestra la pauta ("Cada 8 horas · 2 unidades"), la hora de la próxima alerta ("hoy a las 14:00", "mañana a las 08:00" o la fecha) y una etiqueta con el tiempo restante ("En 2 h 15 min", o "Ahora" si queda menos de un segundo).
- **Sin alertas pendientes**: tratamientos en pausa, o activos cuya pauta ya no tiene más tomas, ordenados por nombre.

El tiempo restante se recalcula cada 30 segundos mientras la pantalla está visible y se redondea hacia arriba al minuto. También se recalcula al instante cuando cambia la medicación (alta, edición, pausa). Arriba se muestra el mismo aviso de permisos que en Tratamientos (notificaciones y alarmas exactas).

### Cómo se calcula
Android no permite consultar las alarmas programadas en `AlarmManager`. Por eso la próxima alerta se calcula con `nextIntakeAfter`, la misma función que usa `AlarmReminderScheduler` para programar la alarma, y coincide con la alarma real.

**Limitación:** los avisos pospuestos ("Posponer 10 min" en la notificación) no aparecen. Su hora de disparo no se guarda en la base de datos y para mostrarlos habría que cambiar una entidad de Room.

## Archivos principales
- `core/domain/model/Alerts.kt` — modelo `Alert`/`AlertStatus` y `buildAlerts` (Kotlin puro)
- `core/domain/usecase/ObserveAlertsUseCase.kt` — combina la medicación con un refresco periódico
- `ui/alerts/AlertsScreen.kt`, `AlertsViewModel.kt`, `AlertsUiState.kt` (incluye `remainingTimeOf`), `NavigationKey.kt`
- `ui/navigation/TopLevelDestination.kt`, `EntryProvider.kt` — nueva pestaña
- `res/drawable/ic_alerts.xml`, textos `alerts_*` en `values/` y `values-es/strings.xml`

## Cómo probarlo
- Tests de unidad: `gradlew.bat testDebugUnitTest`. Los nuevos son `AlertsTest`, `ObserveAlertsUseCaseTest` y `AlertsUiStateTest`.
- Tests instrumentados: `gradlew.bat connectedDebugAndroidTest`. El nuevo es `AlertsScreenTest` y necesita un emulador o dispositivo.
- Manual:
  1. Añade una medicación cada 8 horas con la primera toma dentro de unos minutos.
  2. Abre la pestaña Alertas: la medicación sale en "Próximas" con el tiempo restante.
  3. Pausa el tratamiento desde Tratamientos: pasa a "Sin alertas pendientes" con la etiqueta "En pausa".
