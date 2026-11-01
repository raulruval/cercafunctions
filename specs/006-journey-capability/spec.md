# Feature Specification: UC-006 · Publicar la consulta de trayecto como capacidad

**Feature Branch**: `chore/sdd-foundation` (borrador; rama de construcción por crear)

**Created**: 2026-11-01

**Status**: Draft — pendiente de revisión; ninguna implementación autorizada por este documento.

**Input**: Permitir a un agente externo consultar el trayecto y su estimación con los mismos casos de uso que la UI.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Publicar la consulta de trayecto como capacidad (Priority: P1)

Como usuario quiero autorizar una consulta de mi trayecto desde otro asistente sin abrir la pantalla.

**Why this priority**: Es el resultado observable que cierra UC-006; sus dependencias se detallan abajo.

**Independent Test**: Descubrir e invocar la capacidad con una entrada fija y comparar su resultado con la consulta equivalente en UI.

**Acceptance Scenarios**:

1. **S1**: Dada la capacidad instalada y habilitada, cuando un consumidor autorizado la descubre, entonces obtiene descripción e inputs específicos de la consulta.
2. **S2**: Dados los mismos datos, reloj y salida que en UI, cuando se invoca sin Activity abierta, entonces devuelve la misma llegada, procedencia y estado.
3. **S3**: Dada falta de configuración o servicio no disponible, cuando se invoca, entonces obtiene un resultado o error acotado comprensible y no una llegada inventada.
4. **S4**: Dado un consumidor no autorizado o una entrada inválida, cuando intenta consultar, entonces no obtiene datos de trayecto.

### Edge Cases

Proceso recreado, servicio del sistema ausente, función deshabilitada, cancelación y fuentes caídas.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Exponer solo la consulta de trayecto ya disponible en UI, con salida temporal explícita y parámetros mínimos.
- **FR-002**: Reutilizar el cálculo de dominio y mantener etiquetas de fixture, referencia, desconocido y datos reales.
- **FR-003**: Funcionar con la interfaz cerrada y configuración persistida, sin provocar diálogos de permisos.
- **FR-004**: Publicar contrato descubrible, documentado y comprobable; no devolver calendario ni contactos.
- **FR-005**: Restringir ejecución a consumidores autorizados y mapear errores sin detalles privados.
- **FR-006**: Verificar descubrimiento, contrato y equivalencia con UI mediante entradas deterministas.

### Key Entities

Entrada de capacidad: salida/fecha/zona. Respuesta pública: estimación y evidencia mínima del trayecto.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un consumidor de pruebas descubre e invoca la capacidad y recibe resultados equivalentes al caso de UI.
- **SC-002**: Las pruebas negativas no filtran datos de calendario o detalles de implementación.
- **SC-003**: La consulta funciona sin Activity abierta en una imagen compatible.

## Assumptions

Depende de UC-002/003. Se seguirá la skill oficial AppFunctions vigente; los privilegios del consumidor se verifican separadamente del registro del proveedor.

**Dependencias**: UC-002 y UC-003.

La precisión del borrador es suficiente para revisar alcance; las decisiones propuestas
se refinan al activar el caso. Un escenario escrito no demuestra que ya funcione.
