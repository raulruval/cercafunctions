# Tasks: UC-004 · Calendario local
Petición 2026-11-02 autoriza construir este caso. Checklist documental8/10,
dos hitos humanos abiertos y conservados. Sin hooks registrados. Una historia P1.

## Phase 1: Setup
- [x] T001 Investigar proveedor, permisos, recurrencia y pruebas en specs/004-calendar-selection/research.md.
- [x] T002 Revisar alcance y contratos en specs/004-calendar-selection/spec.md, plan.md, data-model.md y contracts/calendar.md.

## Phase 2: Foundational
- [x] T003 Definir modelos/puerto en core-domain/src/main/kotlin/com/cercafunctions/domain/calendar/CalendarModels.kt: eventId>0; end>=begin; selección dentro de día; título no identifica; sin persistencia.
- [x] T004 Preparar runner/dependencias de pruebas en core-platform/build.gradle.kts y fixtures aislados en core-platform/src/androidTest/kotlin/com/cercafunctions/platform/calendar/.

## Phase 3: US1 · Elegir compromiso (P1)
MVP independiente: permiso explícito, día local, lista de ocurrencias, elección
manual y revalidación. No depende de trayecto/Renfe ni de cuenta Google.
- [x] T005 [US1] Escribir/ejecutar red en core-domain/src/test/kotlin/com/cercafunctions/domain/calendar/CalendarUseCasesTest.kt para S2–S6, día23/25h, zona, identidad y retirada de selección.
- [x] T006 [US1] Implementar GetCalendarDay.kt y RevalidateCalendarSelection.kt en core-domain/src/main/kotlin/com/cercafunctions/domain/calendar/ (FR-002/004/005).
- [x] T007 [US1] Probar e implementar core-platform/src/main/kotlin/com/cercafunctions/platform/calendar/AndroidCalendarRepository.kt mediante src/androidTest/kotlin/com/cercafunctions/platform/calendar/AndroidCalendarRepositoryTest.kt; filtros, races de permiso, Cursor/cancelación y fuente ausente (FR-001/003/006).
- [x] T008 [US1] Verificar proveedor real con src/androidTest/kotlin/com/cercafunctions/platform/calendar/CalendarProviderTest.kt: RRULE/EXDATE, títulos iguales, zona y borrado/movido; solo fixtures creados y limpiados por ID.
- [x] T009 [US1] Probar e implementar core-presentation/src/test/kotlin/com/cercafunctions/presentation/calendar/CalendarViewModelTest.kt y src/main/kotlin/com/cercafunctions/presentation/calendar/CalendarViewModel.kt; no autoelegir, invalidar y cancelar, pérdida de acceso.
- [x] T010 [US1] Probar e implementar core-presentation/src/androidTest/kotlin/com/cercafunctions/presentation/calendar/CalendarPanelTest.kt y src/main/kotlin/com/cercafunctions/presentation/calendar/CalendarPanel.kt, CalendarRoute.kt y recursos strings; permiso contextual, lifecycle y Ajustes.
- [x] T011 [US1] Integrar app/src/main/kotlin/com/cercafunctions/app/di/CalendarModule.kt, MainActivity.kt, manifest READ y home/HomeScreen.kt; probar grafo real con app/src/androidTest/kotlin/com/cercafunctions/app/CalendarFlowTest.kt.

## Phase 4: Polish
- [x] T012 Ejecutar tests/regresión/APK/lint y rechazo/concesión/revocación reales; registrar evidencia sintética en specs/004-calendar-selection/verification.md y docs/evidence/; preparar scripts/calendar-demo-fixture.py para la demo local opcional.
- [x] T013 Explicar ContentResolver/Instances/permisos frente a Flutter en docs/android-for-flutter.md; actualizar docs/architecture.md, START_HERE.md, README.md, AGENTS.md y specs/README.md.
- [x] T014 Aplicar converge y cerrar snapshot/evidencia en specs/004-calendar-selection/verification.md.

## Dependencies and delivery
T001→T002→T003/004→T005(red)→T006→T007→T008→T009→T010→T011→T012→T013→T014.
Se ejecuta secuencialmente. Fixtures de proveedor y textos UI podrían prepararse
en archivos diferentes, pero no marcamos [P] porque se valida primero cada capa.
Solo US1 completa el MVP. No iniciar UC-005 ni publicar AppFunctions en este caso.
