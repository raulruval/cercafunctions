# Viabilidad del flujo entre aplicaciones

## Resultado del análisis

La arquitectura es viable. La demostración completa es condicional: requiere
servicio AppFunctions disponible, descubrimiento operativo, permisos del
calendario y privilegios del consumidor. No se da por probado un flujo LLM
ni un envío fuera del ejemplo de ChatApp en esta fase.

| Paso previsto | Proveedor | Condición / límite |
| --- | --- | --- |
| Consultar trayecto | Cercanías Copilot | Configuración persistida y datos con procedencia |
| Evaluar reunión | Copilot + Calendar Provider | READ_CALENDAR concedido en UI y autorización de compartir un resumen |
| Resolver Ana | ChatApp oficial | Ana no existe en sus contactos de fábrica; preparar un fixture local documentado del ejemplo o revisar el destinatario de demo |
| Preparar propuesta | Agente externo + `fetchSendMessageDetails` | Previsualización de destinatario y texto; sin envío automático |
| Enviar tras confirmación | `sendMessage` de ChatApp | Invocación real al servicio oficial; almacenamiento en memoria, sin backend de mensajería |

No se presupone que Google Calendar exponga AppFunctions. Copilot consulta el
proveedor Android mediante CalendarContract; el agente consulta Copilot.

## Renfe y el significado de «mañana»

Fuentes públicas identificadas:

- [Horarios Cercanías](https://data.renfe.com/dataset/horarios-cercanias): recurso GTFS
  `https://ssl.renfe.com/ftransit/Fichero_CER_FOMENTO/fomento_transit.zip`.
- [Incidencias y avisos](https://data.renfe.com/dataset/incidencias-avisos):
  `https://gtfsrt.renfe.com/alerts.json` y `https://gtfsrt.renfe.com/alerts.pb`.
- Metadatos consultables mediante CKAN `api/3/action/package_show` y `package_search`.

Se ha comprobado el catálogo y sus recursos, sin implementar su ingestión ni
validar todavía cobertura de estaciones, calendario de servicio o predicción
cuantitativa. Una alerta de accesibilidad o autobús sustitutorio no equivale a
minutos de retraso. Ausencia de alertas tampoco demuestra puntualidad.

Para mañana, GTFS solo permite una llegada programada si coinciden trayecto,
fecha, servicio y horario vigente; hay que tratar `calendar_dates`, horas GTFS
mayores de 24, zona horaria y cambios de día. Las alertas tienen periodos de
validez; ninguna alerta actual se convierte por defecto en retraso futuro.
Una duración aportada por el usuario solo permite una estimación de referencia.

En SDD habrá que distinguir al menos horario programado, estimación real vigente,
duración de referencia, escenario fixture y desconocido, con fecha de consulta
y horizonte de validez. Si falta evidencia suficiente, devolver incertidumbre.
Los fixtures de puntualidad y retraso serán reproducibles, con reloj fijo, y
visiblemente separados de cualquier dato Renfe. Aún no se han creado.

## Calendario y datos mínimos

La futura integración usará `CalendarContract.Instances` en un intervalo limitado
para resolver recurrencias, consultando solo columnas necesarias. Se decidirá
qué evento representa «mi reunión»: selección previa del usuario o criterio
inequívoco y, en caso de ambigüedad, selección explícita. Tratar zona horaria,
eventos de día completo, cancelados, calendarios vacíos y permisos revocados.

READ_CALENDAR autoriza lectura local, no publicación indiscriminada al agente.
El resumen externo debe contener únicamente hora de compromiso autorizada,
llegada estimada, diferencia/margen, procedencia y estado de incertidumbre.
No entregar agenda completa, título, descripción, asistentes, emails ni notas.
El servicio en segundo plano no abre un diálogo de permisos; devolverá una
necesidad de intervención en UI si falta autorización.

## Confirmación de mensajes

El código actual de ChatApp ofrece previsualización, pero `sendMessage` ejecuta
el envío directamente y no exige un token de confirmación. Un texto en KDoc
o un prompt no proporciona una barrera de seguridad verificable.

La demo futura deberá detenerse en la previsualización y requerir una acción
explícita sobre destinatario y mensaje exactos antes de invocar `sendMessage`.
Si el agente oficial no garantiza esa pausa, se usará su depuración manual
determinista: preparar, revisar, confirmar e invocar manualmente. Se probarán
rechazo y ausencia de envío, además de aceptación. No se implementará un chat
propio ni se afirmará una garantía de confirmación impuesta por el proveedor.

## Testing Agent y emulador

La skill requiere compileSdk 37+, targetSdk 36+ y Jetpack AppFunctions beta01+.
La base elige compile/target 37. La UI admite API 26+; AppFunctions se valida
en API 36+ y se prefiere una imagen 37+ con el servicio realmente disponible.

`EXECUTE_APP_FUNCTIONS` no es un permiso runtime que una app ordinaria obtenga
con un diálogo. El ejemplo usa `ShellIdentityInstrumentation` para adoptar
identidad shell y mantiene viva la instrumentación. No se resuelve con
`pm grant` ni implica privilegios de producción.

En SDK 37+ el script oficial configura además una allowlist con paquete y
huella; adapta comandos según `cmd app_function help` y `cmd allowlist help`.
Usar sus scripts actuales evita fijar comandos internos que varían entre
imágenes. No exige presuponer root o instalar como app del sistema.
Las pruebas instrumentadas pueden interrumpir esa identidad: ejecutar en secuencia.

El modo LLM añade API key/proveedor disponible y conectividad. El README
documenta variantes `standard` y `retail`; esta última admite la clave Gemini
en build, y el código actual también consulta ajustes del proveedor.
La disponibilidad real debe comprobarse, sin incluir claves en Git ni logs.
La depuración con parámetros deterministas y ADB permite validar los contratos
y la comunicación sin depender de la orquestación LLM.

La aceptación futura separará pruebas de dominio, descubrimiento, invocación
individual, cadena determinista con confirmación y prueba LLM opcional.
No se confundirán esas evidencias.
