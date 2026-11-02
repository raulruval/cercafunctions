# Feature Specification: UC-005 · Evaluar llegada frente al compromiso seleccionado

**Feature Branch**: `feat/005-arrival-assessment`

**Created**: 2026-11-01

**Status**: In progress — petición UC-005 recibida el 2026-11-02. Checklist humano sin cambios.

**Input**: Comparar la llegada estimada con el comienzo de una reunión.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Evaluar llegada frente al compromiso seleccionado (Priority: P1)

Como usuario quiero saber si mi llegada prevista encaja con el compromiso y sobre qué evidencia se basa.

**Why this priority**: Es el resultado observable que cierra UC-005; sus dependencias se detallan abajo.

**Independent Test**: Usar reunión 09:00 y llegadas 08:50, 09:00, 09:15 y desconocida para la misma fecha/zona.

**Acceptance Scenarios**:

1. **S1**: Dada llegada 08:50 y reunión 09:00, cuando evalúo, entonces veo un margen previsto de 10 minutos y la calidad de la estimación.
2. **S2**: Dada llegada 09:00 y reunión 09:00, cuando evalúo, entonces veo margen cero, sin retraso previsto.
3. **S3**: Dada llegada 09:15 y reunión 09:00, cuando evalúo, entonces veo retraso previsto de 15 minutos según la fuente usada.
4. **S4**: Dada llegada desconocida o compromiso ya no válido, cuando evalúo, entonces el resultado es indeterminado con explicación.
5. **S5**: Dados instantes iguales mostrados en zonas distintas, cuando comparo, entonces obtengo el mismo margen.

6. **S6**: Dada una consulta visible cuya duración guardada cambió, cuando evalúo, entonces se exige consultar de nuevo el trayecto, sin comparar silenciosamente otra llegada.
7. **S7**: Dada pérdida de permiso o compromiso borrado/modificado durante la evaluación, cuando revalido, entonces se retiran la selección/detalles invalidados y se conserva una explicación de resultado indeterminado.

### Edge Cases

Evento modificado o eliminado, cambio de día/zona y diferencia menor de un minuto.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Comparar instantes de llegada y comienzo, expresando el margen con precisión de minutos y mostrando los horarios completos.
- **FR-002**: Definir retraso previsto solo cuando llegada es posterior; igualdad es margen cero.
- **FR-003**: Mantener procedencia, vigencia e incertidumbre de la llegada en la evaluación.
- **FR-004**: No convertir un cálculo de referencia o fixture en garantía de puntualidad real.
- **FR-005**: Devolver indeterminado cuando falte llegada o compromiso válido; no disparar acciones de mensajería.
- **FR-006**: Verificar los cinco escenarios; mostrar margen no negativo en minutos completos disponibles (redondeo hacia abajo) y retraso positivo en minutos redondeados hacia arriba cuando haya fracciones.

### Key Entities

Evaluación: horarios comparados, margen/retraso previsto y calidad de evidencia; resultado indeterminado con causa.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Los casos de referencia dan +10, 0 y −15 minutos de margen respectivamente.
- **SC-002**: Toda evaluación conserva la etiqueta de referencia/fixture/real aplicable.
- **SC-003**: La ausencia de evidencia produce cero decisiones automáticas de envío.

## Assumptions

Depende de UC-002 y UC-004; UC-003 añade contexto. Margen adicional inicial 0 minutos y sin desplazamiento estación–reunión: la POC compara llegada a destino ferroviario con comienzo; esta simplificación se muestra al usuario.

## Alcance activado · 2026-11-02
Evaluación explícita, nunca automática ni persistida. Reconsultar misma salida,
zona/offset y escenario con QueryJourneyArrival; si cambia evidencia cuantitativa
o trayecto, pedir refresco. observedAt puede actualizarse en una referencia al
recalcular; no se inventa TTL. Salida pasada reutiliza rechazo de UC-002. Demos
mantienen reloj y datos fijos; no se trasladan al día del calendario ni caducan
por compararlas con el reloj actual. Fechas distintas se muestran completas.

Revalidar compromiso inmediatamente antes de comparar; resultado de dominio sin
título, nombre de calendario ni participantes. Sin selección/llegada conocida no
leer agenda innecesariamente. Invalidar evaluación al editar entrada, seleccionar
otro compromiso, consultar/revalidar calendario o abandonar primer plano. La
selección de UC-004 conserva su semántica de sesión; la evaluación requiere nueva
acción tras recreación/retorno. No se añaden módulos, red ni permisos nuevos.
Los avisos UC-003 permanecen cualitativos: no alteran minutos.

**Dependencias**: UC-002 y UC-004; UC-003 como contexto adicional.

La precisión del borrador es suficiente para revisar alcance; las decisiones propuestas
se refinan al activar el caso. Un escenario escrito no demuestra que ya funcione.
