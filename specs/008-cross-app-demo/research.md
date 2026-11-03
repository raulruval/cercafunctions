# Investigación UC-008 ·2026-11-03
Decisión: pin oficial0bb367a2b7ef276218ac37f8eb45f6c9a81ca0cd, copia nueva,
Windows Git core.longpaths por comando, nunca cambiar configuración global.
https://github.com/android/appfunctions/tree/0bb367a2b7ef276218ac37f8eb45f6c9a81ca0cd
La investigación delegada read-only exigida por speckit-plan confirmó contratos.

ChatApp README usa nombres antiguos: el código actual ofrece searchContacts(query,
contactType), sendMessage(endpointValue,messageBody,imageUris?), searchMessages y
fetchSendMessageDetails. Se descubren IDs, no se copian los del README desactualizado.
Ana no existe: fixture añadida, nunca renombrar Alice. MessageRepositoryImpl almacena
MutableStateFlow y simula demora, no entrega red ni persistencia tras proceso muerto.

Agent standardDebug permite consultas sin clave. Launcher oficial requiere en37+
allowlist paquete+firma y flush; am instrument -w con ShellIdentityInstrumentation
adopta EXECUTE_APP_FUNCTIONS y conserva latch. No basta pm grant o lanzamiento normal.
No set-temporary-caller. No leer preferencias de claves Gemini existentes.
https://github.com/android/appfunctions/blob/main/agent/startAppFunctionTestingAgent

AgentOrchestrator invoca herramientas LLM sin gate genérico de mensajes. Confirm action
se usa para PendingIntent. A2UI preview de proveedor no demuestra renderer/confirmación.
Alternativa descartada: prompt que pide confirmar sin impedir envío. Elegida: pequeña
CLI determinista con modelo de propuesta/confirmación, IO SDK y pruebas de fronteras.
No parchar lógica del agente/proveedor ni añadir app de mensajería/backend.
https://github.com/android/appfunctions/blob/main/agent/app/src/main/java/com/example/appfunctions/agent/domain/AgentOrchestrator.kt

Skill Google se aplica a revisión/descubrimiento de proveedores; construir consumidor
no está cubierto por ella. Contexto/testing y referencias de implementación/KDoc
contrastadas con beta01. Cancelación/TTL provienen de UC-007; cliente no emite grants.
