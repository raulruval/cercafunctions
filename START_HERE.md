# Empieza aquí

**Estado al 2026-11-02**: UC-001 está implementado y verificado. Los otros siete
casos siguen en Draft. Snapshots: `phase-0-base`, `sdd-inception-v1` y `uc-001-v1`.

## Probar el primer caso

Abre el proyecto en Android Studio y ejecuta `app`. Puedes configurar y editar
un trayecto con origen, destino y duración de referencia entre 1 y 1440 minutos.
Guardar conserva los datos al reiniciar; Cancelar o Atrás descarta la edición.
No se consulta todavía Renfe ni el calendario.

1. [Guía de ejecución de UC-001](specs/001-habitual-journey/quickstart.md).
2. [Pruebas y evidencia](specs/001-habitual-journey/verification.md).
3. [Mapa de casos y seguimiento](specs/README.md).

## Aprender de la implementación

[spec.md](specs/001-habitual-journey/spec.md) define comportamiento;
[plan.md](specs/001-habitual-journey/plan.md), el diseño;
[tasks.md](specs/001-habitual-journey/tasks.md), el trabajo y su trazabilidad.
La [guía Android desde Flutter](docs/android-for-flutter.md) explica ViewModel,
StateFlow, Hilt, DataStore y la diferencia entre recrear Activity y morir el proceso.

## Siguiente incremento

El siguiente caso propuesto es [UC-002: llegada de referencia y escenarios](specs/002-journey-estimate/spec.md).
Primero se revisan sus fechas, zona, hora de salida y procedencia; después su plan
y tareas. UC-002 no se ha iniciado ni se considera autorizado por cerrar UC-001.

Con Codex abierto en este repositorio se pueden usar las skills `$speckit-specify`,
`$speckit-plan`, `$speckit-tasks`, `$speckit-implement` y `$speckit-converge`.
También puedes pedir cada paso en lenguaje natural. Ver [método y fuentes](docs/sdd-workflow.md).
