# Snapshot · adopción SDD

Fecha: 2026-11-01. Rama: `chore/sdd-foundation`. Etiqueta: `sdd-inception-v1`.

## Alcance entregado

- Spec Kit 1.2.5 oficial, integración Codex y scripts PowerShell.
- Diez skills instaladas sin modificar su contenido; eventos automáticos desactivados.
- Constitución 1.0.0 basada en instrucciones del usuario y límites de la POC.
- Ocho casos de uso en Draft, con escenarios, requisitos y criterios de aceptación.
- UC-001 con plan, decisiones, modelo/contrato, guía de verificación futura y 12 tareas.
- START_HERE como lectura breve, tablero versionado y método de trabajo documentado.

No se han instalado Conductor ni el motor AWS AI-DLC. No se ha creado una skill
personal para sustituir las instrucciones oficiales. No se modifica código Android.

## Verificación de esta entrega

- Plantillas de constitución y spec resueltas mediante script oficial.
- `setup-plan.ps1 -Json`: caso UC-001 localizado y plantilla creada correctamente.
- `setup-tasks.ps1 -Json`: diseño localizado y plantilla de tareas resuelta.
- `check-prerequisites.ps1 -Json -RequireTasks -IncludeTasks`: correcto; encuentra
  research.md, data-model.md, contracts/, quickstart.md y tasks.md.
- Revisión de 28 documentos Markdown: enlaces locales válidos; 8 specs con secciones,
  IDs únicos dentro de cada caso, supuestos y escenarios; 12 tareas pendientes con
  rutas y trazabilidad a todos los requisitos de UC-001.
- Hashes de las diez skills coinciden con el manifest instalado por Spec Kit.
- Confirmado puntero local UC-001, ignorado por Git, y ausencia de eventos configurados.
- Comparación con HEAD: ningún archivo de código Kotlin, XML o build de producto modificado.

La comprobación estructural se ejecutó con un script temporal fuera del repositorio.
No se añade una herramienta propia de SDD. También se revisaron manualmente límites,
dependencias y consistencia de aceptación. No se afirma que se haya ejecutado la skill
de análisis completo, un flujo automático de agentes o pruebas de funcionalidades futuras.

No se repite Gradle por cambios solo documentales. La evidencia de compilación y
emulador anterior pertenece a fase 0; UC-001 continúa sin implementar.

## Cambios locales preexistentes

Al empezar, Android Studio había añadido `org.gradle.tooling.parallel=true` a
`gradle.properties` y creado `gradle/gradle-daemon-jvm.properties`.
Se conservan intactos y quedan fuera del commit de adopción SDD. El árbol local
puede seguir mostrándolos pendientes; no significan cambios incompletos de este incremento.

## Próximo punto de revisión

Revisar los supuestos de UC-001 en START_HERE y su spec. Los demás borradores se
refinan progresivamente al activarlos. El siguiente trabajo de producto será
construir UC-001 cuando el usuario lo solicite tras esa revisión.
