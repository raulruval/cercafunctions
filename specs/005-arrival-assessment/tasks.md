# Tasks: UC-005 · Evaluación de llegada
Autorización: petición2026-11-02. Checklist humano8/10 preservado; no representa
tests ni aprobación de lectura. Sin hooks registrados. MVP: una historia P1.

## Phase 1: Setup
- [x] T001 Investigar Duration/procedencia y comprobar códigos de estaciones en specs/005-arrival-assessment/research.md.
- [x] T002 Refinar spec, plan, data-model y contracts/assessment.md en specs/005-arrival-assessment/.

## Phase 2: Foundational
- [x] T003 Definir AssessmentModels.kt en core-domain/src/main/kotlin/com/cercafunctions/domain/assessment/: ON_TIME>=0, LATE<0, sin títulos, causa y assessedAt obligatorios, evidencia nullable solo si falta entrada.

## Phase 3: US1 · Evaluar explícitamente (P1)
Independiente: mismo compromiso09:00 y llegadas08:50/09:00/09:15/desconocida
dan+10/0/−15/indeterminado, con revalidación y procedencia visibles.
- [x] T004 [US1] Escribir/ejecutar red en core-domain/src/test/kotlin/com/cercafunctions/domain/assessment/AssessArrivalTest.kt para S1–S7, 1ns/fracciones, zona/medianoche y cancelación (FR-001–006).
- [x] T005 [US1] Implementar AssessArrival.kt en core-domain/src/main/kotlin/com/cercafunctions/domain/assessment/ refrescando misma salida y revalidando calendario; floor positivo, −ceil tardío, sin TTL ni envío.
- [x] T006 [US1] Probar e implementar coordinación en core-presentation/src/test/kotlin/com/cercafunctions/presentation/calendar/CalendarAssessmentViewModelTest.kt y src/main/kotlin/com/cercafunctions/presentation/calendar/CalendarViewModel.kt; job/token independiente, invalidación y borrado atómico ante fallo de calendario.
- [x] T007 [US1] Probar e implementar core-presentation/src/androidTest/kotlin/com/cercafunctions/presentation/assessment/AssessmentPanelTest.kt y src/main/kotlin/com/cercafunctions/presentation/assessment/AssessmentPanel.kt y recursos strings; horas completas, fuente, incertidumbre y tramo/margen0.
- [x] T008 [US1] Conectar HomeRoute/HomeScreen en core-presentation/src/main/kotlin/com/cercafunctions/presentation/home/HomeScreen.kt y app/src/main/kotlin/com/cercafunctions/app/di/CalendarModule.kt sin nuevo módulo ni VM con referencias cruzadas.
- [x] T009 [US1] Verificar grafo real/proveedor con app/src/androidTest/kotlin/com/cercafunctions/app/AssessmentFlowTest.kt, fixtures LOCAL propios y sin red.

## Phase 4: Polish
- [x] T010 Ejecutar tests/regresión/APK/lint y prueba manual sintética; registrar aceptación en specs/005-arrival-assessment/verification.md y docs/evidence/.
- [x] T011 Explicar composición de casos/redondeos frente a Flutter en docs/android-for-flutter.md; actualizar docs/architecture.md, README.md, START_HERE.md, AGENTS.md y specs/README.md.
- [x] T012 Aplicar converge y cerrar snapshot en specs/005-arrival-assessment/verification.md.

## Dependencies and delivery
T001→T002→T003→T004(red)→T005→T006→T007→T008→T009→T010→T011→T012.
Sin tareas [P]: ejecución secuencial por capas; textos UI y fixtures de tests
podrían prepararse en archivos independientes después del contrato. US1 completa
MVP. UC-006 no se inicia. No persistencia ni mensajes automáticos.

## Phase 5: Convergence
- [x] T013 Invalidar evaluación al abandonar primer plano en CalendarRoute.kt/CalendarViewModel.kt y comprobar cancelación ON_STOP en CalendarAssessmentViewModelTest.kt, según spec: alcance activado/lifecycle (partial, MEDIUM).
