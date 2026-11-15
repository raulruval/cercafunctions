# Cercanías Copilot · Android AppFunctions POC

POC Android nativa terminada como demostración de capacidades para agentes.
La UI y las AppFunctions ejecutan los mismos casos de uso de dominio, sin que
el agente navegue por las pantallas. Kotlin, Jetpack Compose, Material 3,
ViewModel/StateFlow, Coroutines y Hilt; patrones inspirados en Rocket-Beer.

## Demo principal

La pantalla inicial combina **calendario real del dispositivo** con **un trayecto
sintético**, para repetir el resultado sin depender de incidencias de Renfe:

1. Permite leer el calendario. Copilot elige la primera reunión con hora de **mañana**,
   en la zona del dispositivo. Si no hay ninguna, crea una desde tu app de calendario.
2. Elige **Puntual**, **+15 min** o **Sin datos**. El trayecto Atocha (demo) → Chamartín
   (demo) dura 35 minutos; la salida se ajusta a 35 minutos antes de la reunión.
3. Activa **Permitir consulta del agente**. Comparte únicamente horarios y resultado,
   sin títulos, notas, asistentes o identificadores del calendario. Persiste hasta revocar.
4. En AppFunctions Testing Agent → **Debug**, ejecuta **evaluateDemoJourney** sin parámetros.
   Esta prueba no requiere modelo ni clave API.
5. Para probar lenguaje natural, configura tu clave **Gemini API** en el agente,
   fuera de cualquier grabación, y utiliza **Agent Demo**.

> Consulta mi trayecto de Cercanías de mañana y comprueba si llegaré a tiempo a mi
> próxima reunión. Si no llego, prepara un mensaje para Ana POC pidiéndole que la retrase.

**Resultados:** Puntual llega al comienzo (margen 0); +15 min llega tarde (−15);
Sin datos devuelve indeterminado. No es una predicción ferroviaria ni un horario de
Renfe; no añade traslado desde la estación hasta la reunión ni margen de seguridad.
No se modifica el calendario. [Guía breve y diagnóstico del agente](docs/demo-simple.md).

## Capacidades publicadas

| AppFunction | Uso | Autorización dentro de Copilot |
| --- | --- | --- |
| `evaluateDemoJourney()` | Demo principal: tren sintético y siguiente reunión real de mañana. Sin parámetros. | READ_CALENDAR e interruptor de consulta del agente. |
| `queryJourney(...)` | Avanzado: llegada de referencia, escenarios y contexto ferroviario. | Consentimiento específico para compartir el trayecto. |
| `evaluateAuthorizedCommitment(...)` | Avanzado: evaluación mínima de una ocurrencia elegida. | Referencia temporal emitida desde la UI y revalidada. |

El servicio Android generado por KSP exige consumidores autorizados por Android;
publicar o descubrir una función no concede acceso a los datos. El servicio no
solicita permisos ni depende de un ViewModel. [Adaptador AppFunctions](appfunctions/README.md).

**Avanzado · referencia y calendario manual** conserva la configuración y persistencia
del trayecto habitual, fecha/hora/zona explícitas, consulta de avisos Renfe, selección
manual de reuniones y autorizaciones acotadas. Sus escenarios completos usan mañana
a las 08:00 en Europe/Madrid; la demo principal ajusta la salida al compromiso real.
Las alertas cualitativas nunca se convierten en minutos de retraso inventados.

## Tres aplicaciones en el laboratorio

- **Cercanías Copilot:** este repositorio, proveedor de la evaluación.
- **AppFunctions Testing Agent:** consumidor oficial de pruebas, con Debug manual
  y Agent Demo mediante Gemini API en la revisión utilizada.
- **ChatApp:** proveedor oficial de ejemplo para buscar contactos y preparar/enviar
  mensajes. Usa el contacto ficticio Ana POC; el mensaje queda en memoria local,
  no se entrega por red. Copilot no implementa mensajería propia.

Los ejemplos oficiales se instalan por separado desde
[android/appfunctions](https://github.com/android/appfunctions); no son módulos de este APK.
Después de reiniciar el emulador, inicia el Testing Agent con los privilegios del laboratorio:

```powershell
.\scripts\start-testing-agent.ps1 -Serial emulator-5554
```

El script es el equivalente Windows del lanzador oficial. No ejecutes pruebas
instrumentadas o UIAutomator a la vez que esa sesión; para detenerla añade `-Stop`.
La versión fijada del agente integra Gemini API: otro proveedor o un LLM local
requieren adaptar el cliente, no solo introducir otra clave.

## Módulos actuales

| Módulo Gradle | Responsabilidad |
| --- | --- |
| `app` | APK, Application, Activity y composición Hilt. |
| `domain` | Modelos, contratos y casos de uso Kotlin/JVM sin Android. |
| `data` | Repositorios JVM, HTTP/parser Renfe, catálogo y fixtures. |
| `platform` | CalendarContract, permisos y persistencia DataStore. |
| `presentation` | Compose, tema, ViewModels y StateFlow. |
| `appfunctions` | Adaptador Android, DTO, KDoc, metadatos y servicio generado. |

Organización por capas con paquetes funcionales internos. Los nombres coinciden con
`settings.gradle.kts`. [Decisión de nombres y comparación con feature-first](docs/module-organization.md).
Los documentos de incrementos antiguos conservan los nombres usados en sus snapshots.

## Compilar y ejecutar

Abre el proyecto en Android Studio y usa su JDK integrado para Gradle. Instala
SDK Platform 37 y los Build Tools solicitados. La UI admite API26+; el laboratorio
AppFunctions requiere API36+ y se verificó en un emulador API37.2.
`local.properties`, claves API y configuración del equipo no se versionan.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :domain:test :data:test :platform:testDebugUnitTest :presentation:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :app:installDebug
```

Versiones fijadas en `gradle/libs.versions.toml`; wrapper Gradle con SHA-256.
[Configuración y pruebas](docs/setup.md).

## Estado y límites de la POC

**UC-001–009 implementados**, con especificación, plan, tareas y evidencia en
[el tablero SDD](specs/README.md). La validación de UC-009 registró **187 tests JVM**,
**72 tests Android**, APK/lint y consultas IPC reales de los tres escenarios,
revocación y arranque sin Activity. [Evidencia](specs/009-simple-demo/verification.md).

UC-008 verificó un flujo determinista entre aplicaciones con confirmación exacta
antes de un mensaje local de prueba. El ensayo Gemini inspeccionado después verificó
la evaluación y la búsqueda del contacto; luego generó un nombre de función inexistente.
Por tanto, el éxito de las herramientas no garantiza que el LLM orqueste siempre el
flujo completo. No se confía en un prompt como barrera técnica antes de enviar.
[Flujo determinista y guía de interoperabilidad](specs/008-cross-app-demo/quickstart.md).

Sin backend propio, mapas, compra de billetes ni predicción operativa. La POC conserva
el alcance educativo; no se presenta como producto listo para producción.

## Documentación

- [Empieza aquí](START_HERE.md) · [Demo sencilla](docs/demo-simple.md).
- [Arquitectura y AppFunctions](docs/architecture.md) · [Android desde Flutter](docs/android-for-flutter.md).
- [Análisis de Rocket-Beer](docs/research.md) · [Conclusiones Agent-Ready](docs/agent-ready-conclusions.md).
- [Método SDD](docs/sdd-workflow.md) · [Casos y evidencias](specs/README.md).
- [Ejemplos oficiales Google](https://github.com/android/appfunctions) · [Skill oficial AppFunctions](https://github.com/android/skills/blob/main/device-ai/appfunctions/SKILL.md).
