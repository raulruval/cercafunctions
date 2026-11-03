# core-appfunctions
Adaptador Android de capacidades, ahora con queryJourney. Depende de dominio,
Jetpack AppFunctions beta01, Coroutines y Hilt. KSP procesa servicio y DTO en este
módulo. No depende de presentation, data ni platform; app ensambla los casos y
la autorización persistida. No duplica cálculo de llegada o reglas ferroviarias.

BaseJourneyAppFunctionService es el entry point abstracto. KSP genera
JourneyAppFunctionService, XML/serializadores y FUNCTION_ID_QUERY_JOURNEY. Manifest
registra el servicio generado bajo permiso BIND_APP_FUNCTION_SERVICE. Metadata
de aplicación usa instructions y DTO usan KDoc inline con tipos java.time nativos.

isEnabled=false por defecto, acceso ANDROID_TRUSTED con compat enforcement. UI
autoriza compartir trayecto; DataStore conserva flagfalse inicial. El servicio
comprueba ese consentimiento antes y después de leer. Habilitar con ADB no lo
sustituye. No abre pantallas ni permisos. Entrada no válida produce error acotado;
cancelación se propaga. No consulta ni devuelve calendario/contactos/mensajes.

Reutilización: patrón de servicio/DI, KSP, documentación, permisos, consentimiento
y pruebas de contrato/IPC. Los DTO y queryJourney son propios de Cercanías: este
módulo no es un SDK corporativo genérico. Un segundo producto permitirá extraer
infraestructura comprobada sin depender de dominio ferroviario.

Guía y límites: specs/006-journey-capability/quickstart.md y verification.md.
Los tests directos del servicio no prueban Binder; UnauthorizedCallerTest usa
otro UID sin identidad shell y necesita el APK app instalado. Shell positivo
es un cliente privilegiado de laboratorio. ChatApp/TestingAgent LLM siguen UC-008.
