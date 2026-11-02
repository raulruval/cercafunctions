# Empieza aquí

**Estado al 2026-11-02**: UC-001 y UC-002 implementados y verificados.
Los otros seis casos siguen en Draft. Snapshots: `phase-0-base`,
`sdd-inception-v1`, `uc-001-v1` y `uc-002-v1`.

## Probar lo construido
Abrir proyecto en Android Studio, sincronizar y ejecutar `app`.
Configurar un trayecto con duración de referencia; bajar al panel de llegada,
confirmar salida/fecha/zona y Consultar salida. Referencia usa duración guardada.
Demos normal, +15 y desconocido usan ruta, fecha y reloj fijos identificados.
No se consulta todavía Renfe ni calendario.

1. [Guía de ejecución UC-002](specs/002-journey-estimate/quickstart.md).
2. [Pruebas y evidencia UC-002](specs/002-journey-estimate/verification.md).
3. [Persistencia UC-001](specs/001-habitual-journey/verification.md).
4. [Mapa de casos y seguimiento](specs/README.md).

## Aprender de la implementación
Cada caso tiene `spec.md` (comportamiento), `plan.md` (diseño), `tasks.md`
(trabajo trazable) y `verification.md` (evidencia). El commit `8829fc6` conserva
la especificación y plan UC-002 antes de su commit de entrega.
[Android desde Flutter](docs/android-for-flutter.md) explica ViewModel,
StateFlow, Hilt, DataStore, java.time/Clock y lifecycle.

## Siguiente incremento
Propuesto [UC-003: contexto ferroviario real](specs/003-renfe-context/spec.md).
Revisar primero fuentes, cobertura temporal y límites; permanece sin iniciar.

Skills disponibles: `$speckit-specify`, `$speckit-plan`, `$speckit-tasks`,
`$speckit-implement` y `$speckit-converge`. Puedes pedir sus pasos en lenguaje
natural; [método y fuentes](docs/sdd-workflow.md). Los checklists documentales
no se convierten en aprobación humana por ejecutar pruebas.
