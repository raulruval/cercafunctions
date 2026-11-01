# Cercanías Copilot · Cercafunctions

Empieza por [START_HERE.md](START_HERE.md): mapa de casos, primer incremento y método SDD.

Base Android nativa para investigar una arquitectura que proporcione capacidades
a agentes, inspirada en Rocket-Beer. Fase 0 ejecutable y SDD configurado; casos de uso en Draft.

## Qué ejecuta esta base

Primera pantalla Compose + Material 3, ViewModel, StateFlow y Hilt. Consulta un
caso de uso Kotlin puro y presenta el estado «sin trayecto configurado».
El repositorio inicial está vacío y vive en memoria; no hay datos ferroviarios simulados.

## Módulos

| Módulo | Responsabilidad |
| --- | --- |
| `app` | Application, Activity y ensamblado Hilt |
| `core-presentation` | Compose, tema, estado y ViewModel |
| `core-domain` | Modelos, contratos y casos de uso Kotlin/JVM |
| `core-data` | Implementaciones de repositorios Kotlin/JVM |
| `core-platform` | Frontera Android reservada para CalendarContract |
| `core-appfunctions` | Frontera Android de capacidades; biblioteca preparada, sin servicio publicado |

## Ejecutar

Abrir esta carpeta en Android Studio. Seleccionar su JDK integrado como Gradle JDK.
Instalar Android SDK Platform 37 y Build Tools 36.0.0 o los solicitados por AGP.
La UI admite API 26+; para el laboratorio AppFunctions usar API 36+ (preferido 37+).
Ver [configuración y pruebas](docs/setup.md).

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :core-domain:test :app:assembleDebug :app:lintDebug
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
creado sus especificaciones iniciales; todavía no hay implementación de funcionalidades ni backend.
