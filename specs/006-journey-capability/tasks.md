# Tasks ·UC-006
Petición2026-11-03 autoriza construir. Checklist humano8/10 sin cambios; sin hooks.
## Phase 1: Setup
- [x] T001 Investigar skill/referencias/API y emulador en specs/006-journey-capability/research.md.
- [x] T002 Refinar spec,plan,data-model,contracts/journey.md y quickstart en specs/006-journey-capability/.
## Phase 2: Foundational
- [ ] T003 Definir core-domain/src/main/kotlin/com/cercafunctions/domain/sharing/JourneySharingRepository.kt y probar orden fail closed/persistencia/errores en core-platform/src/test/kotlin/com/cercafunctions/platform/sharing/DataStoreJourneySharingRepositoryTest.kt antes de implementar adaptador homónimo main; false por defecto.
## Phase 3: US1 ·Consultar trayecto desde consumidor autorizado (P1)
MVP independiente: opt-in UI, descubrir función y consultar fixture sin Activity;
misma llegada/fuente que UI, negativas sin datos y sin calendario/mensajería.
- [ ] T004 [US1] Configurar KSP+Hilt en core-appfunctions/build.gradle.kts y crear JourneyResponse.kt con tipos java.time/KDoc inline según data-model; nullable llegada/retraso no equivale cero.
- [ ] T005 [US1] Escribir pruebas del adaptador en core-appfunctions/src/androidTest/kotlin/com/cercafunctions/appfunctions/JourneyFunctionTest.kt: equivalencia/demos/validación/consent/errores/cancelación antes del cuerpo de BaseJourneyAppFunctionService.kt.
- [ ] T006 [US1] Implementar BaseJourneyAppFunctionService.kt en core-appfunctions/src/main/kotlin/com/cercafunctions/appfunctions/: IO, casos compartidos, validar antes de leer, acceso trusted compat, isEnabled=false, doble gate y errores constantes.
- [ ] T007 [US1] Registrar servicio generado y app_metadata.xml en core-appfunctions/src/main/AndroidManifest.xml y res/xml/; depurar KDoc inmediatamente y verificar XML/KSP/serializadores del APK.
- [ ] T008 [US1] Probar e implementar SharingViewModel.kt y SharingPanel.kt bajo core-presentation/src/{main,test,androidTest}/kotlin/com/cercafunctions/presentation/sharing/ con estado disponible/carga/busy/error y opt-in explícito.
- [ ] T009 [US1] Enlazar app/di/SharingModule.kt, MainActivity.kt y presentation/home/HomeScreen.kt; callback Manager nullable; habilitar sistema antes de guardartrue y revocarfalse antes de deshabilitar, sin VM cruzados.
- [ ] T010 [US1] Verificar UI/grafo persistencia y revocación con app/src/androidTest/kotlin/com/cercafunctions/app/SharingFlowTest.kt; sin agenda ni red.
- [ ] T011 [US1] Verificar denegación IPC real con otro UID en core-appfunctions/src/androidTest/kotlin/com/cercafunctions/appfunctions/UnauthorizedCallerTest.kt, APK app instalado y sin adoptar identidad shell.
- [ ] T012 [US1] Crear scripts/appfunctions-smoke.py para SDK determinista y registrar descubrimiento/invocación/proceso frío/missing/invalid/disabled/consent en specs/006-journey-capability/verification.md.
## Phase 4: Polish
- [ ] T013 Ejecutar regresión/APK/lint/instrumentadas secuenciales y registrar evidencia en specs/006-journey-capability/verification.md.
- [ ] T014 Explicar servicio/KSP/IPC/consent frente a Flutter en docs/android-for-flutter.md; actualizar docs/architecture.md,core-appfunctions/README.md,README.md,START_HERE.md,AGENTS.md y specs/README.md.
- [ ] T015 Aplicar converge y cerrar snapshot UC-006 en specs/006-journey-capability/verification.md.
## Dependencies / strategy
T001→T002→T003→T004→T005(red)→T006→T007→T008→T009→T010→T011→T012→T013→T014→T015.
Una historia P1 constituye MVP. Sin tareas [P]: construcción secuencial por capas;
los textos y pruebas de módulos distintos podrían redactarse independientemente
tras contrato, pero ejecución de emulador siempre secuencial. No iniciar UC-007/008.
