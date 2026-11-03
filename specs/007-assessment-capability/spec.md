# Feature Specification: UC-007 · Compartir una evaluación mínima del compromiso autorizado

**Feature Branch**: `feat/007-assessment-capability`

**Created**: 2026-11-01

**Status**: Verified — implementación y evidencia2026-11-03; checklist humano intacto.

**Input**: Exponer la evaluación del compromiso compartiendo únicamente los datos estrictamente necesarios.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Compartir una evaluación mínima del compromiso autorizado (Priority: P1)

Como usuario quiero que el asistente evalúe una reunión que he autorizado sin recibir mi agenda completa.

**Why this priority**: Es el resultado observable que cierra UC-007; sus dependencias se detallan abajo.

**Independent Test**: Autorizar una ocurrencia de prueba, obtener un identificador temporal y consultar desde agente; intentar reutilizarlo tras revocar o expirar.

**Acceptance Scenarios**:

1. **S1**: Dada una reunión elegida en UI, cuando autorizo compartir su evaluación durante 30 minutos, entonces recibo una referencia opaca limitada a esa ocurrencia.
2. **S2**: Dada esa referencia válida y lectura de calendario permitida, cuando el agente consulta, entonces obtiene horarios mínimos, margen y evidencia sin título, notas, asistentes o emails.
3. **S3**: Dada referencia expirada, desconocida o revocada, cuando se invoca, entonces no se divulga el compromiso.
4. **S4**: Dado permiso revocado o evento modificado desde el consentimiento, cuando se invoca, entonces se requiere volver a la UI para seleccionar y autorizar de nuevo.

### Edge Cases

Recreación de proceso, fallo al recuperar autorización, cambio del reloj del dispositivo y acceso desde un consumidor no autorizado. Cualquier fallo de autorización deniega datos.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Separar permiso local de lectura de autorización explícita para compartir la evaluación de una única ocurrencia.
- **FR-002**: Usar referencia opaca limitada a esa ocurrencia, sin aceptar identificadores arbitrarios de eventos para enumerar agenda.
- **FR-003**: Limitar vigencia inicial a 30 minutos o comienzo del evento, lo que ocurra antes; permitir revocación explícita en UI.
- **FR-004**: Reutilizar la evaluación UC-005 y devolver solo horarios autorizados, margen/retraso, procedencia y estado.
- **FR-005**: Invalidar autorización si cambia el compromiso o se revoca el permiso; no intentar conseguir permiso en segundo plano.
- **FR-006**: Verificar equivalencia con UI y ausencia de campos privados, además de expiración, revocación y referencias inválidas.

### Key Entities

Autorización de divulgación: referencia opaca, ocurrencia, versión del compromiso, caducidad y revocación. Resumen de evaluación mínimo.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Ninguna respuesta incluye título, descripción, asistentes o correo.
- **SC-002**: Todas las referencias inválidas, revocadas o expiradas fallan sin divulgar el compromiso.
- **SC-003**: La autorización válida permite el mismo resultado de UC-005 con UI cerrada.

## Assumptions

Depende de UC-005/006. La vigencia y selección previa son propuestas de POC a revisar al activar este caso. No se diseña un sistema general de permisos, autenticación o agenda remota.

**Dependencias**: UC-005 y UC-006.

La precisión del borrador es suficiente para revisar alcance; las decisiones propuestas
se refinan al activar el caso. Un escenario escrito no demuestra que ya funcione.


## Alcance activado ·2026-11-03
Una autorización activa, emitida solo por UI para selección y detalles locales
revalidados. Nueva emisión sustituye y revoca referencia anterior. Token CSPRNG256bits
Base64URL; solo hash y selección/versión mínima persisten. Token recién emitido
en memoria, copia solo explícita; tras proceso frío se puede revocar, no recuperar
rawtoken. No guardar SavedState ni registrar tokens en logs/capturas versionadas.

Versión observable: EVENT_ID+BEGIN+END+título+nombre de calendario, que ya lee UC-004.
HMAC con el token detecta cambios sin persistir textos. No se leen notas/asistentes
para versionar. No se promete detectar campos no proyectados ni divergencia
Events/Instances. Permiso ausente observado invalida duraderamente la autorización;
no se promete detectar revocación y reconcesión ocurridas entre comprobaciones
con proceso muerto. Fallo de proveedor/autorización deniega y requiere reautorizar.

Límite min(30min,comienzo), comprobado con Clock+elapsedRealtime+bootCount. Cambio
de boot, elapsed atrás, cualquier límite vencido o desviación wall/elapsed>5s
invalida. Los5s toleran ajuste pequeño y nunca amplían el límite monotónico.
Sin alarmas/receivers o sistema de permisos general. Estado de habilitación del
sistema puede quedar activo tras caducar hasta la próxima comprobación, pero el
token no permite ejecutar con éxito más allá del límite.

evaluateAuthorizedCommitment acepta referencia y salida/zona/escenario/offset;
nunca eventId ni selección enviada por consumidor. Autorización propia independiente
del opt-in de trayecto UC-006. Revalida token/versión/permiso antes y después de
componer QueryJourneyArrival y AssessArrival. Indeterminado de llegada no divulga
horarios de reunión; errores de autorización son uniformes sin confirmar existencia.
DTO solo estado, causa, margen, horarios/zonas mínimos, fuente/escenario y fechas
de evidencia/evaluación. No nombres de trayecto, IDs, título, calendario o token.
Comparación estación–reunión0 y margen adicional0 se describen en contrato.
Un entry point, segunda función disabled-default/trusted compat. Sin mensajes.
