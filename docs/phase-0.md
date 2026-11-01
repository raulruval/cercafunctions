# Snapshot · fase 0: repositorio base

Fecha: 1 de noviembre de 2026. Estado de producto: base inicial, revisión técnica
pendiente antes de iniciar SDD.

## Alcance construido

- Seis módulos y dirección de dependencias documentada.
- Primera pantalla Compose/Material 3, Preview, ViewModel/StateFlow y Hilt.
- Modelo, contrato y caso de uso JVM para leer la configuración habitual.
- Repositorio en memoria vacío, sin persistencia ni rutas inventadas.
- Tests JVM y configuración Gradle con wrapper y versiones fijadas.
- Investigación Rocket-Beer, skill y ejemplos oficiales, límites de Renfe,
  requisitos de emulador/Testing Agent y guía de aprendizaje desde Flutter.
- Fronteras CalendarContract y AppFunctions preparadas, sin capacidades publicadas.

## Fuera de esta entrega

No hay formulario de configuración, consultas Renfe, permisos de calendario,
cálculo de llegada, fixtures de retraso, AppFunctions registradas, ChatApp
instalada, envío de mensajes ni pruebas de orquestación. No se ha iniciado
SDD ni dividido el desarrollo futuro en especificaciones o tareas ejecutables.

## Verificación

- `:core-domain:test`: 7 tests, 0 fallos, 0 errores, 0 omitidos.
- `:app:assembleDebug`: APK generado correctamente.
- `:app:lintDebug`: 0 errores y 9 avisos de actualización de dependencias, que
  se conservan visibles; las versiones se mantienen fijadas para esta base.
- APK instalado en `emulator-5554`, AVD Pixel_10_Pro, API 37.2.
- `am start -W`: Status ok, arranque en frío y proceso activo.
- Primera pantalla revisada visualmente mediante captura: estado vacío correcto,
  textos legibles y sin desbordamientos en ese dispositivo.
- `adb shell cmd app_function help`: servicio disponible; anuncia listado,
  ejecución y gestión de estado. Esto no prueba registro de funciones de Copilot.
- No se ha ejecutado Testing Agent, ChatApp ni una cadena LLM.

La primera pasada de lint detectó un escape ausente en `local.properties`;
se corrigió con `C\:/...`. La configuración de backups se hizo explícita para
API anteriores y posteriores a 31. No se añadieron baselines ni supresiones.

Snapshot Git: commit inicial y etiqueta local `phase-0-base`.
Consultar `git show phase-0-base` para inspeccionar exactamente esta entrega.
No se publica ni se hace push a GitHub en esta fase.
La máquina no tenía identidad Git configurada. Este commit usa el autor técnico
`Codex <codex@local.invalid>` mediante opciones de esa invocación, sin modificar
la configuración local o global del usuario.

## Punto de revisión

Revisar docs/architecture.md y docs/feasibility.md. La siguiente conversación
podrá convertir el alcance aprobado en casos de uso y tareas SDD progresivas,
con criterios de aceptación y snapshots. Esta fase termina en esa revisión.
