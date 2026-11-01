# Feature Specification: UC-003 · Consultar contexto ferroviario real con vigencia explícita

**Feature Branch**: `chore/sdd-foundation` (borrador; rama de construcción por crear)

**Created**: 2026-11-01

**Status**: Draft — pendiente de revisión; ninguna implementación autorizada por este documento.

**Input**: Consultar estado del trayecto e incidencias usando Renfe Data cuando sus datos sean aplicables.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consultar contexto ferroviario real con vigencia explícita (Priority: P1)

Como usuario quiero conocer las incidencias relevantes y qué permiten concluir para mi viaje.

**Why this priority**: Es el resultado observable que cierra UC-003; sus dependencias se detallan abajo.

**Independent Test**: Probar datos públicos y respuestas reproducibles con alerta aplicable, alerta no relacionada, fallo de fuente y datos caducados.

**Acceptance Scenarios**:

1. **S1**: Dada una alerta que corresponde inequívocamente a mi trayecto y periodo, cuando consulto, entonces veo la alerta y su fuente/vigencia.
2. **S2**: Dada una alerta actual sin evidencia cuantitativa para mañana, cuando consulto mañana, entonces no se añade un retraso inventado a la referencia.
3. **S3**: Dada una estación o servicio que no se puede relacionar con certeza, cuando consulto, entonces se informa de cobertura no resuelta.
4. **S4**: Dada una fuente inaccesible o caducada, cuando consulto, entonces veo contexto no disponible con la causa; una referencia o fixture conservan su etiqueta propia.
5. **S5**: Dado un horario programado válido para la fecha y servicio, cuando se usa para calcular llegada, entonces aparece como programado y no como predicción.

### Edge Cases

Red caída, formatos incompletos, periodo desconocido, alerta de accesibilidad, horas programadas posteriores a 24:00 y nombres de estación ambiguos.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Consultar recursos públicos de Renfe y acotar las alertas a trayecto y periodo cuando exista correspondencia verificable.
- **FR-002**: Mostrar instante de consulta, vigencia conocida y procedencia; ante ausencia de vigencia no afirmar actualidad demostrada.
- **FR-003**: Usar un horario o estimación cuantitativa solo tras comprobar cobertura, fecha y servicio; de otro modo mantener referencia o desconocido.
- **FR-004**: Separar falta de alertas, falta de cobertura y fuente no disponible; ninguna equivale a servicio puntual.
- **FR-005**: Permitir fallback explícito de referencia y selección explícita de fixture, sin presentar estos como información Renfe.
- **FR-006**: Verificar parseo y límites con respuestas reproducibles; documentar aparte una consulta real y sus limitaciones.

### Key Entities

Contexto de servicio: alertas, cobertura y vigencia. Evidencia cuantitativa: horario o estimación asociada a un viaje/fecha.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Una alerta no relacionada o sin minutos nunca modifica la llegada cuantitativa.
- **SC-002**: Cada resultado muestra su procedencia y su estado de disponibilidad.
- **SC-003**: Una consulta pública real queda registrada como evidencia sin reemplazar las pruebas deterministas.

## Assumptions

Depende de UC-002. Se limitará a un trayecto directo y una cobertura demostrable. Si hace falta seleccionar estaciones identificadas, se hará explícitamente antes de afirmar cobertura. No se exige construir un planificador GTFS completo.

**Dependencias**: UC-002.

La precisión del borrador es suficiente para revisar alcance; las decisiones propuestas
se refinan al activar el caso. Un escenario escrito no demuestra que ya funcione.
