# Cercanías Copilot Constitution

## Core Principles

### I. Comportamiento especificado antes de implementar

Cada incremento DEBE tener `spec.md` con escenarios verificables. Su `plan.md`
y `tasks.md` DEBEN delimitar la implementación. Una petición de especificar
no autoriza a implementar. Una instrucción posterior de implementar el caso
revisado basta; no pedir aprobación de nuevo para cada tarea ya autorizada.

### II. Un dominio compartido e independiente

UI y AppFunctions DEBEN ejecutar los mismos casos de uso. `core-domain`
DEBE permanecer Kotlin/JVM sin Android, Compose, Hilt ni AppFunctions.
Se mantienen las seis fronteras actuales; cualquier cambio de módulos DEBE
justificarse en el plan. Los originales Rocket-Beer NO se modifican.

### III. Evidencia temporal y datos mínimos

Los resultados ferroviarios DEBEN distinguir fuente, vigencia, horario,
estimación, referencia, fixture y desconocido. Una alerta actual NO permite
inventar retrasos para mañana. Lectura de calendario y divulgación al agente
DEBEN tratarse como autorizaciones diferentes. Los mensajes DEBEN requerir
confirmación explícita de destinatario y contenido antes del envío.

### IV. Verificación proporcional y trazable

Cada criterio de aceptación DEBE vincularse a una comprobación prevista y,
al entregar, a evidencia real. Reglas de negocio llevan pruebas de dominio;
persistencia/permisos/IPC llevan pruebas de integración cuando se implementen.
La cancelación de coroutines DEBE propagarse. No declarar verificado lo que
solo está especificado, ni instalar supresiones para ocultar fallos.

### V. Incrementos pequeños y aprendizaje

Solo un caso de uso estará en construcción. No añadir backend, mapas,
mensajería propia, módulos o frameworks genéricos sin necesidad del alcance.
Cada entrega DEBE explicar el concepto Android relevante desde Flutter/BLoC,
cómo probarlo y las limitaciones observadas. Especificaciones y evidencias
se versionan con Git; el chat complementa esos artefactos.

## Restricciones del producto

POC local para un usuario y un trayecto habitual. Kotlin, Compose/Material 3,
ViewModel/StateFlow, Coroutines y Hilt. Calendario mediante CalendarContract;
capacidades mediante la skill oficial AppFunctions y ejemplos Google.
Credenciales, eventos reales y archivos locales de SDK NO se versionan.
La arquitectura de fase 0 sigue siendo revisable; arrancar SDD no la ratifica
íntegramente ni convierte la POC en un SDK empresarial publicado.

## Flujo de desarrollo

Definición → especificación → plan → tareas → construcción → validación → cierre.
`specs/README.md` registra el estado; `docs/sdd-workflow.md` define el método.
Una tarea se marca completada con evidencia, no por intención. Las pruebas
y comandos se eligen según el cambio; documentos solos no exigen compilar Android.
Antes de código se revisan alcance, contratos, riesgos y pruebas previstas.
Una autorización de implementación ya dada se respeta durante todo el incremento.

## Governance

Esta constitución recoge los límites solicitados por el usuario para trabajar
en este repositorio. No sustituye sus instrucciones. Se cambia mediante diff
explícito, con motivo y efecto en specs/planes. Versionado: MAJOR para romper
principios, MINOR para ampliarlos y PATCH para aclarar redacción.
La adopción documental inicial es 2026-11-01. Las specs de producto siguen
Draft hasta su revisión; un checklist documental no es una aprobación humana.

**Version**: 1.0.0 | **Ratified**: 2026-11-01 | **Last Amended**: 2026-11-01
