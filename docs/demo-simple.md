# Demo sencilla: calendario real + trayecto de demostración

## Preparación y recorrido

1. Abre Cercanías Copilot. La pantalla principal es ahora la demo; el recorrido
   anterior queda en **Avanzado · referencia y calendario manual**.
2. Pulsa **Permitir acceso al calendario** si aparece. Se usa `CalendarContract`
   del dispositivo, no Google Calendar API, ni un calendario inventado por Copilot.
   Puede leer eventos locales o sincronizados que el proveedor tenga disponibles.
3. Debe existir una reunión **con hora mañana**, en un calendario visible. La app
   elige la primera por hora de inicio (empates por ID), excluyendo canceladas y
   eventos de día completo. La zona del dispositivo aparece junto a la hora.
   Si no existe, crea una desde tu app de calendario y pulsa **Actualizar**.
   No hace falta ejecutar ningún script de eventos; no se añaden eventos automáticamente.
4. Elige **Puntual**, **+15 min** o **Sin datos**. El escenario se guarda en DataStore.
   El título del evento se muestra solo aquí, dentro de Copilot.
5. Activa **Permitir consulta del agente**. Es una decisión distinta del permiso de
   lectura: autoriza el horario mínimo de la próxima reunión de mañana y el resultado,
   mientras siga encendido. **Ya no necesitas copiar una referencia temporal**.
6. Abre Testing Agent → **Debug** → Cercanías Copilot → **evaluateDemoJourney** → ejecutar.
   No tiene parámetros: deja el formulario vacío (`{}` en una llamada por terminal).
   Las otras dos funciones son el recorrido avanzado, con sus antiguos controles.
7. Para Gemini, en **Agent Demo** conecta Cercanías Copilot, conserva tu API key
   en Settings y utiliza el prompt de abajo. La clave no se incluye en scripts ni en Git.
   Empieza conectando solo Copilot: basta para consultar y preparar un borrador.

> Consulta mi trayecto de Cercanías de mañana y comprueba si llegaré a tiempo a mi
> próxima reunión. Si no llego, prepara un mensaje para Ana POC pidiéndole que la retrase.

En el escenario +15 min, espera una comparación de llegada tardía y un borrador.
El LLM decide cómo expresarlo: Debug es la comprobación determinista si falla el
modelo, su cuota o la selección de herramientas. No se ha convertido un prompt en
un mecanismo de seguridad de envío.

## Qué es real y qué es de demo

| Elemento | Procedencia / comportamiento |
| --- | --- |
| Reunión | Primera ocurrencia con hora de mañana en CalendarContract real. Se relee en cada consulta. |
| Fecha | Día local actual +1 en la zona del dispositivo; no es una fecha fija. |
| Ruta | Atocha (demo) → Chamartín (demo), 35 min sintéticos; no es un tren ni horario de Renfe. |
| Salida | Se ajusta deliberadamente a 35 min antes del inicio de la reunión. Puede caer el día anterior si la reunión es poco después de medianoche. |
| Puntual | Llegada exactamente al inicio, margen 0. |
| +15 min | Llegada 15 min después del inicio, margen −15. |
| Sin datos | Llegada y margen nulos; resultado INDETERMINATE. No asume puntualidad. |
| Agente | Recibe ruta ficticia, tiempos, zona, fuente y resultado; nunca título, notas, asistentes ni ID del evento. |
| Renfe | No se consulta en la pantalla principal ni en la nueva AppFunction. Sigue en Avanzado. |
| Mensaje | Copilot no envía. Preparar el borrador es una operación sin envío. |

La comparación es llegada a estación contra comienzo de reunión; no incluye
traslado final ni margen adicional. La demo ilustra intercambio real de capacidades
con datos de calendario reales y un transporte controlado, no una predicción real.

## Reiniciar el Testing Agent

Después de reiniciar el emulador hay que arrancarlo con privilegios de laboratorio;
abrir su icono directamente puede mostrar **Missing Privileged Access**.
Desde PowerShell en `C:\dev\cercafunctions`:

```powershell
.\scripts\start-testing-agent.ps1 -Serial emulator-5554
```

Es el equivalente Windows del `run_privileged.sh` oficial ya utilizado. No requiere
root ni entrega el permiso a cualquier app; mantiene la instrumentación de pruebas.
No ejecutar UIAutomator o pruebas instrumentadas a la vez. Si el número de dispositivo
cambia, consulta `adb devices` y cambia `-Serial`. Para detenerlo añade `-Stop`.

## ChatApp y confirmación

