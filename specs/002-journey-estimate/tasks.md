# Tasks: UC-002 · Llegada de referencia

Input: spec, plan, research, data-model y contracts de este directorio.
Autorización: «continua con el siguiente caso de uso», 2026-11-02.
Checklist requirements: 10 elementos, 8 marcados y 2 pendientes de lectura/revisión
humana. Se conserva sin cambios; la petición actual autoriza implementación.
Pruebas solicitadas por FR-006 y constitución; red antes de implementación.

## Phase 1: Setup
- [x] T001 Revisar UC-002, investigar java.time y delimitar alcance en specs/002-journey-estimate/spec.md y research.md.
- [x] T002 Diseñar seis módulos sin dependencias nuevas en specs/002-journey-estimate/plan.md, data-model.md, contracts/arrival.md y quickstart.md.

## Phase 2: Foundational
- [x] T003 Definir tipos, reloj y catálogo en core-domain/src/main/kotlin/com/cercafunctions/domain/estimate/ArrivalModels.kt: offset opcional debe ser válido; llegada y retraso nullable no significan cero; fuente y observedAt obligatorios.

## Phase 3: US1 · Consultar llegada (P1)
Objetivo/MVP: referencia35 → 08:35, demo+15 →08:50 y desconocido sin llegada.
Prueba independiente: catálogo fijo, reloj fijo y consulta desde Activity real.

- [x] T004 [US1] Escribir y ejecutar red de core-domain/src/test/kotlin/com/cercafunctions/domain/estimate/QueryJourneyArrivalTest.kt para S1–S5, pasado, mañana, DST, medianoche y cancelación (FR-001/002/005/006).
- [x] T005 [US1] Implementar core-domain/src/main/kotlin/com/cercafunctions/domain/estimate/QueryJourneyArrival.kt con validación explícita, suma de instantes y captura única de reloj (S1–S5).
- [x] T006 [US1] Probar e implementar catálogo fijo en core-data/src/test/kotlin/com/cercafunctions/data/estimate/ReproducibleJourneyFixturesTest.kt y src/main/kotlin/com/cercafunctions/data/estimate/ReproducibleJourneyFixtures.kt; retraso nulo significa estimación desconocida (S6, FR-003/004, SC-002/003).
- [x] T007 [US1] Probar e implementar core-presentation/src/test/kotlin/com/cercafunctions/presentation/estimate/ArrivalViewModelTest.kt y src/main/kotlin/com/cercafunctions/presentation/estimate/ArrivalViewModel.kt; invalidar resultado al editar, respetar cancelación, formatos y elección de offset.
- [x] T008 [US1] Probar e implementar panel en core-presentation/src/androidTest/kotlin/com/cercafunctions/presentation/estimate/ArrivalPanelTest.kt y src/main/kotlin/com/cercafunctions/presentation/estimate/ArrivalPanel.kt, recursos res/values/strings.xml y slot home/HomeScreen.kt; fuente, reloj, horizonte y campos bloqueados en demo (FR-004).
- [x] T009 [US1] Integrar Hilt/Activity en app/src/main/kotlin/com/cercafunctions/app/di/JourneyModule.kt y MainActivity.kt; verificar grafo real con app/src/androidTest/kotlin/com/cercafunctions/app/ArrivalFlowTest.kt, manteniendo UC-001.

## Phase 4: Polish y cierre
- [x] T010 Ejecutar unitarias, instrumentadas secuenciales, assemble y lint; registrar resultados, cobertura S1–S6 y evidencia en specs/002-journey-estimate/verification.md.
- [x] T011 Explicar java.time/Clock/StateFlow desde Flutter en docs/android-for-flutter.md; actualizar START_HERE.md, AGENTS.md, README.md y specs/README.md.
- [x] T012 Aplicar converge contra spec/plan/tasks, resolver hallazgos y cerrar evidencia en specs/002-journey-estimate/verification.md.

## Dependencias y ejecución
T001→T002→T003→T004(red)→T005→T006→T007→T008→T009→T010→T011→T012.
Una historia y un desarrollador activo; no se necesitan tareas paralelas. Tras T005,
pruebas de fixtures y diseño de pruebas UI pueden prepararse en archivos separados,
pero no se marca [P] porque el orden elegido valida cada capa antes de avanzar.
No se inicia UC-003. Marcar cada tarea solo tras evidencia correspondiente.
