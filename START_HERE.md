# Empieza aquí
**Estado al 2026-11-02**: UC-001–005 implementados y verificados.
UC-006–008 siguen Draft. Snapshots: phase-0-base, sdd-inception-v1,
uc-001-v1, uc-002-v1, uc-003-v1, uc-004-v1 y uc-005-v1.

## Probar la evaluación
Elegir explícitamente una ocurrencia del calendario con permiso READ_CALENDAR.
Configurar trayecto, consultar salida y pulsar Evaluar llegada. Se reconsulta la
misma salida y se revalida el compromiso antes de comparar instantes. La pantalla
muestra margen/retraso, fuente y horarios completos. Cambiar entradas o volver a
primer plano retira la evaluación; calendario inválido retira también selección.
No se publican capacidades ni se envían mensajes todavía.

Para una prueba sin cuenta: `python scripts/calendar-demo-fixture.py create`.
Usar fecha impresa y UTC, elegir reunión09:00, configurar duración35 y salidas
08:15/08:25/08:40 en Referencia: margen10/0/retraso15. Terminar con `cleanup`.
El APK solo lee agenda; la utilidad SDK crea y elimina exclusivamente sus fixtures.
La referencia no garantiza puntualidad real y no incluye estación–reunión.
Los códigos de estaciones para los avisos son Atocha18000 y Chamartín17000:
escribir código y pulsar sugerencia. No cambian los minutos de la evaluación.

1. [Ejecución UC-005](specs/005-arrival-assessment/quickstart.md).
2. [Pruebas y límites UC-005](specs/005-arrival-assessment/verification.md).
3. [Calendario UC-004](specs/004-calendar-selection/verification.md).
4. [Avisos Renfe UC-003](specs/003-renfe-context/verification.md).
5. [Referencia y demos UC-002](specs/002-journey-estimate/verification.md).
6. [Persistencia UC-001](specs/001-habitual-journey/verification.md).
7. [Tablero de casos](specs/README.md).

## SDD y aprendizaje
Cada caso conserva spec, plan, tasks y verification. `0558900` guarda spec/plan
UC-005 antes del producto. [Android desde Flutter](docs/android-for-flutter.md)
explica composición de casos de uso, StateFlow, cancelación y precisión temporal.
Skills: speckit-specify/plan/tasks/implement/converge; [método](docs/sdd-workflow.md).
Los checklists humanos no se marcan por pasar tests.

## Siguiente incremento
[UC-006: publicar consulta de trayecto](specs/006-journey-capability/spec.md)
permanece sin iniciar. Revisar skill oficial Google, API vigente, servicio y
contratos de descubrimiento antes de construir esa frontera.
