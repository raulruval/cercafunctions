# Mapa de casos de uso y seguimiento

Fecha inicial: 2026-11-01. Este archivo es el tablero versionado del proyecto.
Base ejecutable: etiqueta `phase-0-base`. Adopción SDD: etiqueta `sdd-inception-v1`.

| Caso | Resultado de usuario | Dependencias | Estado | Plan / construcción |
| --- | --- | --- | --- | --- |
| UC-001 | [Configurar y conservar mi trayecto habitual](001-habitual-journey/spec.md) | Base phase-0-base. | Verified | 12/12; ver [evidencia](001-habitual-journey/verification.md) |
| UC-002 | [Consultar llegada de referencia y escenarios reproducibles](002-journey-estimate/spec.md) | UC-001. | Verified | 12/12; ver [evidencia](002-journey-estimate/verification.md) |
| UC-003 | [Consultar contexto ferroviario real con vigencia explícita](003-renfe-context/spec.md) | UC-002. | Verified | 14/14; ver [evidencia](003-renfe-context/verification.md) |
| UC-004 | [Elegir un compromiso del calendario del dispositivo](004-calendar-selection/spec.md) | Base Android; independiente de UC-001–003. | Verified | 14/14; ver [evidencia](004-calendar-selection/verification.md) |
| UC-005 | [Evaluar llegada frente al compromiso seleccionado](005-arrival-assessment/spec.md) | UC-002 y UC-004; UC-003 como contexto adicional. | Verified | 13/13; ver [evidencia](005-arrival-assessment/verification.md) |
| UC-006 | [Publicar la consulta de trayecto como capacidad](006-journey-capability/spec.md) | UC-002 y UC-003. | Draft | Se preparan al activar |
| UC-007 | [Compartir una evaluación mínima del compromiso autorizado](007-assessment-capability/spec.md) | UC-005 y UC-006. | Draft | Se preparan al activar |
| UC-008 | [Orquestar propuesta a Ana con confirmación explícita](008-cross-app-demo/spec.md) | UC-006 y UC-007; ChatApp y Testing Agent oficiales. | Draft | Se preparan al activar |

## Orden y dependencias

```mermaid
flowchart LR
    U1[001 Configurar] --> U2[002 Estimar]
    U2 --> U3[003 Contexto Renfe]
    U2 --> U5[005 Evaluar]
    U4[004 Elegir calendario] --> U5
    U3 --> U6[006 Capacidad trayecto]
    U2 --> U6
    U5 --> U7[007 Capacidad evaluación]
    U6 --> U7
    U6 --> U8[008 Demo entre apps]
    U7 --> U8
```

La numeración propone el orden de aprendizaje; el diagrama expresa dependencias
reales. UC-004 puede prepararse de forma independiente. No desarrollamos ocho
capas por separado: cada caso acaba con comportamiento comprobable desde UI o,
en las últimas etapas, desde el consumidor externo además de la UI existente.

## Cómo interpretar el estado

- **Draft**: contrato propuesto; pendiente de revisión de alcance/supuestos.
- **Ready**: spec, plan y tareas revisados y petición de construcción recibida.
- **In progress**: se ejecutan sus tareas, como máximo un caso a la vez.
- **Verified**: aceptación satisfecha y evidencia registrada en `verification.md`.
- **Blocked**: obstáculo real encontrado, con causa y acción pendiente documentadas.

Estado al 2026-11-02: **UC-001–005 Verified**, **65/65 tareas completadas** y **5/8 casos Verified**. Las casillas
marcadas de `checklists/requirements.md` solo indican revisión del documento.
No representan tests ejecutados ni aprobación humana. Al implementar, actualizar
estado y tareas en el mismo commit que su código/evidencia.

## Recorrido de aprendizaje

- **UC-001**: Persistencia, lifecycle y ViewModel frente a Cubit.
- **UC-002**: java.time, Clock y coroutines testeables.
- **UC-003**: Repositorios, red, DTO y mapeo a dominio.
- **UC-004**: Permisos, ContentResolver, CalendarContract y recurrencias.
- **UC-005**: Casos de uso puros y pruebas de límites temporales.
- **UC-006**: AppFunctions, KSP, servicios, serialización e IPC.
- **UC-007**: Consentimiento de divulgación, frontera de datos y ciclo de vida sin interfaz.
- **UC-008**: Descubrimiento entre apps, privilegios y comprobación de efectos.

## Contratos comunes a todas las specs

- «Mañana» = siguiente fecha local en la zona mostrada; nunca sumar ciegamente 24 horas.
- Hora de salida y compromiso se comparan como instantes, conservando contexto de zona.
- Duración de referencia, horario programado, predicción real y fixture son conceptos distintos.
- Una evaluación basada en referencia no garantiza la puntualidad real del tren.
- Lectura de calendario no implica permiso para divulgarlo a un agente.
- Envío requiere confirmación de destinatario y texto exactos; rechazo implica cero envíos.
- Los detalles nuevos de cada borrador se revisan al activarlo; no se consideran aprobados por generar este mapa.

## Próxima sesión

UC-005 queda cerrado en `uc-005-v1`. El siguiente caso propuesto es UC-006:
revisar skill Google, contratos AppFunctions y descubrimiento del trayecto.
No se ha iniciado. Ver [START_HERE](../START_HERE.md) y las evidencias de cada caso.
