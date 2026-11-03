# Investigación ·UC-007 ·2026-11-03
Decisión: segunda función en entry point existente, AppFunctions beta01 vigente,
sin migración legacy. Skill Google actual y sus4 referencias/contexto leídas;
snippets antiguos contrastados con API real. KDoc inline, instructions y annotations
explícitas. https://developer.android.com/jetpack/androidx/releases/appfunctions
https://github.com/android/skills/blob/main/device-ai/appfunctions/SKILL.md

Decisión: un grant temporal con hash del token y HMAC-SHA256(token,campos con
longitudes), sin títulos ni rawtoken persistidos. Datos mínimos en DataStore
privado existente; disable/replace ordenados como UC-006, CAS por hash al invalidar
para no borrar un grant nuevo desde petición antigua. Evitar permisos genéricos
o SDK de autenticación; referencia no autoriza crear grants ni listar agenda.

Decisión: Clock civil+elapsedRealtime+BOOT_COUNT; reinicio o incoherencia deniegan.
Proceso frío mismo boot sigue funcionando. Deadline monotónico impide extender
30min por rollback; tolerancia drift5s no amplía deadline. No forzar reloj real
del emulador: pruebas con clock inyectado.
https://developer.android.com/reference/android/os/SystemClock
https://developer.android.com/reference/android/provider/Settings.Global#BOOT_COUNT

Decisión: versión de campos ya observados de Instances (incluye título/calendario).
LAST_DATE no es lastModified, sino límite de recurrencia. No aumentar proyección
a descripción o asistentes. Límite de proveedor UC-004 se conserva. No detectar
historial de revocación/reconcesión que proceso muerto no pudo observar.
https://developer.android.com/reference/android/provider/CalendarContract.EventsColumns

Decisión: resultado público propio, no JourneyResponse, porque este incluye
origen/destino. Mismos casos QueryJourneyArrival/AssessArrival que UI; checks de
token/permiso/versión se repiten antes de devolver. Autorización inválida no devuelve
ni siquiera un indeterminado con horarios. Shell positivo privilegiado y testhost
otro UID negativo, sin LLM. ChatApp/TestingAgent integración queda UC-008.
