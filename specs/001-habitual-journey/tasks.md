# Tasks: UC-001 · Trayecto habitual persistido

**Input**: [spec.md](spec.md), [plan.md](plan.md), [data-model.md](data-model.md),
[contracts/journey.md](contracts/journey.md), [research.md](research.md).

**Status**: Draft. 0/12 ejecutadas. La creación de esta lista no inicia construcción.

**Tests**: pedidos expresamente en FR-008. Crear pruebas de comportamiento antes
de su implementación y observar el fallo relevante; no aceptar fallos solo de setup.
Las rutas son relativas a la raíz del repositorio y propuestas para el incremento.

## Phase 1: Setup

- [ ] T001 Fijar DataStore 1.3.0 y dependencias de tests en `gradle/libs.versions.toml`, `core-platform/build.gradle.kts` y `core-presentation/build.gradle.kts`; configurar runner de pruebas Compose sin añadir módulos.

## Phase 2: Foundational

- [ ] T002 Definir lectura/guardado y resultados específicos en `core-domain/src/main/kotlin/com/cercafunctions/domain/journey/JourneyRepository.kt`, `JourneyReadResult.kt` y `JourneySaveResult.kt`, conforme a `contracts/journey.md`; mantener el dominio sin Android.

## Phase 3: User Story 1 · Configurar y conservar trayecto (P1)

**Goal**: Guardar, recuperar y editar un único trayecto con errores explícitos.

**Independent Test**: Guardar 35 minutos, recrear el proceso, recuperar y actualizar a 40.

### Tests

- [ ] T003 [P] [US1] Escribir casos de validación, normalización, lectura ausente/no disponible y cancelación en `core-domain/src/test/kotlin/com/cercafunctions/domain/journey/GetHabitualJourneyTest.kt` y `SaveHabitualJourneyTest.kt`; cubrir límites 0/1/1440/1441 y no invocar guardado ante datos inválidos.
- [ ] T004 [P] [US1] Escribir pruebas con archivo temporal, cierre/recreación del scope, actualización íntegra, datos parciales, fallos inyectados y concurrencia en `core-platform/src/test/kotlin/com/cercafunctions/platform/journey/DataStoreJourneyRepositoryTest.kt`; nunca dos instancias activas sobre el mismo archivo.
- [ ] T005 [US1] Escribir pruebas de carga, edición/cancelación, errores, doble guardado y propagación de cancelación en `core-presentation/src/test/kotlin/com/cercafunctions/presentation/home/HomeViewModelTest.kt`; usar scheduler de coroutines controlado.
- [ ] T006 [US1] Escribir pruebas de formulario y resumen para S1/S3/S4/S5/S7/S8 en `core-presentation/src/androidTest/kotlin/com/cercafunctions/presentation/home/HomeScreenTest.kt`, con estado/callbacks controlados, sin red o calendario.

### Implementation

- [ ] T007 [US1] Implementar validación/normalización en `core-domain/src/main/kotlin/com/cercafunctions/domain/journey/SaveHabitualJourney.kt` y `HabitualJourney.kt`, adaptar `GetHabitualJourney.kt` y `core-data/src/main/kotlin/com/cercafunctions/data/journey/InMemoryJourneyRepository.kt` al contrato; pasar las pruebas T003.
- [ ] T008 [US1] Implementar almacenamiento transaccional y errores en `core-platform/src/main/kotlin/com/cercafunctions/platform/journey/DataStoreJourneyRepository.kt`; enlazar una instancia de DataStore por archivo en `app/src/main/kotlin/com/cercafunctions/app/di/JourneyModule.kt`, sustituyendo el binding en memoria; pasar T004.
- [ ] T009 [US1] Adaptar estados y acciones en `core-presentation/src/main/kotlin/com/cercafunctions/presentation/home/HomeUiState.kt` y `HomeViewModel.kt`, conservando borrador durante errores/rotación y evitando guardados simultáneos; pasar T005.
- [ ] T010 [US1] Añadir configurar/editar/guardar/cancelar/reintentar en `core-presentation/src/main/kotlin/com/cercafunctions/presentation/home/HomeScreen.kt` y `core-presentation/src/main/res/values/strings.xml`; actualizar Preview y pasar T006.

**Checkpoint**: S1–S8 comprobables y pruebas específicas en verde; falta validación del APK real.

## Phase 4: Polish & Cross-Cutting Concerns

- [ ] T011 Ejecutar pruebas, compilación/lint y relanzamiento real de `quickstart.md`; registrar comandos, resultados, entorno y evidencia S1–S8 en `specs/001-habitual-journey/verification.md`, diferenciando pruebas automáticas de pasos manuales.
- [ ] T012 Revisar conformidad spec/código con `$speckit-converge`, cerrar hallazgos, actualizar `specs/README.md` y la explicación de DataStore/lifecycle en `docs/android-for-flutter.md`; guardar un commit/snapshot local solo cuando la evidencia permita Verified.

## Dependencies & Execution Order

T001 → T002 → pruebas T003/T004/T005/T006 → implementación T007 → T008 → T009 → T010 → T011 → T012.
T005/T006 se escriben contra el contrato de UI definido y pueden fallar por comportamiento
ausente; ajustar firmas mínimas al construir sin cambiar la aceptación. Cada implementación
se valida con sus pruebas. T008 depende de T004/T007; T009 de T005/T007; T010 de T006/T009.

### Parallel Opportunities

Tras T002, T003 y T004 trabajan en archivos independientes y podrían redactarse en paralelo.
La ejecución por defecto es secuencial; no se necesita un equipo de agentes para este incremento.
El atributo `[P]` solo indica esa independencia, no autoriza ejecución por sí mismo.

## Traceability

Los IDs FR/SC se interpretan dentro de UC-001, no globalmente.

| Requisito | Tareas | Evidencia prevista |
| --- | --- | --- |
| FR-001, FR-002 | T002, T003, T007, T010 | Validación y normalización; S1/S3/S4 |
| FR-003 | T004, T008, T011 | Archivo recreado y relanzamiento; S2 |
| FR-004 | T004, T005, T008, T009 | Lectura ausente y fallos; S6 |
| FR-005 | T005, T006, T009, T010 | Edición/cancelación; S7/S8 |
| FR-006 | T005, T006, T009, T010 | Doble pulsación y fallo; S5 |
| FR-007 | T001, T008, T010, T011 | Sin permisos ni red; etiqueta de referencia |
| FR-008 | T003, T004, T005, T006, T011 | Cancelación, persistencia e interacción |
| SC-001 | T004, T011 | Reinicio y reemplazo íntegro |
| SC-002 | T003, T004, T006, T011 | Inválidos, fallos y ningún éxito falso |
| SC-003 | T006, T011 | S1–S8 sin dependencias externas |

## Implementation Strategy

MVP: la única historia US1 completa, incluida persistencia y errores; no basta guardar
en memoria ni presentar un formulario. Después de la autorización de construcción,
seguir tareas y verificar. Si surge un cambio de alcance, revisar spec/plan/tareas
afectadas antes de seguir. No añadir tareas de Renfe, calendario o AppFunctions a UC-001.
