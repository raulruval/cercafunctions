# Feature Specification: UC-007 · Compartir una evaluación mínima del compromiso autorizado

**Feature Branch**: `chore/sdd-foundation` (borrador; rama de construcción por crear)

**Created**: 2026-11-01

**Status**: Draft — pendiente de revisión; ninguna implementación autorizada por este documento.

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
