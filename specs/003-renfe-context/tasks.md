# Tasks: UC-003 · Avisos con vigencia
Entrada: spec/plan/research/data-model/contracts del caso. Petición 2026-11-02
autoriza implementar. Checklist requirements8/10; dos hitos humanos se conservan.
Sin hooks en .specify/extensions.yml. MVP: una historia P1, sin ampliar módulos.

## Phase 1: Setup
- [x] T001 Investigar endpoints y registrar consulta real/hashes en specs/003-renfe-context/research.md y live-probe.json (SC-003).
- [x] T002 Refinar alcance/contratos y límites en specs/003-renfe-context/spec.md, plan.md, data-model.md y contracts/context.md.

## Phase 2: Foundational
- [x] T003 Añadir dependencias fijadas en gradle/libs.versions.toml y core-data/build.gradle.kts; generar snapshot reproducible con scripts/update-renfe-stations.py y core-data/src/main/resources/renfe-stations.tsv.
- [x] T004 Definir contratos en core-domain/src/main/kotlin/com/cercafunctions/domain/rail/RailModels.kt: IDs distintos del catálogo; periodos con al menos un extremo y start<end; ausencia no significa cero; timestamp opcional conservado.

## Phase 3: US1 · Contexto real (P1)
Independiente: referencia + estaciones explícitas + respuesta fija demuestra aviso
aplicable/no relacionado/periodo desconocido/caducado/fallo sin modificar llegada.
- [x] T005 [US1] Escribir y ejecutar pruebas red de core-domain/src/test/kotlin/com/cercafunctions/domain/rail/QueryRailContextTest.kt para S1–S7/FR-001–005.
- [x] T006 [US1] Implementar core-domain/src/main/kotlin/com/cercafunctions/domain/rail/QueryRailContext.kt con coincidencia conservadora, frescura y preservación de referencia.
- [x] T007 [US1] Probar e implementar parser/repo/catálogo en core-data/src/test/kotlin/com/cercafunctions/data/rail/RenfeRailRepositoryTest.kt y src/main/kotlin/com/cercafunctions/data/rail/; malformed nunca es vacío, selector incompleto no se degrada a positivo (FR-006).
- [x] T008 [US1] Probar HTTP acotado/cancelación en core-data/src/test/kotlin/com/cercafunctions/data/rail/OkHttpAlertsSourceTest.kt e implementar src/main/kotlin/com/cercafunctions/data/rail/OkHttpAlertsSource.kt con TLS normal, límites1MiB y timeout15s.
- [x] T009 [US1] Probar e implementar core-presentation/src/test/kotlin/com/cercafunctions/presentation/rail/RailContextViewModelTest.kt y src/main/kotlin/com/cercafunctions/presentation/rail/RailContextViewModel.kt; cancelar e invalidar al cambiar entrada.
- [x] T010 [US1] Probar e implementar panel en core-presentation/src/androidTest/kotlin/com/cercafunctions/presentation/rail/RailContextPanelTest.kt y src/main/kotlin/com/cercafunctions/presentation/rail/RailContextPanel.kt, resources/strings y slot estimate/ArrivalPanel.kt.
- [x] T011 [US1] Ensamblar app/src/main/kotlin/com/cercafunctions/app/di/JourneyModule.kt, MainActivity.kt y app/src/main/AndroidManifest.xml (INTERNET); probar interfaz real sin depender de red en app/src/androidTest/kotlin/com/cercafunctions/app/RailContextFlowTest.kt.

## Phase 4: Polish
- [x] T012 Ejecutar pruebas/regresión/lint/APK y consulta real desde APK; registrar evidencia y trazabilidad en specs/003-renfe-context/verification.md y docs/evidence/.
- [x] T013 Explicar HTTP/coroutines/DTO y Flutter en docs/android-for-flutter.md; actualizar docs/architecture.md, README.md, START_HERE.md, AGENTS.md y specs/README.md.
- [x] T014 Aplicar converge contra intención y cerrar snapshot en specs/003-renfe-context/verification.md.

## Dependencies / strategy
T001→T002→T003/004→T005(red)→T006→T007→T008→T009→T010→T011→T012→T013→T014.
Una sola historia completa el MVP. Sin tareas [P]: se valida cada capa antes de
la siguiente. Posible trabajo independiente: preparar JSON sintéticos y textos
UI en archivos distintos, pero aquí se ejecuta secuencialmente. No iniciar UC-004.
