# Ajuste autorizado: demos para mañana · 2026-11-04

## Especificación
Los tres escenarios sintéticos usan la fecha actual en Europe/Madrid más un día
de calendario, conservando salida 08:00, ruta demo, duración35 y retrasos0/15/null.
observedAt es el instante del Clock inyectado al crear la evidencia. REFERENCE
conserva sus entradas explícitas. Con Clock fijo las demos siguen siendo reproducibles.
No se mezclan con datos Renfe. UI y AppFunctions reciben la misma política.
Consultar de nuevo o volver a seleccionar una demo actualiza la fecha aunque haya
pasado medianoche; el resultado visible debe mostrar la fecha realmente consultada.

## Plan
Inyectar el Clock compartido en ReproducibleJourneyFixtures. Actualizar el refresco
de entrada demo del ViewModel, textos y metadatos. Mantener APIs e IDs publicados.
Probar los tres modos, cambio de día/año y horario de verano, sin sumar 24 horas
a un instante para representar mañana. No tocar los cambios de renombrado pendientes.

## Tareas
- [x] Cambiar fixtures, inyección y refresco de UI.
- [x] Actualizar KDoc, instrucciones y documentación vigente.
- [x] Ejecutar pruebas JVM, compilar APK y lint; registrar evidencia.

## Verificación
BUILD SUCCESSFUL en45s;196 tareas (79 ejecutadas y117 up-to-date).
Pruebas JVM: domain=77, data=21, platform=21, presentation=54; total=173, sin fallos ni omitidas.
Cuatro regresiones nuevas: mañana en Madrid frente UTC, año nuevo, avance de día/DST y refresco de ViewModel. appfunctions:testDebugUnitTest NO-SOURCE; no se ejecutaron pruebas instrumentadas en esta sesión. assembleDebug y lintDebug correctos; APK instalada con adb install -r en emulator-5554.
