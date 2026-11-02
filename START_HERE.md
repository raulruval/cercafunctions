# Empieza aquí
**Estado al 2026-11-02**: UC-001–004 implementados y verificados.
UC-005–008 siguen Draft. Snapshots: phase-0-base, sdd-inception-v1,
uc-001-v1, uc-002-v1, uc-003-v1 y uc-004-v1.

## Probar calendario
Ejecutar app en Android Studio. El panel Calendario no necesita trayecto.
Elegir fecha y zona, Consultar calendario y conceder READ_CALENDAR. Elegir una
ocurrencia explícitamente y revalidarla. Solo eventos con hora que comienzan ese
día en calendarios visibles. Rechazo/revocación retiran datos; no se comparten
con agentes ni se persisten. La selección se conserva al recrear Activity,
pero no tras morir proceso. No se ha implementado evaluación de puntualidad aún.

Sin cuenta, usar opcionalmente `python scripts/calendar-demo-fixture.py create`:
introducir fecha impresa y zona UTC. Al terminar, ejecutar el mismo script con
`cleanup`. Es solo una utilidad de SDK; el APK no escribe eventos.

1. [Ejecución UC-004](specs/004-calendar-selection/quickstart.md).
2. [Pruebas y límites UC-004](specs/004-calendar-selection/verification.md).
3. [Avisos Renfe UC-003](specs/003-renfe-context/verification.md).
4. [Referencia y demos UC-002](specs/002-journey-estimate/verification.md).
5. [Persistencia UC-001](specs/001-habitual-journey/verification.md).
6. [Tablero de casos](specs/README.md).

## SDD y aprendizaje
Cada caso conserva spec, plan, tasks y verification. `421cd04` guarda spec/plan
UC-004 antes del producto. [Android desde Flutter](docs/android-for-flutter.md)
añade permisos runtime, ContentResolver, Instances, cancelación y límites del proveedor.
Skills: speckit-specify/plan/tasks/implement/converge; [método](docs/sdd-workflow.md).
Los checklists humanos no se marcan por pasar tests.

## Siguiente incremento
[UC-005: evaluar llegada frente al compromiso](specs/005-arrival-assessment/spec.md)
permanece sin iniciar. Revisar margen y reglas de evaluación antes de construir.
