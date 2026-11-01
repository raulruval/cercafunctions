# Empieza aquí

El proyecto compila y abre la pantalla base. Ahora definimos comportamiento antes
de programar cada incremento. No necesitas leer toda la documentación de fase 0.

1. Lee el [mapa de ocho casos de uso](specs/README.md).
2. Revisa la [especificación UC-001](specs/001-habitual-journey/spec.md).
3. Su [plan](specs/001-habitual-journey/plan.md) y [tareas](specs/001-habitual-journey/tasks.md)
   enseñan cómo se convierte ese contrato en un incremento Android.

**Estado**: ocho specs Draft; UC-001 tiene plan y tareas propuestos, sin código nuevo.
La revisión humana de los supuestos sigue pendiente. Después podremos construir UC-001.

## Las tres decisiones pequeñas del primer incremento

- Un único trayecto, editable; nombres de origen/destino escritos por el usuario.
- Duración de referencia entre 1 y 1440 minutos; todavía sin horario de salida guardado.
- Guardado local, sin red, calendario ni agentes en este incremento.

Son propuestas explícitas de alcance de la POC, no requisitos impuestos por Android.
La hora de salida se confirma al consultar UC-002. Identificar estaciones reales es UC-003.

## Trabajar conmigo

Puedes usar lenguaje natural, por ejemplo:

- «Revisa conmigo los supuestos de UC-001».
- «Ajusta UC-001 para que la duración máxima sea otra».
- «Implementa UC-001 conforme a la especificación y su plan» cuando lo hayas revisado.
- «Muéstrame el estado y la evidencia de UC-001».

También hay skills oficiales de Spec Kit en `.agents/skills`, utilizables al abrir
Codex en este repositorio: `$speckit-specify`, `$speckit-plan`, `$speckit-tasks`,
`$speckit-implement` y `$speckit-converge`. Ver el [método y fuentes](docs/sdd-workflow.md).