ChatApp sigue siendo el ejemplo oficial. Se puede mostrar su contacto ficticio
**Ana POC** y, en Debug, su búsqueda de contactos para ilustrar otra aplicación.
La nueva función de Copilot no busca contactos ni envía mensajes.
Para un envío, usa el flujo determinista ya documentado en UC-008, confirma
destinatario y texto exactos, o invoca manualmente la función de ChatApp después
de revisarlos. El ChatApp del laboratorio solo conserva mensajes en memoria local.

Conectar ChatApp al LLM expone también su herramienta de envío: el ejemplo oficial
no proporciona nuestro gate determinista de confirmación. No se considera seguro
afirmar que pedir «solo prepara» en el prompt impide técnicamente un envío. Para
la toma sencilla usa Copilot conectado y deja el envío como demostración manual
separada, o conserva el consumidor determinista de UC-008.

## Seguridad simplificada, con un alcance distinto

La función nueva está publicada y habilitada por defecto para que sea descubrible,
pero comprueba `READ_CALENDAR` y el interruptor antes y después de evaluar. Android
sigue comprobando el consumidor. Apagar el interruptor bloquea futuras consultas,
aunque el proceso de Copilot no tenga ninguna Activity abierta.

El interruptor persiste y autoriza una **consulta continua del siguiente compromiso
de mañana**, que puede cambiar entre llamadas y días. No es autorización para un
evento fijo. Esta es la simplificación frente al grant acotado de UC-007: el usuario
elige un alcance mayor, visible y revocable; los horarios siguen siendo personales.
No concedemos automáticamente el permiso ni activamos el interruptor al instalar.
Un dato ya recibido por un agente no se puede retirar apagándolo después.

## Código para enseñar

- `presentation/.../demo/DemoScreen.kt`: permiso desde UI, escenario, resultado e interruptor.
- `presentation/.../demo/DemoViewModel.kt`: Estado StateFlow y operaciones de usuario; equivalente al papel de un Cubit.
- `domain/.../demo/EvaluateDemoJourney.kt`: caso de uso compartido que lee el calendario y construye la entrada sintética.
- `domain/.../assessment/CompareArrivalTiming.kt`: regla pura reutilizada también por AssessArrival del flujo anterior.
- `platform/.../demo/DataStoreDemoSettingsRepository.kt`: persiste escenario y decisión, sin guardar títulos/eventos.
- `appfunctions/.../BaseJourneyAppFunctionService.kt`: `evaluateDemoJourney()` sin parámetros, guards y mapeo del DTO.
- `appfunctions/.../DemoJourneyResponse.kt`: contrato mínimo con KDoc que publica KSP.
- `appfunctions/.../res/xml/app_metadata.xml`: distingue el recorrido sencillo de las dos APIs avanzadas.

Son rutas relativas a la raíz del repositorio. UI y AppFunction ejecutan el mismo
EvaluateDemoJourney; el servicio no depende de un ViewModel ni de una pantalla abierta.
No se añaden módulos ni una infraestructura de agentes propia.

## Si Gemini muestra «Called unknown function»

Incidencia observada el 2026-11-04 con gemini-3.5-flash-lite: el usuario ejecutó el
prompt corto con Copilot y ChatApp conectados. Los logs confirman resultado LATE
de evaluateDemoJourney (fuentes FIXTURE y ANDROID_CALENDAR), seguido de un resultado
de searchContacts de ChatApp sin error. La siguiente llamada generada fue
`appfunctions_BaseJourneyAppFunctionService_fetchSendMessageDetails`, que no existe.
El nombre combina el servicio de Copilot con un método de ChatApp. El método válido
pertenece a `BaseChatAppFunctionService`. El Testing Agent rechazó el nombre por no
estar en sus herramientas disponibles; no lo redirigió ni ejecutó.

No es un error de READ_CALENDAR en esta ejecución. La capacidad de Copilot y la
búsqueda entre apps se utilizaron realmente; la preparación completa quedó interrumpida.
No se afirma que el mensaje se preparase ni se enviase en ese intento.

Para intentar corregir la selección en esa conversación, indicar:

> La preparación del mensaje corresponde a ChatApp. Utiliza fetchSendMessageDetails
> de ChatApp para preparar la propuesta para Ana POC. Detente antes de enviar.

Esto es una corrección para el modelo, no una garantía de selección correcta ni
un gate técnico de escritura. Para el recorrido sencillo y reproducible, crear
una conversación nueva con + y conectar solo Copilot para obtener el resultado y
redactar el borrador en texto. Para enseñar ambas apps, Debug permite invocar la
búsqueda y la preparación de ChatApp sin depender de la selección de herramientas
del LLM. Se mantiene la revisión explícita antes de cualquier envío.
