# Tasks UC-007
Petición2026-11-03 autoriza construir; checklist humano8/10 intacto, sin hooks.
## Phase 1: Setup
- [x] T001 Investigar tiempos/versión/AppFunctions en specs/007-assessment-capability/research.md.
- [x] T002 Refinar spec,plan,data-model,contracts y quickstart en specs/007-assessment-capability/.
## Phase 2: Foundation
- [ ] T003 Crear modelos/contratos y tests red en core-domain/src/{main,test}/kotlin/com/cercafunctions/domain/authorization/ para30min/comienzo/token/versión/permiso/rollback/boot/cancelación/CAS.
- [ ] T004 Implementar AssessmentAuthorizations.kt y AuthorizedAssessment.kt en core-domain/.../authorization/ reutilizando UC-005; hash/HMAC sin textos persistidos.
## Phase 3: US1 ·Evaluación autorizada mínima (P1)
Independiente: autorizar ocurrencia09:00, referencia opaca, salida08:40 duración35→15min
tarde sin título; revocar/caducar/cambiar ocurrencia→Denied sin horarios.
- [ ] T005 [US1] Escribir tests de persistencia/orden/CAS/corrupción en core-platform/src/test/kotlin/com/cercafunctions/platform/authorization/DataStoreGrantRepositoryTest.kt antes de adaptador.
- [ ] T006 [US1] Implementar DataStoreGrantRepository.kt/AndroidGrantClock.kt en core-platform y app/di/AuthorizationModule.kt con callback habilitación propio, sin store/módulo nuevo.
- [ ] T007 [US1] Probar e implementar AssessmentSharingViewModel.kt bajo core-presentation/src/{main,test}/kotlin/com/cercafunctions/presentation/authorization/; emisión/revocar/status y rawtoken solo memoria/ON_STOP.
- [ ] T008 [US1] Probar/implementar AssessmentSharingPanel.kt en presentation/main/androidTest y enlazar HomeScreen.kt/MainActivity.kt sin VMs cruzados, copia explícita y alcance visible.
- [ ] T009 [US1] Escribir AssessmentFunctionTest.kt en core-appfunctions/src/androidTest/kotlin/com/cercafunctions/appfunctions/ para equivalencia, privacidad, gates independientes, entradas inválidas y cancelación.
- [ ] T010 [US1] Implementar AssessmentResponse.kt y evaluateAuthorizedCommitment en BaseJourneyAppFunctionService.kt; refinar KDoc/metadata inmediatamente y verificar XML/APK de dos funciones.
- [ ] T011 [US1] Verificar UI/Hilt/proveedor real con fixture propia en app/src/androidTest/kotlin/com/cercafunctions/app/AuthorizationFlowTest.kt; nunca agenda personal en evidencia.
- [ ] T012 [US1] Extender UnauthorizedCallerTest.kt en core-appfunctions/androidTest a ambas funciones sin adoptar shell y UID distinto.
- [ ] T013 [US1] Extender scripts/appfunctions-smoke.py con assessment stdin y registrar IPC/proceso frío/revocación/permiso sin rawtokens en specs/007-assessment-capability/verification.md.
## Phase 4: Polish
- [ ] T014 Ejecutar regresión/APK/lint/instrumentadas secuenciales y evidencia en specs/007-assessment-capability/verification.md.
- [ ] T015 Explicar capacidades temporales/IPC/reloj frente a Flutter en docs/android-for-flutter.md; actualizar docs/architecture.md,README,START_HERE,AGENTS,core-appfunctions/README y specs/README.md.
- [ ] T016 Aplicar converge y cerrar snapshot en specs/007-assessment-capability/verification.md.
## Dependencies / delivery
T001→T002→T003(red)→T004→T005(red)→T006→T007→T008→T009→T010→T011→T012→T013→T014→T015→T016.
Una historia P1 completa MVP; sin tareas[P], emulador siempre secuencial. Tras contrato
pueden redactarse tests de módulos distintos independientemente. UC-008 no se inicia.
