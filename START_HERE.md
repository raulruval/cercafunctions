# Empieza aquí
Estado al2026-11-03: UC-001–007 implementados y verificados; UC-008 Draft.
Snapshots phase-0-base,sdd-inception-v1 y uc-001-v1…uc-007-v1.

## Probar una evaluación autorizada
Instalar APK en sistema AppFunctions compatible (probado37.2). Consultar Calendario,
conceder READ_CALENDAR y elegir explícitamente una ocurrencia futura. En Compartir
evaluación del compromiso, pulsar Autorizar esta evaluación temporal y copiar
referencia solo si deseas entregarla al asistente. Caduca a30min o al comienzo.
Una emisión nueva sustituye la anterior; puedes revocar. Al salir de primer plano
se oculta el token: para obtener otro hay que reautorizar. El permiso local de
calendario y la autorización de trayecto UC-006 son decisiones diferentes.

```powershell
python scripts/appfunctions-smoke.py discover
python scripts/appfunctions-smoke.py assessment --departure 2026-11-04T08:40 --zone UTC
```
La referencia se solicita por entrada oculta/stdin; no va en argumentos del cliente
ni del adb anfitrión o historial de la terminal.
Usar fecha de salida futura para referencia. Calendario ficticio opcional: scripts/
calendar-demo-fixture.py create; fecha/UTC impresas, reunión09:00 y trayecto35
dan15min tarde para08:40. Limpiar fixture con cleanup usando el mismo archivo de estado.
El consumidor SDK es privilegiado, no una app ordinaria. La autorización de pruebas
se dejó revocada y READ_CALENDAR desactivado. No se han enviado mensajes.

1. [Guía UC-007](specs/007-assessment-capability/quickstart.md).
2. [Evidencia y límites](specs/007-assessment-capability/verification.md).
3. [Consulta de trayecto UC-006](specs/006-journey-capability/quickstart.md).
4. [Evaluación local UC-005](specs/005-arrival-assessment/quickstart.md).
5. [Tablero](specs/README.md).

## SDD y aprendizaje
5fd05e2 guarda spec/plan UC-007 antes del código. Cada caso conserva spec,plan,
tasks,verification; checklist humano8/10 intacto, sin atribuir lectura al usuario.
[Android desde Flutter](docs/android-for-flutter.md) explica reloj monotónico,
referencias de alcance mínimo, lifecycle y revocación concurrente.
UC-008 permanece sin iniciar: ChatApp/TestingAgent oficiales, propuesta a Ana y
confirmación explícita antes de envío.
