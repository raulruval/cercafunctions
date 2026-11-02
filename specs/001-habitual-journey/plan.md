# Implementation Plan: UC-001 · Trayecto habitual persistido

**Branch**: `feat/001-habitual-journey`.
**Date**: 2026-11-01 | **Spec**: [spec.md](spec.md)
**Status**: Implementado y verificado el 2026-11-02; ver verification.md.

## Summary

Ampliar la pantalla actual con configuración/edición y persistir un trayecto
completo. Usar el contrato de dominio desde ViewModel y un repositorio
Preferences DataStore en `core-platform`, ensamblado por Hilt en `app`.
El caso queda cerrado al recuperar la configuración tras recrear el proceso.

## Technical Context

- Kotlin/JVM 17; toolchain Android actual del catálogo, API mínima 26.
- Compose, Material 3, ViewModel, StateFlow, Hilt y Coroutines ya presentes.
- Dependencia nueva propuesta: `androidx.datastore:datastore-preferences:1.3.0`.
- Storage: un archivo de preferencias, una instancia de DataStore por proceso/archivo.
- Testing: JUnit/coroutines-test, DataStore con archivo temporal, interacción Compose.
- Alcance: una configuración local; sin red, permisos nuevos ni servicios exportados.
- Operaciones de almacenamiento asíncronas; sin lectura/escritura bloqueante en UI.
- Recuperación de formulario guardado tras recreación; borrador solo en memoria.
- Pruebas temporales no aplican aún; no introducir Clock en una operación sin fechas.

## Constitution Check

Antes y después del diseño: seis módulos conservados, dominio puro, sin capacidades
nuevas publicadas, sin datos privados, sin predicciones. Dependencia Android nueva
acotada a plataforma. Contratos de dominio compartibles con el futuro servicio.
Diseño conforme; construcción solicitada el 2026-11-02 y comprobaciones completadas.

## Project Structure

`core-domain/.../journey/`: ampliar `JourneyRepository`, adaptar `GetHabitualJourney`
y añadir `SaveHabitualJourney` con errores/resultados tipados específicos del trayecto.
La validación canónica es de dominio; la UI solo traduce formato de entrada.

`core-platform/.../journey/DataStoreJourneyRepository.kt`: implementar el contrato,
leyendo/escribiendo tres campos completos en una transacción y distinguiendo
ausencia, corrupción y error. Recibir DataStore por constructor para probarlo.

`app/.../di/JourneyModule.kt`: crear DataStore singleton usando Application context
y enlazar el repositorio real. Mantener Activity y futuro servicio en proceso predeterminado.

`core-data/.../journey/InMemoryJourneyRepository.kt`: adaptar al contrato cambiado;
conservarlo como soporte de pruebas/fixtures y quitarlo del ensamblado de producción.
La persistencia Android encaja directamente en plataforma; no añadir un LocalDataSource
artificial ni hacer que plataforma dependa de data. Documentar esta excepción práctica
a la ubicación habitual de repositorios; data se ocupará de Renfe en UC-003.

`core-presentation/.../home/`: estado inicial/cargando/listo/error y estado de formulario,
acciones editar/guardar/cancelar/reintentar. Textos en recursos, componentes con estado
recibido para Preview y pruebas. Validación de edición y guardado se mantiene al rotar.

## Decisions and Contracts

Ver [research.md](research.md), [data-model.md](data-model.md) y
[contrato de UI/dominio](contracts/journey.md). Cambiar el retorno nullable actual
a resultado de lectura explícito rompe una API interna aún no publicada; actualizar
consumidores y tests en el mismo incremento. No crear una jerarquía global Either/Failure.

Propagar cancelación. Una cancelación no garantiza rollback de una escritura ya
confirmada; solo se exige estado completo y ausencia de confirmación falsa.
No capturar errores de almacenamiento devolviendo configuración vacía por defecto.

La dependencia DataStore se declara `api` en plataforma porque su constructor
forma parte del contrato de composición consumido por app. Se añade una prueba
de integración en app para verificar Activity, Hilt y repositorio reales.

## Validation and Delivery

Ejecutar tests de dominio, persistencia e interacción definidos en tasks.md,
y después compilación/lint. Comprobar cierre y relanzamiento en emulador.
[quickstart.md](quickstart.md) describe la aceptación futura. Añadir `verification.md`
con resultados reales al terminar; actualizar el tablero y crear snapshot verificable.

## Complexity Tracking

Una biblioteca oficial adicional y un adaptador de persistencia. Sin nuevos módulos,
servidor, navegación, repositorios genéricos, capa de datasource adicional ni migración
de datos de usuario: la base anterior no tenía persistencia.
