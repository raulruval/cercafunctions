# UC-009 — Demo sencilla con calendario real
Estado: Verified; ver verification.md. Solicitud de implementación y corrección de alcance recibidas el 2026-11-04.

## Objetivo
Mostrar AppFunctions con una pantalla y una consulta sin parámetros. El calendario
es el real del dispositivo; únicamente el trayecto y sus retrasos son sintéticos.
Se mantiene el recorrido anterior en Avanzado, sin eliminar sus controles.

## Escenarios de aceptación
1. Al abrir, la pantalla principal muestra Demo, tres escenarios y ningún formulario técnico.
   No aparecen Renfe, referencias opacas ni Próximamente en este recorrido.
2. Sin READ_CALENDAR se explica el acceso y se solicita desde UI. Denegar no bloquea
   la app ni se convierte en un calendario ficticio.
3. Con permiso, se consulta la primera ocurrencia con hora de mañana, en la zona
   del dispositivo. Se excluyen todo-el-día, cancelados y calendarios no visibles.
   Sin reuniones se indica explícitamente; no se inventa ninguna.
4. El trayecto sintético dura 35 minutos y sale 35 minutos antes de esa reunión:
   puntual llega al inicio; retrasado suma 15 minutos; desconocido no compara.
   Esta adaptación deliberada se explica en pantalla. No es horario ni predicción Renfe.
5. Un interruptor explícito, apagado inicialmente, permite al agente consultar el
   horario de la próxima reunión de mañana y el resultado. Es autorización continua
   para ese alcance, no una referencia a una ocurrencia fija. Puede desactivarse.
6. La nueva función no recibe parámetros, relee calendario y configuración en cada
   llamada y comparte solamente tiempos, resultado y ruta sintética, nunca títulos,
   identificadores, notas o asistentes. Consentimiento y permiso se comprueban de nuevo
   antes de devolver datos. Sin ellos se deniega incluso aunque UI no esté abierta.
7. El escenario persiste y es compartido por UI/servicio. Mañana se calcula por fecha
   local y Clock, también al reabrir la app. Cancelación y errores no filtran datos.
8. El agente puede preparar un borrador para Ana POC. No se añade mensajería ni se
   autoriza un envío nuevo; permanece la confirmación del destinatario y texto exactos.

## Fuera de alcance
Predicción operativa, nuevos módulos, garantía de comportamiento LLM, Nano, cambios
en ChatApp/Testing Agent y eliminación de las APIs anteriores.
