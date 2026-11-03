# Empieza aquí
**Estado al2026-11-03**: UC-001–006 implementados y verificados; UC-007/008 Draft.
Snapshots phase-0-base, sdd-inception-v1 y uc-001-v1…uc-006-v1.

## Probar la capacidad de trayecto
Ejecutar app en imagen compatible API36+ (laboratorio probado API37.2). Configurar
trayecto y pulsar Autorizar consulta del trayecto en Compartir trayecto con asistentes.
La app explica que comparte origen/destino/estimación con consumidores autorizados
por Android, incluso cerrada; no calendario/contactos ni mensajes. Revocar en el
mismo panel. Desactivado por defecto; no hace falta permiso de calendario.

```powershell
python scripts/appfunctions-smoke.py discover
python scripts/appfunctions-smoke.py state
python scripts/appfunctions-smoke.py query --departure 2026-11-04T08:00 --zone UTC
python scripts/appfunctions-smoke.py query --departure 2026-11-04T08:00 --zone UTC --scenario DEMO_UNKNOWN
```
Usar una salida futura para Referencia. La demo conserva su fecha/reloj fijos
2026-11-02; no se mueve a la fecha pedida. Sin IDs no hay acceso a la red. Con
`--origin-id 18000 --destination-id 17000` se consulta contexto
Renfe cualitativo. Referencia no es predicción de operación; avisos no suman minutos.
El cliente SDK usa shell privilegiado; no autoriza a una app Android ordinaria.
La autorización de pruebas se dejó revocada al terminar.

1. [Guía UC-006](specs/006-journey-capability/quickstart.md).
2. [Evidencia y límites UC-006](specs/006-journey-capability/verification.md).
3. [Evaluación local UC-005](specs/005-arrival-assessment/quickstart.md).
4. [Calendario UC-004](specs/004-calendar-selection/quickstart.md).
5. [Tablero de casos](specs/README.md).

## SDD y aprendizaje
dc44f8d guarda spec/plan UC-006 antes del producto. Cada caso conserva spec,plan,
tasks y verification; [método](docs/sdd-workflow.md). Checklist humano8/10 permanece
sin cambios. [Android desde Flutter](docs/android-for-flutter.md) explica Service,
Binder, KSP, Hilt y consentimiento frente a ViewModel/BLoC.

## Siguiente incremento
UC-007 compartirá una evaluación mínima de calendario, con autorización propia.
Permanece sin iniciar. UC-008 integrará ChatApp y TestingAgent oficiales.
