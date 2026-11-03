# Verificación UC-008 ·2026-11-03
Estado: Verified.13 tareas del incremento; cierre de convergencia al final.
18 pruebas nuevas Python correctas. Las245 Android de UC-007 se conservan como
baseline; no se reejecutan ni se presentan como pruebas nuevas de este caso.
Ningún módulo Kotlin/build del producto cambia. Se compilaron e instalaron los
dos proyectos oficiales: ChatApp63 tareas/1m59s y agente45 tareas/2m35s, sin errores.
[SDK real](sdk-results.json), [APK/hash](artifacts.json), [guía Windows](quickstart.md).

## Trazabilidad
| Criterio | Evidencia ejecutada |
| --- | --- |
| S1 | Trayecto35/salida08:00 frente09:00→ON_TIME, prepare=None, cero envíos; comprobado por SDK |
| S2 / FR-002–003 | Salida08:40→09:15, LATE/-15 REFERENCE; búsqueda real Ana devuelve Ana POC/uc008-ana, texto y hora UTC explícitos sin título/agenda |
| S3 / SC-001 | Gates con cadena vacía y NO:0 invocaciones y0 mensajes exactos antes/después; CLI preview real por stdin tampoco envía |
| S4 / SC-002 / FR-004–005 | El usuario confirmó expresamente en chat destinatario y texto exactos. Un único sendMessage real, searchMessages pasa0→1. Segunda confirmación del mismo gate conserva1 llamada. UI de ChatApp inspeccionada con ese único mensaje |
| S5 | SDK DEMO_UNKNOWN no propone; Bob devuelve ambigüedad sin elegir primero; contacto inexistente no produce propuesta. Unitarias rechazan evidencia incoherente antes de consultar contactos |
| S6 / SC-003 | Testing Agent standardDebug instalado, allowlist acotada e instrumentación oficial viva; su Debugger descubre Copilot y ejecuta searchContacts. Sin clave/llamada LLM configurada en esta ejecución; fallback por ausencia de gate general de envío |
| FR-001 / privilegios | Launcher PowerShell equivalente al oficial, firma obtenida del paquete e identidad EXECUTE_APP_FUNCTIONS por instrumentación. No pm grant ni set-temporary-caller. Instrumentación detenida al acabar |
| FR-006–007 | Copia oficial pin0bb367a2b7ef276218ac37f8eb45f6c9a81ca0cd; único cambio funcional añade Ana ficticia, no renombra otro contacto. MessageRepository original en memoria, sin entrega de red |
| Privacidad / lifecycle | SDK mata proceso debug Copilot por su UID, invoca con nueva PID sin abrir Activity. Tokens solo RAM/entrada, sin archivos/argv host; fixture propia limpiada y ambas capacidades desactivadas según lectura final |

## Pruebas y problemas resueltos
Comando: python -m unittest discover -s tests -p test_interop_flow.py.
13 pruebas fallaron antes de implementar;18 finales pasan sin fallos/omitidas.
La regresión del descubrimiento falló antes de corregirla: applicationId no debe
inferirse del paquete Kotlin y functionId no equivale al ID interno del documento.
Se usa functionId de metadatos filtrados por paquete. Windows redirected stdout
usaba una página de códigos antigua: el CLI ahora emite UTF-8 explícito y el preview
real completo volvió a pasar. Sin desactivar controles de consentimiento.
Git requirió core.longpaths=true por comando para archivos screenshot de muestras;
no se cambió configuración Git global ni lógica de proveedor/agente.

Evidencia visual inspeccionada: [resultado del agente](../../docs/evidence/uc008-agent-contact-result.png),
[Copilot descubierto/desactivado](../../docs/evidence/uc008-agent-copilot-functions.png),
[mensaje aprobado](../../docs/evidence/uc008-chatapp-approved-message.png).
Solo datos sintéticos. El botón Confirm del resultado del Debugger es un cierre
del resultado, no una prueba de gate previo de envío.

## Límites reales
SDK shell y agente instrumentado son privilegiados; no se ha demostrado acceso de
una app Android ordinaria. El rechazo de otro UID ya se verificó en UC-006/007.
El flujo completo es determinista; no se afirma una cadena LLM autónoma. El agente
invoca herramientas directamente y su Confirm action protege PendingIntent, mientras
sendMessage devuelve String. Preview A2UI por sí solo no establece una barrera.
No se introdujeron claves Gemini ni se leyeron preferencias para buscarlas.

La única escritura aprobada llama al ejemplo oficial y queda en su proceso, no en
red/servidor/Room. No matar ChatApp antes de verificar: los mensajes desaparecen
tras morir el proceso. Gate de intento único por sesión, no exactly-once distribuido:
UNKNOWN no reintenta, pero un reinicio del consumidor no tiene ledger durable.
Revalidar tras confirmación evita enviar una propuesta cambiada observada, sin
congelar proveedor/consentimiento entre procesos. La duración de referencia35min no es pronóstico
operativo. Sigue sin incluir tránsito estación–reunión o margen adicional.

Fixture borrada con propiedad comprobada. READ_CALENDAR retirado y ambas funciones
de Copilot disabled. Agente detenido; la allowlist del paquete de laboratorio puede
permanecer, sin identidad shell activa. ChatApp queda mostrando el mensaje sintético.
No matriz de dispositivos/hardware, ni evaluación de Gemini/credenciales/red de chat.
Checklist humano8/10 intacto: la confirmación de esta escritura sí se recibió;
no se atribuye al usuario una lectura de toda la documentación SDD.

## SDD
e5604a5 conserva spec/plan antes de implementación. Converge limpio:16 criterios
(7FR+3SC+6escenarios),7 decisiones,5 principios y13 tareas contrastados con código
y evidencia, sin comparación Git como evaluación. Sin hooks ni tareas añadidas.
tasks.md permaneció byte por byte intacto durante converge: SHA256
3C6AA2D5F9DAE707AEFE034B87C81F95B65FF67C3262C2266C20F4A619DEB427.
T013 se cierra después, fuera de converge. Rama feat/008-cross-app-demo y etiqueta
uc-008-v1, sin fusionar main. No se inicia trabajo fuera de UC-008.
