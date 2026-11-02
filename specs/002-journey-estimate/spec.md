# Feature Specification: UC-002 · Consultar llegada de referencia y escenarios reproducibles

**Feature Branch**: `feat/002-journey-estimate`

**Created**: 2026-11-01

**Status**: In progress — petición de continuar UC-002 recibida el 2026-11-02. No implica lectura documental ni aprobación humana de checklists.

**Input**: Consultar mi trayecto para mañana y disponer de escenarios reproducibles de puntualidad y retraso.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consultar llegada de referencia y escenarios reproducibles (Priority: P1)

Como usuario quiero una llegada para una salida concreta, con su origen de datos claramente visible.

**Why this priority**: Es el resultado observable que cierra UC-002; sus dependencias se detallan abajo.

**Independent Test**: Fijar fecha y zona, usar duración 35 y salida 08:00; comprobar llegada 08:35 y fixture de retraso +15 → 08:50.

**Acceptance Scenarios**:

1. **S1**: Dado un trayecto de 35 minutos y una salida a las 08:00 de una fecha explícita, cuando consulto referencia, entonces veo 08:35 como llegada basada en mi duración.
2. **S2**: Dado el escenario fixture +15 minutos para ese viaje, cuando lo selecciono, entonces veo 08:50 con una etiqueta inequívoca de demostración.
3. **S3**: Dado que solicito mañana, cuando preparo la consulta, entonces veo la fecha concreta y zona horaria usadas y debo aportar o confirmar la hora de salida.
4. **S4**: Dada una salida ambigua o inexistente por cambio de hora, cuando consulto, entonces se requiere una elección temporal válida y no se elige una interpretación en silencio.
5. **S5**: Dado que no hay trayecto configurado, cuando consulto, entonces se indica que falta configuración y no se inventa una llegada.

6. **S6**: Dados los fixtures normal, +15 y desconocido, cuando consulto cada uno, entonces obtengo 08:35, 08:50 y llegada desconocida respectivamente, siempre con ruta, salida y reloj de demostración fijos y visibles.

### Edge Cases

Cambio de día, horario de verano/invierno, consulta cerca de medianoche y trayecto ausente.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Calcular referencia desde una salida completa y la duración guardada, sin atribuirle certeza de operación real.
- **FR-002**: Resolver mañana como siguiente fecha local; mostrar fecha, hora y zona; rechazar salidas pasadas.
- **FR-003**: Ofrecer fixtures separados normal, retraso de 15 minutos y sin estimación, con reloj y datos fijos.
- **FR-004**: Incluir procedencia y fecha de consulta en cada resultado y distinguir referencia, fixture y desconocido.
- **FR-005**: Preservar la fecha al cruzar medianoche y resolver explícitamente horas ambiguas/inexistentes.
- **FR-006**: Verificar cálculo temporal, desconocido y escenarios con entradas fijas.

### Key Entities

Consulta temporal: fecha/hora/zona de salida. Estimación: llegada opcional, procedencia, instante de consulta y horizonte aplicable.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 35 minutos y fixture +15 producen las dos llegadas indicadas para el mismo instante de salida.
- **SC-002**: Repetir entradas y reloj fijo da exactamente el mismo resultado.
- **SC-003**: Ningún fixture aparece como dato real y desconocido no se transforma en retraso cero.

## Assumptions

Fixtures: ruta «Origen demo → Destino demo», duración 35 min, salida 2026-11-02 08:00 Europe/Madrid, reloj 2026-11-01T08:00:00Z. No representan el trayecto real guardado; requieren que exista configuración para acceder al panel. Referencia y demos son modos separados. La referencia no conoce el retraso real; su horizonte es únicamente la salida solicitada, sin validación ferroviaria.

Depende de UC-001. La hora de salida se confirma por consulta; aún no se almacena una rutina semanal. No es un planificador de horarios ferroviarios.

**Dependencias**: UC-001.

La precisión del borrador es suficiente para revisar alcance; las decisiones propuestas
se refinan al activar el caso. Un escenario escrito no demuestra que ya funcione.
