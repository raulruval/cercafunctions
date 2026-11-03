# Cercanías Copilot · Cercafunctions

Empieza por [START_HERE.md](START_HERE.md): mapa de casos, primer incremento y método SDD.

Base Android nativa para investigar una arquitectura que proporcione capacidades
a agentes, inspirada en Rocket-Beer. UC-001–007 implementados y verificados; UC-008 sigue Draft.

## Qué ejecuta esta base

Pantalla Compose + Material 3 para configurar, guardar y editar un trayecto habitual.
ViewModel/StateFlow ejecuta los casos de uso Kotlin puros y Hilt enlaza Preferences
DataStore. La configuración se recupera tras reiniciar el proceso. Los campos se
validan antes de escribir y los errores no se convierten en configuración vacía.

La duración es una referencia del usuario, no una predicción ferroviaria. Ver
[pruebas y evidencia de UC-001](specs/001-habitual-journey/verification.md).

El panel de llegada calcula una referencia para fecha/hora/zona confirmadas.
Incluye tres demos con reloj y datos fijos: normal, +15 y desconocido; nunca se
presentan como operación real. Pruebas cubren medianoche y cambios de hora.
Ver [guía UC-002](specs/002-journey-estimate/quickstart.md) y
[evidencia](specs/002-journey-estimate/verification.md).

UC-003 consulta avisos públicos de Renfe por HTTPS y filtra localmente por dos
estaciones seleccionadas expresamente del catálogo. Distingue feed caducado,
actualidad desconocida y fuente no disponible. Cobertura solo de extremos;
ningún aviso añade minutos inventados. Ver [guía](specs/003-renfe-context/quickstart.md)
y [evidencia](specs/003-renfe-context/verification.md).

UC-004 permite consultar un día del calendario Android con permiso contextual,
elegir una ocurrencia y revalidarla. No modifica ni persiste agenda; distingue
denegación, revocación, vacío y fallo de proveedor. No divulga datos a agentes.
Ver [ejecución y demo opcional sin cuenta](specs/004-calendar-selection/quickstart.md)
y [verificación](specs/004-calendar-selection/verification.md).

UC-005 compara la llegada con el compromiso revalidado mediante AssessArrival,
caso de dominio compartible con futuras capacidades. Distingue margen, retraso
e indeterminado; preserva fuente e incertidumbre y retira resultados obsoletos.
Sin margen extra ni desplazamiento estación–reunión, explícitos en pantalla.
Ver [prueba guiada](specs/005-arrival-assessment/quickstart.md) y
[verificación](specs/005-arrival-assessment/verification.md).

UC-006 publica queryJourney mediante el servicio AppFunctions generado por KSP.
Opt-in de compartir trayecto desde UI, desactivado por defecto; consumidores
autorizados por Android, sin calendario/contactos ni mensajes. Funciona con
proceso frío y sin Activity usando los mismos casos de dominio. Ver
[guía SDK](specs/006-journey-capability/quickstart.md) y
[verificación](specs/006-journey-capability/verification.md).

UC-007 publica evaluación mínima de una ocurrencia autorizada mediante referencia
temporal. Límite30min/comienzo, revocación y validación de versión/permiso/reloj;
se guarda hash y selección mínima, sin rawtoken ni títulos. Capacidad independiente
del opt-in de trayecto, sin agenda completa ni mensajes. Ver
[guía](specs/007-assessment-capability/quickstart.md) y
[pruebas](specs/007-assessment-capability/verification.md).

## Módulos

| Módulo | Responsabilidad |
| --- | --- |
| `app` | Application, Activity y ensamblado Hilt |
| `core-presentation` | Compose, tema, estado y ViewModel |
| `core-domain` | Modelos, contratos y casos de uso Kotlin/JVM |
| `core-data` | Repositorios JVM, HTTP/parser Renfe, catálogo de estaciones y fixtures |
| `core-platform` | Persistencia DataStore y lectura CalendarContract con cancelación |
| `core-appfunctions` | Servicio AppFunctions, DTO, KSP, permisos y mapeo a dominio |

## Ejecutar

Abrir esta carpeta en Android Studio. Seleccionar su JDK integrado como Gradle JDK.
Instalar Android SDK Platform 37 y Build Tools 36.0.0 o los solicitados por AGP.
La UI admite API 26+; para el laboratorio AppFunctions usar API 36+ (preferido 37+).
Ver [configuración y pruebas](docs/setup.md).

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :core-domain:test :core-data:test :core-platform:testDebugUnitTest :core-presentation:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :app:installDebug
```

El wrapper descarga Gradle 9.6.1 y verifica su SHA-256. Las versiones están fijadas
en `gradle/libs.versions.toml`. AGP 9 usa Kotlin integrado en los módulos Android;
`org.jetbrains.kotlin.jvm` se aplica solo a los módulos puros.

## Documentación para revisar

- [Análisis Rocket-Beer y fuentes fijadas por commit](docs/research.md).
- [Arquitectura y diseño de core-appfunctions](docs/architecture.md).
- [Viabilidad, privacidad y límites de la interoperabilidad](docs/feasibility.md).
- [Guía Android desde Flutter](docs/android-for-flutter.md).
- [Snapshot de esta fase y validación](docs/phase-0.md).

ChatApp y Testing Agent siguen en UC-008 de `specs/`. UC-001–007 son incrementos funcionales
con Spec Kit y evidencia versionada; no hay backend ni predicción de puntualidad real.
