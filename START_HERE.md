# Empieza aquí
**Estado al 2026-11-02**: UC-001, UC-002 y UC-003 implementados y verificados.
UC-004–008 siguen Draft. Snapshots: phase-0-base, sdd-inception-v1,
uc-001-v1, uc-002-v1 y uc-003-v1.

## Probar
Ejecutar app desde Android Studio. Guardar trayecto, consultar salida futura en
Referencia y bajar a Avisos Renfe. Buscar dos estaciones por nombre/ID y pulsar
cada sugerencia para confirmar asociación. Consultar avisos: fuente, consulta,
actualización y cobertura parcial se muestran sin modificar la llegada.
Las demos UC-002 permanecen separadas y no consultan Renfe.
No hay aún calendario ni AppFunctions publicadas.

1. [Guía UC-003](specs/003-renfe-context/quickstart.md).
2. [Pruebas/evidencia UC-003](specs/003-renfe-context/verification.md).
3. [Referencia y demos UC-002](specs/002-journey-estimate/verification.md).
4. [Persistencia UC-001](specs/001-habitual-journey/verification.md).
5. [Tablero de casos](specs/README.md).

## SDD y aprendizaje
Cada caso conserva spec (comportamiento), plan (diseño), tasks (trabajo) y
verification (evidencia). `8c2834c` guarda spec/plan UC-003 antes del producto.
[Android desde Flutter](docs/android-for-flutter.md): ViewModel, lifecycle,
DataStore, java.time, HTTP cancelable, DTO/mapeo y permisos normales/runtime.
Skills oficiales locales: speckit-specify/plan/tasks/implement/converge;
[método](docs/sdd-workflow.md). Los checklists humanos no se marcan por pasar tests.

## Siguiente incremento
[UC-004: elegir compromiso de calendario](specs/004-calendar-selection/spec.md)
permanece sin iniciar. Revisar permisos, recurrencias y selección antes de construir.
