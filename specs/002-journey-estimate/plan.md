# Implementation Plan: UC-002 · Llegada de referencia

**Branch**: `feat/002-journey-estimate` | **Date**: 2026-11-02 | **Spec**: [spec.md](spec.md)

## Summary
Un caso de uso Kotlin puro obtiene trayecto guardado, valida salida y calcula
referencia o resultado reproducible de demostración. UI muestra fuente, fecha,
zona, offset e instante de consulta. No añade predicción ferroviaria.

## Technical Context
Kotlin 2.4.10/JVM 17, Android minSdk 26, Compose/Material3, StateFlow, coroutines,
Hilt ya instalados. java.time y Clock sin nueva biblioteca. DataStore de UC-001
permanece como fuente del trayecto; consulta efímera sin esquema nuevo.
JUnit/coroutines-test, Compose instrumentation y Activity/Hilt real. Cálculo
constante y local; sin red, backend ni métricas artificiales de latencia.

## Constitution Check
Antes y después del diseño: PASS. Spec antes de código; seis módulos; dominio
sin Android; fuentes explícitas; cancelación propagada; pruebas temporales y
evidencia; un único incremento activo. Petición del usuario autoriza construir;
checklist histórico se conserva como revisión documental pendiente de lectura humana.

## Project Structure
```text
core-domain/.../domain/estimate/    modelos, catálogo, QueryJourneyArrival
core-data/.../data/estimate/        ReproducibleJourneyFixtures
core-presentation/.../estimate/     ArrivalViewModel, ArrivalPanel
core-presentation/.../home/         slot para panel solo con trayecto disponible
app/.../di/JourneyModule.kt         Clock, ZoneId, catálogo y caso de uso
app/.../MainActivity.kt             segundo ViewModel con Hilt
specs/002-journey-estimate/          spec, plan, research, data-model, contracts,
                                   quickstart, tasks, verification
```
Los directorios Kotlin conservan sus prefijos `src/main/kotlin/com/cercafunctions/`.
Pruebas en `src/test` de dominio/datos/presentation y `src/androidTest` de
presentation/app. core-platform y core-appfunctions no necesitan código nuevo.

## Decisions and sequence
1. Contratos y pruebas de tiempo, luego implementación y fixtures.
2. Pruebas de ViewModel antes de UI; cancelar trabajo al editar e invalidar
   resultados cuando cambie el trayecto; captura única del reloj.
3. Panel stateless y enlace con Activity; entradas estrictas, offsets explícitos.
4. Probar UI y grafo real en emulador existente API37.2; regresión UC-001, lint y APK.
5. Converge contra intención y registrar evidencia, limitaciones y mentoría.

## Complexity Tracking
Sin excepciones constitucionales ni nuevos módulos/dependencias. Un catálogo
concreto y un contrato de lectura bastan; no motor genérico de escenarios.
