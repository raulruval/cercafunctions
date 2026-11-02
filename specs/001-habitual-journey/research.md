# Decisiones de UC-001

Estado: decisiones aplicadas en UC-001; evidencia de implementación en verification.md.

## R1 · Persistencia mínima

**Decision**: Preferences DataStore 1.3.0 para tres campos en una transacción.
**Rationale**: volumen pequeño, operaciones asíncronas y consistentes. Versión
estable consultada el 2026-11-01 en [AndroidX](https://developer.android.com/jetpack/androidx/releases/datastore).
**Alternatives considered**: Room para relaciones/consultas complejas; Proto para
esquema tipado; almacenamiento solo en memoria no satisface reinicio.

## R2 · Frontera de plataforma

**Decision**: DataStoreJourneyRepository en core-platform implementa JourneyRepository.
**Rationale**: conservar dominio/data JVM y dependencias existentes con menos capas.
**Alternatives considered**: añadir puerto de datasource a dominio y un repositorio
delegador en data; coste sin comportamiento adicional en este incremento.

## R3 · Errores y cancelación

**Decision**: diferenciar ausencia, datos inválidos y operación no disponible;
confirmar guardado solo cuando termine la actualización duradera.
**Rationale**: una lectura fallida no prueba primera instalación; una escritura
incompleta no debe convertirse en éxito. Propagar CancellationException.
**Alternatives considered**: capturar toda excepción y devolver null: pierde estados.
Referencia: [contrato DataStore](https://developer.android.com/reference/kotlin/androidx/datastore/core/DataStore).

## R4 · Pruebas proporcionadas

**Decision**: archivo temporal con cierre/recreación de DataStore, fallos inyectados,
validación de dominio e interacción de formulario; verificación real de relanzamiento.
**Rationale**: protegen los límites del incremento sin probar trivialidades de UI.
**Alternatives considered**: confiar solo en mocks no prueba persistencia duradera.

La instancia de DataStore será única por archivo; no mantener dos instancias vivas
sobre el mismo archivo durante el test de recreación. No necesitamos multiproceso:
UI y proveedor Copilot comparten proceso; el agente futuro invoca mediante IPC.
[Guía oficial](https://developer.android.com/topic/libraries/architecture/datastore).
