# Feature Specification: UC-008 · Orquestar propuesta a Ana con confirmación explícita

**Feature Branch**: `chore/sdd-foundation` (borrador; rama de construcción por crear)

**Created**: 2026-11-01

**Status**: Draft — pendiente de revisión; ninguna implementación autorizada por este documento.

**Input**: Demostrar el flujo Copilot → evaluación → ChatApp oficial → propuesta revisada → envío confirmado.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Orquestar propuesta a Ana con confirmación explícita (Priority: P1)

Como usuario quiero que un agente prepare una propuesta a Ana cuando exista retraso previsto y que me deje decidir el envío.

**Why this priority**: Es el resultado observable que cierra UC-008; sus dependencias se detallan abajo.

**Independent Test**: Ejecutar cadena determinista con retraso y destinatario de prueba; rechazar una propuesta y aceptar otra, comprobando efectos en ChatApp.

**Acceptance Scenarios**:

1. **S1**: Dada evaluación sin retraso, cuando se ejecuta el flujo, entonces no se prepara ni envía un mensaje de aplazamiento.
2. **S2**: Dado retraso previsto de 15 minutos y una Ana de prueba inequívoca, cuando se orquesta, entonces se presenta destinatario, texto y propuesta de nueva hora antes del envío.
3. **S3**: Dada la propuesta visible, cuando rechazo o no confirmo, entonces se observa cero invocaciones de envío y cero mensajes nuevos.
4. **S4**: Dado destinatario y texto exactos revisados, cuando confirmo expresamente, entonces se realiza una invocación de envío a ChatApp y se verifica el mensaje en su almacén/UI.
5. **S5**: Dados varios resultados para Ana o una estimación desconocida, cuando se ejecuta, entonces se pide desambiguación o se informa de incertidumbre, sin envío.
6. **S6**: Dado que el agente LLM no está disponible o no asegura pausa de confirmación, cuando se demuestra, entonces se usa invocación manual determinista con revisión humana y se documenta el alcance.

### Edge Cases

Contacto ausente/ambiguo, usuario edita el texto, caída de invocación tras envío y agente sin privilegios. Texto o destinatario modificados requieren revisión de la nueva propuesta.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Usar ChatApp y Testing Agent oficiales, verificando privilegios y disponibilidad de descubrimiento/invocación.
- **FR-002**: Resolver el destinatario mediante la capacidad oficial y no atribuir un contacto distinto al nombre Ana.
- **FR-003**: Preparar texto con retraso previsto y propuesta de hora, sin incluir detalles privados del calendario.
- **FR-004**: Requerir confirmación explícita del destinatario y contenido exactos; no se considera confirmación una instrucción del modelo a sí mismo.
- **FR-005**: No reintentar envíos de resultado incierto automáticamente ni repetir un envío por una segunda pulsación de confirmación.
- **FR-006**: Demostrar rechazo y aceptación con evidencia; separar cadena determinista de intento LLM y envío local del ejemplo de entrega en red.
- **FR-007**: Documentar un fixture mínimo de contacto en la copia de demo si Ana no existe; no construir mensajería propia.

### Key Entities

Propuesta pendiente: destinatario resuelto, texto exacto, hora propuesta y estado de confirmación. Evidencia de invocación y efecto.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Rechazo/no confirmación producen cero envíos en la prueba determinista.
- **SC-002**: Aceptación produce una sola invocación y un mensaje verificable en ChatApp.
- **SC-003**: La evidencia diferencia intento LLM, comunicación real entre procesos y almacenamiento local del ejemplo.

## Assumptions

Depende de UC-006/007 y de las aplicaciones oficiales instaladas. El proveedor de ChatApp no impone por sí mismo confirmación: la demo debe imponerla desde el consumidor o mediante depuración manual. No se promete una orquestación LLM autónoma disponible en todas las imágenes.

**Dependencias**: UC-006 y UC-007; ChatApp y Testing Agent oficiales.

La precisión del borrador es suficiente para revisar alcance; las decisiones propuestas
se refinan al activar el caso. Un escenario escrito no demuestra que ya funcione.
