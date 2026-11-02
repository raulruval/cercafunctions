# Cercanías Copilot · Cercafunctions

Empieza por [START_HERE.md](START_HERE.md): mapa de casos, primer incremento y método SDD.

Base Android nativa para investigar una arquitectura que proporcione capacidades
a agentes, inspirada en Rocket-Beer. UC-001 implementado y verificado; siete casos siguientes en Draft.

## Qué ejecuta esta base

Pantalla Compose + Material 3 para configurar, guardar y editar un trayecto habitual.
ViewModel/StateFlow ejecuta los casos de uso Kotlin puros y Hilt enlaza Preferences
DataStore. La configuración se recupera tras reiniciar el proceso. Los campos se
validan antes de escribir y los errores no se convierten en configuración vacía.

La duración es una referencia del usuario, no una predicción ferroviaria. Ver
[pruebas y evidencia de UC-001](specs/001-habitual-journey/verification.md).

## Módulos

| Módulo | Responsabilidad |
| --- | --- |
| `app` | Application, Activity y ensamblado Hilt |
| `core-presentation` | Compose, tema, estado y ViewModel |
| `core-domain` | Modelos, contratos y casos de uso Kotlin/JVM |
| `core-data` | Repositorios JVM; implementación en memoria para fixtures |
| `core-platform` | Persistencia DataStore; futura integración CalendarContract |
| `core-appfunctions` | Frontera Android de capacidades; biblioteca preparada, sin servicio publicado |

## Ejecutar

Abrir esta carpeta en Android Studio. Seleccionar su JDK integrado como Gradle JDK.
Instalar Android SDK Platform 37 y Build Tools 36.0.0 o los solicitados por AGP.
La UI admite API 26+; para el laboratorio AppFunctions usar API 36+ (preferido 37+).
Ver [configuración y pruebas](docs/setup.md).

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :core-domain:test :core-platform:testDebugUnitTest :core-presentation:testDebugUnitTest :app:assembleDebug :app:lintDebug
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

Calendario, Renfe, cálculo de puntualidad, publicación AppFunctions, ChatApp y
Testing Agent se abordan mediante los casos de uso de `specs/`. Se ha configurado Spec Kit y
creado sus especificaciones iniciales. UC-001 es el primer incremento funcional; no hay backend.
