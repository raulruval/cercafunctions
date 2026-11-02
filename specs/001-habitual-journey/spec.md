# Feature Specification: UC-001 · Configurar y conservar mi trayecto habitual

**Feature Branch**: `feat/001-habitual-journey`

**Created**: 2026-11-01

**Status**: Verified — implementado y comprobado el 2026-11-02; ver verification.md.

**Input**: Configurar origen, destino y duración estimada de un trayecto habitual.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Configurar y conservar mi trayecto habitual (Priority: P1)

Como usuario quiero guardar un único trayecto para reutilizarlo al consultar mis viajes.

**Why this priority**: Es el resultado observable que cierra UC-001; sus dependencias se detallan abajo.

**Independent Test**: En una instalación sin configuración, guardar Origen de prueba → Destino de prueba, 35 minutos, cerrar y volver a abrir; después editar a 40 minutos.

**Acceptance Scenarios**:

1. **S1**: Dado que no existe configuración, cuando guardo dos nombres distintos y 35 minutos, entonces veo los tres valores y confirmación de guardado.
2. **S2**: Dado un trayecto guardado, cuando se cierra el proceso y vuelvo a abrir la app, entonces recupero exactamente los últimos valores confirmados.
3. **S3**: Dados nombres vacíos o iguales tras quitar espacios exteriores e ignorar mayúsculas, cuando intento guardar, entonces se señala el campo y no se sustituye la configuración anterior.
4. **S4**: Dada una duración vacía, no entera, menor que 1 o mayor que 1440, cuando intento guardar, entonces se explica el error y no se guarda.
5. **S5**: Dado un fallo de escritura antes de confirmar la operación, cuando guardo, entonces veo un error, conservo mi edición para reintentar y no recibo confirmación falsa.
6. **S6**: Dado un fallo de lectura o una configuración parcial, cuando abro la app, entonces veo configuración no disponible; no se presenta como primera instalación ni se sobrescribe automáticamente.
7. **S7**: Dado un trayecto existente, cuando entro a editarlo, entonces el formulario muestra sus valores; al guardar 40 minutos queda un único trayecto actualizado.
8. **S8**: Dado un formulario con cambios sin guardar, cuando abandono la edición, entonces el trayecto guardado sigue intacto; al volver se carga el valor guardado.

### Edge Cases

Espacios, diferencias solo de mayúsculas, entero fuera de rango, doble pulsación, escritura fallida y lectura corrupta. Ante cierre durante guardado puede recuperarse el estado anterior o el nuevo íntegro, nunca campos mezclados; sin confirmación no se promete cuál.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Permitir un único trayecto con origen, destino y duración en minutos enteros; valores válidos conforme a escenarios 3 y 4.
- **FR-002**: Eliminar espacios exteriores al guardar nombres y conservar sus acentos y presentación; la igualdad para validar ignora mayúsculas.
- **FR-003**: Confirmar guardado solo al finalizar la escritura; conservar el conjunto completo de campos y recuperarlo después de cerrar el proceso.
- **FR-004**: Distinguir sin configuración, lectura en curso, configuración disponible y lectura no disponible.
- **FR-005**: Ofrecer edición del trayecto existente y reemplazarlo completamente al confirmar; salir sin guardar conserva la versión anterior.
- **FR-006**: Impedir envíos duplicados mientras se guarda y mantener los valores introducidos si falla la operación.
- **FR-007**: Funcionar sin conexión ni acceso al calendario; la duración se presenta como referencia aportada por el usuario, no como predicción ferroviaria.
- **FR-008**: Verificar validación, persistencia tras recreación, fallos y cancelación, además del recorrido de pantalla.

### Key Entities

Trayecto habitual: nombre de origen, nombre de destino y duración de referencia. Borrador: edición todavía no confirmada.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Una configuración válida se recupera íntegra al reiniciar y se actualiza sin crear un segundo trayecto.
- **SC-002**: Los cinco grupos de entradas inválidas y los fallos de lectura/escritura del escenario se distinguen sin falsos mensajes de éxito.
- **SC-003**: Los escenarios 1–8 se pueden comprobar sin red, calendario ni agente externo.

## Assumptions

Alcance inicial propuesto: un trayecto, nombres de texto y duración de 1 a 1440 minutos. No se validan estaciones reales ni se configura la hora de salida aquí; eso pertenece a UC-002/003. No hay listado, borrado, historial, sincronización o backup. Los borradores no confirmados pueden perderse al morir el proceso.

**Dependencias**: Base phase-0-base.

Autorización: «Haz push al repo de lo que llevamos hasta ahora y comienza con el primer caso de uso» (2026-11-02).
Los criterios permanecen iguales; su cumplimiento se registra en verification.md al cierre.
