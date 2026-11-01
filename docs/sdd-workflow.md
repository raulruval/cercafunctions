# Método SDD y ciclo de trabajo asistido por IA

## Qué significa cada término aquí

SDD usa una especificación versionada como contrato del cambio: qué resultado
esperamos, qué queda fuera y cómo sabremos que funciona. Un ciclo de trabajo
con IA añade contexto persistente, planificación, construcción, revisión y
evidencia. Un caso de uso describe un resultado; una user story expresa su
valor; un criterio de aceptación lo vuelve comprobable; una tarea organiza
trabajo técnico. Un caso puede necesitar varias clases de caso de uso Kotlin.

No hay una única plantilla universal obligatoria de SDD ni un estándar Android
para todo ese ciclo. Usamos un toolkit público concreto y convenciones locales
pequeñas, diferenciando lo oficial de lo que elegimos para esta POC.

## Referencias oficiales contrastadas el 2026-11-01

| Fuente | Papel en el proyecto |
| --- | --- |
| [Android Skills](https://github.com/android/skills) | Instrucciones técnicas de Android. El catálogo consultado no contiene una skill general de SDD; AppFunctions sigue siendo nuestra referencia especializada |
| [Conductor, presentado por Google](https://developers.googleblog.com/evolving-spec-driven-development-conductor-now-supports-antigravity/) | Referencia Google para contexto persistente y spec/plan por incremento; su README actual documenta Antigravity y Claude Code |
| [GitHub Spec Kit](https://github.com/github/spec-kit/tree/v1.2.5) | Herramienta elegida: versión 1.2.5, integración Codex, plantilla de spec y skills oficiales |
| [Quickstart Spec Kit](https://github.github.com/spec-kit/quickstart.html) | Flujo constitución → specify → plan → tasks → implement → converge, con aclaración/análisis cuando aportan valor |
| [AI-DLC de AWS Labs](https://github.com/awslabs/aidlc-workflows) | Referencia de ciclo de vida y trazabilidad. No es de Android/Google; no se instala su motor en esta POC |

Esta es una adopción ligera basada en Spec Kit. No es una certificación ni una
instalación completa de AI-DLC. Su motor actual documenta más fases y etapas;
no presentamos nuestra simplificación como una reproducción exacta.

## Configuración efectuada

Se instaló Specify CLI 1.2.5 desde el tag oficial, commit
`4572a67fe0e72a6837e2d7bb0fda875bc41482e5`, en un entorno Python aislado del área
de trabajo de esta sesión. No se añade como dependencia de Android ni al PATH global.
Dentro del repositorio se ejecutó:

```powershell
specify init --here --force --integration codex --integration-options='--events false' --script ps --ignore-agent-tools --non-interactive
```

El resultado son `.specify/` y `.agents/skills/speckit-*/SKILL.md` oficiales.
No se modifican sus plantillas para fingir un framework propio. No se activan
eventos automáticos, extensiones de Git/GitHub, servidor MCP o ejecución LLM autónoma.
Las skills pueden leer y escribir documentos desde el chat sin servidor adicional.

Para disponer de CLI reproducible en otra sesión/máquina, opcionalmente:

```powershell
python -m venv .venv-sdd
.\.venv-sdd\Scripts\python.exe -m pip install -r tools/requirements-sdd.txt
.\.venv-sdd\Scripts\specify.exe --version
```

No repetir `init --force` en un proyecto configurado: podría sobrescribir archivos
administrados. Actualizar herramientas es un cambio separado y revisable.

## Un ciclo por incremento

1. **Definir**. Leer constitución, spec activa y estado de Git. Mantener cambios
   locales ajenos fuera del incremento. Capturar supuestos en `spec.md`.
2. **Especificar**. Resultado observable, escenarios Dado/Cuando/Entonces, errores,
   límites y criterios medibles. La spec expresa comportamiento, no clases Android.
3. **Planificar**. Seleccionar diseño mínimo y pruebas con rutas concretas, usando
   la arquitectura existente. Registrar decisiones pequeñas en `plan.md`/`research.md`.
4. **Desglosar**. `tasks.md` ordena trabajo por historia, dependencias y archivos;
   cada tarea empieza pendiente. En UC-001 hay 12, no un backlog técnico de toda la POC.
5. **Revisar y construir**. Una petición explícita de implementación del caso
   revisado inicia el trabajo. Continuar sus tareas sin pedir permisos repetidos.
6. **Verificar y reconciliar**. Comprobar aceptación con pruebas proporcionales;
   contrastar código, spec y tareas. Si cambia el requisito, actualizar primero
   o junto al código y explicar el efecto. Cerrar solo con evidencia.

La sesión actual termina después de preparar documentos. No autoriza código
de funcionalidades. La documentación del resto de casos es Draft y no requiere
que el usuario tome ahora todas las decisiones de la POC.

## Skills y uso desde el chat

```text
$speckit-specify <descripción de un nuevo incremento>
$speckit-clarify <ambigüedades del caso activo>
$speckit-plan <contexto técnico del caso activo>
$speckit-tasks
$speckit-analyze
$speckit-implement
$speckit-converge
```

Son instrucciones del agente, no comandos PowerShell. En la integración Codex
se usa `$speckit-...`; no prometer comandos `/specify` de versiones anteriores.
También se puede pedir cada operación en lenguaje natural.

Abrir Codex con `C:\dev\cercafunctions` como proyecto para descubrir sus skills.
Esta conversación comenzó fuera de ese directorio: aquí se han leído las skills
directamente, sin asumir que aparezcan automáticamente en el selector del chat.

## La característica activa no es la rama Git

[La integración oficial](https://github.github.com/spec-kit/integrations/codex.html)
usa `.specify/feature.json` como referencia local. Está ignorado por la propia
herramienta. La copia actual apunta a `specs/001-habitual-journey`.
Al clonar o cambiar de incremento, establecerlo explícitamente:

```powershell
$env:SPECIFY_FEATURE_DIRECTORY = 'specs/001-habitual-journey'
```

También puede escribirse el puntero JSON local con codificación UTF-8 sin BOM.
`git switch` por sí solo
no selecciona la spec activa. No ejecutar specify para UC-001 ya existente
sin indicar que se trata de revisarlo, porque puede crear un nuevo incremento.

## Snapshots y seguimiento

Rama de adopción: `chore/sdd-foundation`; snapshot inicial: `sdd-inception-v1`.
Las siguientes ramas podrán ser `feat/001-habitual-journey`, etc., basadas en
la adopción revisada, no en la etiqueta antigua que carece de Spec Kit.
Git no se configura globalmente ni se publica sin una instrucción posterior.

`specs/README.md` es el tablero de estado. `tasks.md` marca avance por tarea.
Al entregar se añade `verification.md` al incremento con: commit, comandos,
resultados, escenarios manuales, evidencia y limitaciones. No se necesita
un dashboard o una automatización periódica para esta POC.

Mantener `spec.md` como contrato vivo. El historial Git y las etiquetas preservan
los snapshots anteriores. Si cambia un requisito, se revisan plan, tareas y
pruebas afectadas. Preferir un commit por cambio coherente; no uno por cada
línea ni una reescritura de historial. Cada entrega verificada puede llevar etiqueta.

## Qué verificar según el cambio

Documentos: enlaces, IDs, dependencias y coherencia spec/plan/tareas. Kotlin puro:
tests del módulo. Persistencia: tests de archivo/recreación y fallos. Pantalla:
comprobación visual y prueba de interacción donde proteja el comportamiento.
Permisos/CalendarContract/AppFunctions: pruebas instrumentadas y de contrato
cuando se construyan; el flujo determinista se registra separado del LLM.

Comprobación Android base cuando corresponda:

```powershell
.\gradlew.bat :core-domain:test :app:assembleDebug :app:lintDebug
```

No generar métricas de cobertura o logs de datos privados como sustituto de
criterios de aceptación. Un checklist de calidad documental no prueba el producto.
