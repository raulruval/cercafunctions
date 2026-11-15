# appfunctions

La entrada principal de UC-009 es **evaluateDemoJourney()**, sin parámetros:
calendario real de mañana + escenario de tren sintético guardado en la UI.
Comparte únicamente tiempos y resultado, con permiso Android y consentimiento
explícito persistente/revocable. No necesita grant opaco. Consumidor ANDROID_TRUSTED.
Está habilitada por defecto para descubrimiento, pero los guards deniegan el
resultado sin ambas autorizaciones. Relee el calendario en cada ejecución.
UI y servicio comparten EvaluateDemoJourney; no se inyecta ningún ViewModel.
Ver [guía](../docs/demo-simple.md) y [UC-009](../specs/009-simple-demo/spec.md).

## Capacidades avanzadas preservadas

Adaptador Android de capacidades: queryJourney y evaluateAuthorizedCommitment.
Depende de dominio, Jetpack AppFunctions beta01, Coroutines/Hilt; ambos compiladores
KSP generan servicio, DTO y XML. Un entry point conserva IDs publicados de UC-006.
No depende de presentation/platform/data; app ensambla los casos y repositorios.

queryJourney exige opt-in propio del trayecto. evaluateAuthorizedCommitment exige
una referencia de ocurrencia emitida solo desde UI, con30min/comienzo como límite.
No reutiliza flag de trayecto ni ofrece emisión/listado de grants por IPC.
Ambas funciones disabled-default y ANDROID_TRUSTED compat; BIND protege servicio.
El sistema autoriza consumidor y el dominio comprueba consentimiento: gates separados.

DTO de evaluación propio con horarios/margen/estado/evidencia, sin origen/destino,
IDs de agenda, títulos, notas, participantes, correos o token. Reutiliza AssessArrival;
no invoca ViewModels ni permisos UI. KDoc inline e instructions describen alcance,
fuentes/fixtures y comparación estación–reunión0. No hay mensajería.

Patrón reutilizable: adaptador/DI/KSP, contrato mínimo, autorización acotada y tests
de IPC. Funciones/DTO ferroviarios son del producto: este AAR no es un SDK genérico.
Guía: specs/007-assessment-capability/quickstart.md y verification.md.
Testhost de biblioteca usa UID distinto para negativas; SDK shell positivo es
privilegiado. ChatApp/TestingAgent y orquestación LLM quedan UC-008.
