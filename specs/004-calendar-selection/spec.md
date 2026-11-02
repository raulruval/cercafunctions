# Feature Specification: UC-004 · Elegir un compromiso del calendario del dispositivo

**Feature Branch**: `feat/004-calendar-selection`

**Created**: 2026-11-01

**Status**: In progress — petición «Continua con el UC-004», 2026-11-02. No equivale a lectura/aprobación del checklist humano.

**Input**: Consultar eventos del calendario Android con permiso y seleccionar la reunión relevante.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Elegir un compromiso del calendario del dispositivo (Priority: P1)

Como usuario quiero elegir un compromiso de la fecha consultada y controlar el acceso al calendario.

**Why this priority**: Es el resultado observable que cierra UC-004; sus dependencias se detallan abajo.

**Independent Test**: Usar un calendario de prueba con un evento, dos reuniones y una recurrencia; probar concesión, rechazo y revocación del acceso.

**Acceptance Scenarios**:

1. **S1**: Dado que no se ha concedido lectura, cuando decido consultar el calendario, entonces se solicita permiso desde una pantalla visible.
2. **S2**: Dado acceso concedido y varios compromisos en la fecha seleccionada, cuando se muestran, entonces elijo uno explícitamente y se identifica la ocurrencia concreta.
3. **S3**: Dado un evento recurrente, cuando consulto esa fecha, entonces veo la ocurrencia correspondiente, no la fecha original de toda la serie.
4. **S4**: Dado rechazo, revocación o calendario vacío, cuando consulto, entonces se informa de ese estado sin inventar una reunión.

5. **S5**: Dada una selección borrada, movida, cancelada o con fin cambiado, cuando la revalido, entonces se retira y se exige una nueva consulta/selección. La revocación retira también lista y selección.
6. **S6**: Dadas dos reuniones con el mismo título en calendarios visibles, cuando elijo una, entonces la selección conserva su identidad de ocurrencia; cambiar fecha o zona invalida la consulta anterior.

### Edge Cases

Día de 23/25 horas, falta de proveedor, múltiples calendarios, evento borrado tras seleccionarlo y ausencia de eventos sincronizados.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Solicitar lectura de calendario solo tras una acción contextual del usuario y manejar rechazo/revocación.
- **FR-002**: Acotar la consulta al día local seleccionado, con intervalo desde su inicio hasta el inicio del día siguiente.
- **FR-003**: Mostrar compromisos con hora que comienzan en ese intervalo, excluyendo eventos cancelados y de día completo.
- **FR-004**: Exigir selección explícita de una ocurrencia, incluso cuando solo haya una, y mostrar su fecha/hora para revisar.
- **FR-005**: Conservar la selección solo durante la sesión y revalidar la ocurrencia antes de evaluar; no copiar la agenda a almacenamiento propio.
- **FR-006**: No compartir todavía datos con agentes ni modificar eventos; probar recurrencias y cambios de zona.

### Key Entities

Compromiso seleccionado: identificador local de ocurrencia, comienzo y zona; detalles legibles solo en UI local para elegir.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: La selección apunta a una sola ocurrencia aunque existan títulos iguales.
- **SC-002**: Rechazo y revocación impiden leer datos y no hacen fallar la aplicación.
- **SC-003**: La consulta de una fecha no muestra eventos ajenos al intervalo definido.

## Assumptions

Puede desarrollarse independientemente del cálculo ferroviario. El usuario de demo preparará eventos ficticios en el proveedor local; no se presupone una API pública de capacidades de Google Calendar.

## Alcance activado · 2026-11-02
Panel independiente del trayecto, con fecha local y zona explícitas (por defecto
mañana en la zona del dispositivo). Consulta manual de calendarios visibles del
proveedor Android; no se supone que haya cuenta Google ni sincronización.
Selección en memoria del ViewModel durante la sesión, conservada al recrear la
Activity; perdida al morir proceso. Sin SavedState/DataStore para datos de agenda.

Instances expande recurrencias. Identidad EVENT_ID+BEGIN; título no identifica.
Proyección: identidad, comienzo, fin, título, nombre legible del calendario,
ALL_DAY y STATUS para filtrar. No leer descripción, asistentes, cuenta ni ubicación.
Horas se muestran en zona elegida, preservando instantes/fecha/offset; no se
precisa la zona original de creación del evento.

Solo READ_CALENDAR en producción. Petición contextual con Activity Result API;
rechazo y enlace explícito a Ajustes. En ON_RESUME comprobar acceso y retirar
datos si falta; el repositorio comprueba acceso antes/después de consultar y
captura SecurityException. Sin consultas automáticas al volver de Ajustes.
Revalidar selección es una acción explícita que lee de nuevo el día y exige
misma identidad y fin. No evalúa puntualidad (UC-005) ni autoriza divulgación (UC-007).
No asegura congelar el proveedor: cualquier evaluación posterior debe revalidar
de nuevo inmediatamente antes de usar los datos. Sin polling/ContentObserver.

**Dependencias**: Base Android; independiente de UC-001–003.

La precisión del borrador es suficiente para revisar alcance; las decisiones propuestas
se refinan al activar el caso. Un escenario escrito no demuestra que ya funcione.
