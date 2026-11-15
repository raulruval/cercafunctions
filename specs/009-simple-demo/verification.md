# Verificación UC-009 — 2026-11-04

## Resultado
Construido e instalado en emulator-5554 / Pixel_10_Pro, Android API37.2 x86_64.
Calendario REAL mediante CalendarContract, no un repositorio de calendario falso.
El dispositivo usa GMT; la fecha local siguiente observada fue 2026-11-05.
No se crearon ni modificaron eventos para este recorrido. El transporte sí es sintético.

## Evidencia ejecutada
- 187 pruebas JVM: domain84, data21, platform23 y presentation59; cero fallos/errores.
- 72 pruebas Android reales: presentation43, appfunctions22 y app7; cero fallos/omitidas.
  Incluyen nueva UI, DTO mínimo, revocación durante consulta, permiso denegado,
  cancelación, consumidor de UID extranjero y regresión del recorrido avanzado.
- APK debug y APKs de pruebas compiladas. `:app:lintDebug` pasa sin errores fatales.
  Se conservan avisos previos del proyecto y de deprecación de API de pruebas Compose.
- Registro del sistema: tercera capacidad evaluateDemoJourney, habilitada por defecto,
  androidTrusted, sin parámetros y KDoc generado. Las dos capacidades anteriores conservan IDs.
- Consulta SDK real `{}` con el calendario del dispositivo: ON_TIME/margen0,
  LATE/margen−15 e INDETERMINATE/arrival y margen ausentes. Cambio de escenario
  desde UI seguido de consulta IPC, sin introducir fechas ni referencias.
- Consulta positiva tras `am force-stop` y `pidof` sin proceso: el servicio arranca
  sin Activity usando configuración persistida y la misma evaluación del dominio.
- Consulta con interruptor apagado: ERROR_DENIED/code1000 sin resultado de calendario.
- Inspección visual de pantalla inicial sin permiso y de pantalla con reunión real:
  fuentes diferenciadas, controles de escenario, horarios, resultado e interruptor.
- Tras probar se restaura +15 min y consentimiento del agente apagado; el permiso
  de lectura queda concedido para visualizar el calendario en Copilot.

Comandos de construcción/verificación:
```powershell
.\gradlew.bat :domain:test :data:test :platform:testDebugUnitTest :presentation:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :appfunctions:connectedDebugAndroidTest :presentation:connectedDebugAndroidTest :app:connectedDebugAndroidTest
```
La nueva pantalla obliga a las pruebas anteriores de Activity a abrir Avanzado.
La prueba de fechas de ArrivalFlow se actualizó a mañana en vez del antiguo día fijo.
Los logs, JSON de registro y capturas se conservaron como artefactos locales de la
sesión, no como contenido de calendario versionado. La evidencia no incluye claves.

## Límites de lo comprobado
No se ejecutó Gemini ni se utilizó la clave del usuario. El agente LLM y sus cuotas
siguen siendo un componente externo: no se afirma una orquestación LLM validada.
No se invocó sendMessage ni se autorizó un envío nuevo. El ejemplo oficial de agente
no garantiza el gate de escritura; para la toma sencilla se conecta solo Copilot y
se prepara un borrador, dejando ChatApp/manual o UC-008 como alternativa de envío.
No se valida una predicción ferroviaria, traslado final ni disponibilidad de red Renfe.

## Aprendizaje Android desde Flutter
DemoViewModel/StateFlow tiene el papel de Cubit/Stream de estado. El cambio clave
para AppFunctions es que DataStore y CalendarContract son independientes de ese
estado de pantalla: otro proceso llama al servicio y Hilt monta el mismo dominio.
El permiso de Android autoriza la lectura de la app; el interruptor añade la decisión
de compartir horarios. No son un token copiado ni una dependencia del lifecycle de UI.

## Ensayo LLM del usuario posterior a la entrega — 2026-11-04
La captura del usuario muestra «Called unknown function». Se revisó el código del
Testing Agent oficial: GeminiProviderImpl rechaza nombres que no coinciden con las
herramientas descubiertas; GeminiToolConverter conserva un sufijo del ID al generar
su alias. No hay una redirección automática entre aplicaciones.
Se inspeccionaron solo nombres de herramientas y marcadores de resultado de los
logs, sin imprimir clave, detalles de contactos ni contenido privado del calendario.
El modelo llamó evaluateDemoJourney y recibió LATE/FIXTURE/ANDROID_CALENDAR; luego
llamó searchContacts de ChatApp y recibió resultado sin error. La siguiente llamada
mezcló BaseJourneyAppFunctionService con fetchSendMessageDetails, método de ChatApp,
y se rechazó antes de invocarlo. Se verifica el progreso de lectura entre apps,
no una preparación/envío completos ni una corrección garantizada del comportamiento LLM.
El asistente no lanzó nuevas peticiones Gemini ni funciones de mensajería.
Se añadió diagnóstico y alternativa de ensayo en docs/demo-simple.md.

## Cierre documental y Git — 2026-11-15
Se revisó el estado final y se prepararon conjuntamente los cambios autorizados que
seguían locales: renombrado de los cinco módulos, fechas relativas a mañana y UC-009.
README, START_HERE, arquitectura, setup y tablero SDD se actualizaron al recorrido final.
Los snapshots previos se conservan; los nombres antiguos solo describen historia
o los repositorios externos Rocket-Beer. El índice Git reconoce 95 movimientos.

Se comprobaron enlaces de entrada, nombres Gradle y ausencia de artefactos locales,
calendarios o credenciales detectables entre los archivos preparados para commit.
Se conservan los informes previos de 187 tests JVM y la evidencia de 72 Android.
El intento de repetir Gradle en esta sesión no pudo configurar :domain: Java informa
que el directorio no existe o no es escribible, aunque está presente y accesible desde
las herramientas de archivos. No se declara una compilación nueva ni se modifica código
funcional para eludir esta restricción del entorno.

La publicación remota debe confirmarse por comparación de refs, no por haber generado
el commit. Si el gestor de credenciales no puede ejecutarse, se entrega el estado
pendiente explícitamente para completar el push desde una terminal del usuario.
