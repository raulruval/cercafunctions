# Cercanías Copilot

## Contexto y alcance vigente

Leer `START_HERE.md`, `.specify/memory/constitution.md` y `specs/README.md`.
La petición del2026-11-03 autoriza UC-006 además de base/SDD y UC-001–005 previos.
Los seis quedan Verified; UC-007/008 siguen Draft. No iniciar el siguiente caso
sin petición. Ver specs/006-journey-capability/verification.md.
La fase 0 queda preservada en su etiqueta; los documentos de aquella fase son históricos.

## Flujo SDD

- Usar las skills oficiales instaladas en `.agents/skills/speckit-*`.
- Una característica activa en `.specify/feature.json` o `SPECIFY_FEATURE_DIRECTORY`.
  La rama Git no cambia automáticamente ese puntero local ignorado por Git.
- `spec.md` define comportamiento; `plan.md`, diseño; `tasks.md`, trabajo ejecutable.
- Un solo caso en construcción. Mantener estado y evidencia en los archivos del caso.
- Revisar supuestos de borrador antes de construir. Una petición de implementar
  el caso revisado autoriza sus tareas; no pedir confirmación para cada paso.
- No marcar una tarea o criterio completado sin comprobación real. Distinguir
  revisión documental de pruebas de producto y de aprobación humana.
- Conservar cambios locales ajenos; no incluirlos incidentalmente en commits.

## Arquitectura y seguridad

- Mantener seis módulos y dependencias de `docs/architecture.md`.
- `core-domain` y `core-data`: Kotlin/JVM sin Android, Compose, Hilt o AppFunctions.
- UI y capacidades consumen los mismos casos de uso; sin ViewModel en servicios.
- No modificar Rocket-Beer ni incorporar sus artefactos privados.
- AppFunctions: leer la skill oficial Google y sus referencias al trabajar esa frontera.
  API beta01 o posterior; contrastar snippets antiguos con notas de versión.
- Exponer únicamente capacidades ya ofrecidas por UI; no pedir permisos desde servicios.
- Identificar fixtures y procedencia; no extrapolar alertas actuales a mañana.
- Separar lectura de calendario de autorización para compartir datos mínimos.
- Envío de mensajes solo tras confirmar destinatario y contenido exactos.
- No versionar credenciales, calendarios reales, local.properties o entornos Python.

## Verificación y mentoría

Elegir comprobaciones según `docs/sdd-workflow.md`; no compilar por documentos solos.
Base Android: `./gradlew :core-domain:test :app:assembleDebug :app:lintDebug`.
Explicar decisiones Android desde experiencia Flutter/Dart/BLoC y aportar evidencia.
