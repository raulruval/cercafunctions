# Investigación UC-006 ·2026-11-03
## Decisión: beta01 y servicio generado
AppFunctions1.0.0-beta01 sigue vigente,21oct2026. Se leyó skill oficial Google
actual28oct y sus4 referencias más contexto. Versionado ya fijado, sin migración.
Los snippets @AppFunction/appfn:description de la propia skill son antiguos: usar
@AppFunctionDeclaration(isDescribedByKDoc=true), serializables con KDoc inline e
instructions. KSP y Hilt en core-appfunctions, entry point abstracto único y
servicio concreto generado registrado con BIND_APP_FUNCTION_SERVICE. Fuente:
https://developer.android.com/jetpack/androidx/releases/appfunctions
https://github.com/android/skills/blob/main/device-ai/appfunctions/SKILL.md
https://github.com/android/appfunctions/tree/0bb367a2b7ef276218ac37f8eb45f6c9a81ca0cd
Se contrastaron sources.jar beta01 de Google Maven y ChatApp/shared de ese commit.
Alternativa descartada: API legacy, manual provider o un servicio propio de IPC.

## Decisión: consulta única, dos casos de dominio
QueryJourneyArrival y QueryRailContext ya están en UI. Servicio hace validación,
ejecución IO y proyección DTO, sin lógica temporal nueva. java.time nativo evita
inventar formato de serialización. Resumen de contexto sin texto de feed reduce
payload/datos no necesarios. No ampliar a calendario o mensajería.

## Decisión: consentimiento y autorización distintos
isEnabled=false; usuario autoriza desde UI y DataStore guarda bool false por
defecto. Al habilitar: sistema antes de guardartrue; si se interrumpe, gate sigue
cerrado. Al revocar: false antes de deshabilitar sistema, fail closed. Callback
inyectado en adaptador de plataforma evita dependencia de plataforma en AppFunctions.
Servicio comprueba consentimiento antes/después. Trusted Android + permiso BIND
protegen IPC; AccessLevel explícito con compat enforcement. No booleano enviado
por agente, allowlist propia de paquetes ni consentimiento inferido de READ_CALENDAR.
https://developer.android.com/reference/kotlin/androidx/appfunctions/AppFunctionAccessLevel

## Decisión: pruebas reales y límites de privilegios
API37.2 emulator Pixel10Pro. cmd app_function help leído: list,execute,set-enabled,
get-app-function-states e is-enabled. Shell positivo es privilegiado; negativa
requiere otro UID. Se usará testhost de biblioteca core-appfunctions frente al
APK app instalado, verificando UIDs diferentes. AppFunctionTestRule beta01 usa
fakes/Robolectric y no sustituye esa comprobación. Sin nueva app/módulo de agente.
Testing Agent oficial necesita launcher ShellIdentityInstrumentation para adoptar
EXECUTE_APP_FUNCTIONS; abrirlo normalmente no lo autoriza. No hace falta LLM.
https://github.com/android/appfunctions/blob/0bb367a2b7ef276218ac37f8eb45f6c9a81ca0cd/agent/startAppFunctionTestingAgent
