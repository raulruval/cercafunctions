# Feature Specification: UC-003 · Consultar contexto ferroviario real con vigencia explícita

**Feature Branch**: `feat/003-renfe-context`

**Created**: 2026-11-01

**Status**: Verified — alcance acotado implementado y verificado el 2026-11-02; ver [verification.md](verification.md). Checklist humano se conserva sin cambios.

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

6. **S6**: Dados identificadores seleccionados expresamente del catálogo Renfe, cuando consulto, entonces solo se afirma cobertura de las estaciones extremas; línea, tren, dirección y estaciones intermedias quedan sin resolver.
7. **S7**: Dada una demo UC-002 o una consulta modificada, cuando cambia el resultado de referencia, entonces no se consulta Renfe para la demo y se retira/cancela el contexto anterior.

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

Depende de UC-002. El alcance activado solo demuestra asociación a dos estaciones extremas seleccionadas explícitamente; no demuestra servicio directo. No se exige construir un planificador GTFS completo. Los detalles están en el alcance revisado siguiente.

## Alcance revisado al activar · 2026-11-02
Se implementa consulta HTTPS manual a alerts.json y selección explícita de dos
estaciones mediante catálogo GTFS snapshot (1147 IDs/nombres, 2026-11-02).
No se deducen IDs de nombres libres ni se demuestra servicio directo. Cobertura
parcial de extremos siempre visible. Catálogo no garantiza vigencia del servicio.

Se separa frescura del feed de periodos publicados y afectación al viaje.
Política POC: timestamp de feed con antigüedad máxima 10 minutos; más de 60 segundos
en futuro se rechaza; timestamp ausente no permite afirmar actualidad. Mostrar
timestamp original y consulta incluso en caducado. active_period/communication_period
son publicación, no prueba de impacto; sin impact_period, afectación sin confirmar.
No interpretar minutos en texto libre. Feed inválido falla completo, nunca vacío.

No se consumen horarios ni trip_updates para calcular llegada: S5 es condicional
y no se activa en este incremento. FR-003 se satisface manteniendo referencia
independiente; horas GTFS >24:00 quedan fuera del parser de alertas. Se investigan
sus fuentes y se documenta ese límite. No se construye un planificador GTFS.
Referencia y demos UC-002 siguen siendo elecciones explícitas; no hay fallback
silencioso a fixtures ni red automática. Sin datos válidos, se conserva su etiqueta.

**Dependencias**: UC-002.

La evidencia ejecutada y los límites de cada escenario se registran en verification.md.
La revisión documental del checklist no equivale a aprobación humana.
