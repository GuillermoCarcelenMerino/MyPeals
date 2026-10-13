# Guía de uso de IA en MyPeals

## 1. Cuándo usarla
- Generar tests de unidad, boilerplate y documentación (KDoc, docs/<rama>.md).
- Diagnosticar errores de build o de tests (ej.: Espresso 3.5.1 en API 37).
- Refactors acotados siguiendo las convenciones del CLAUDE.md.

## 2. Cuándo ir con cuidado
- Migraciones de base de datos, criptografía y seguridad, concurrencia, lógica de dosis y alarmas.
- En estos casos, la IA propone un plan y una persona lo aprueba antes de escribir código.

## 3. Cómo revisar lo que genera
- Nada entra sin entenderlo: se revisa el código.
- Compilar y ejecutar los tests no basta: rompe el código a propósito y comprueba que algún test falla.
- Desconfía de los rodeos: si la IA esquiva una regla (ej.: crear una `Log` falsa en vez de configurar Gradle), pide la solución estándar.
- Todo cambio llega por PR con revisión humana; el push requiere confirmación.

## 4. Qué no compartir nunca
- Keystore y configuración de firma, `local.properties`, secretos y API keys, datos de usuarios.
- Bloqueado con permisos en `.claude/settings.json`, no solo con instrucciones.

## 5. Candados (defensa en capas)
| Capa | Herramienta | Qué garantiza |
|---|---|---|
| Instrucciones | CLAUDE.md | Arquitectura, convenciones y checklist de terminado |
| Permisos | .claude/settings.json | deny: keystore y secretos; ask: dependencias, Gradle, push |
| Verificación | Tests + `koverVerifyDebug` (mínimo en build.gradle.kts) | El build falla si se rompe algo o baja la cobertura |
| Revisión humana | Ramas + PR | Nada llega a master sin revisión |

## 6. Cómo medimos
- Cobertura de tests de unidad: 49,3 % → 62,8 % (líneas) tras generar los tests de los ViewModels con IA.
- Tiempo por tarea y fallos detectados en revisión: ver notas-ia.md.