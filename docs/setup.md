# Configuración y verificación

## Toolchain fijada

Gradle 9.6.1, AGP 9.3.3, Kotlin/Compose compiler 2.4.10, KSP 2.3.10,
Hilt 2.60.1, Compose BOM 2026.06.01 y AppFunctions 1.0.0-beta01.
El catálogo contiene las versiones exactas. El compilador KSP de AppFunctions
procesa las tres capacidades de Copilot; servicio y metadatos están implementados.

AGP requiere al menos JDK 17; el bytecode del proyecto se fija a 17. En esta
máquina se usa el JBR 25 de Android Studio con Gradle 9.6.1. Instalar SDK Platform
37 y Build Tools solicitados por AGP; no se incluye un SDK en el repositorio.
Crear `local.properties` con `sdk.dir=C\:/ruta/al/Android/Sdk` si Studio no lo genera.
Ese archivo queda ignorado por Git.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :domain:test :app:assembleDebug :app:lintDebug --console=plain
```

En macOS/Linux usar `./gradlew` y configurar el JDK/SDK locales. El wrapper
descarga Gradle y comprueba `distributionSha256Sum`; no hace falta Gradle global.
La primera ejecución necesita red para dependencias públicas y herramientas SDK.

## Primera pantalla

Crear o iniciar un AVD API 26+ y ejecutar `app` desde Android Studio, o:

```powershell
.\gradlew.bat :app:installDebug
adb shell am start -n com.cercafunctions.app/.MainActivity
```

Resultado: pantalla Demo de Cercanías Copilot, escenarios Puntual/+15 min/Sin datos
y acceso al calendario real desde un botón explícito. El recorrido inicial no consulta
Renfe; Avanzado conserva el trayecto de referencia y la selección manual.
APK: `app/build/outputs/apk/debug/app-debug.apk`.

Tests JVM: `domain/build/reports/tests/test/index.html`.
Lint: `app/build/reports/lint-results-debug.html`.
Las suites cubren dominio, persistencia, ViewModels, cancelación, calendario,
autorización y frontera AppFunctions. La evidencia de UC-009 incluye consultas IPC
reales y 72 pruebas instrumentadas; no se confunde con una garantía de orquestación LLM.

## Laboratorio AppFunctions

Clonar el ejemplo oficial en otra carpeta, sin copiarlo como módulos del APK.
Para reproducibilidad usar inicialmente el commit de docs/research.md.
En Windows activar rutas largas para ese clon con `git -c core.longpaths=true clone`.
Los scripts oficiales se ejecutan desde Git Bash/WSL con JDK, SDK y adb accesibles;
preferir un único entorno y un único dispositivo ADB para evitar ambigüedades.

Comprobación inicial de la imagen:

```powershell
adb devices -l
adb shell getprop ro.build.version.sdk
adb shell cmd app_function help
adb shell cmd app_function list-app-functions
```

API 36+ es condición necesaria; comprobar el servicio y los comandos de esa
imagen antes de seguir. Si falta `app_function`, escoger otra imagen compatible.
Los nombres e IDs de funciones deben obtenerse del listado, no del README antiguo.

Desde `android/appfunctions/agent`, el arranque documentado es:

```bash
./run_privileged.sh --build --serial <serial>
```

El script compila e instala el APK oficial y delega en
`startAppFunctionTestingAgent`. En SDK 37+ gestiona la allowlist y ejecuta
`ShellIdentityInstrumentation`. La instrumentación debe permanecer viva.
No se ejecuta ni se concede esta identidad en la fase 0.

Primero validar descubrimiento e invocación manual con parámetros fijos. Después
intentar el flujo LLM con proveedor configurado y consentimiento para los datos
que salgan del dispositivo. No guardar ni pasar claves en documentos versionados.
Antes de enviar un mensaje debe existir confirmación explícita; ver los límites
de la API actual de ChatApp en docs/feasibility.md.

## Entorno encontrado en esta sesión

- Android Studio instalado con JBR 25.0.3.
- SDK Platform `android-37.0`, Build Tools 36.0.0 y 37.0.0.
- AVD `Pixel_10_Pro`, imagen API 37.2 Google APIs Play Store x86_64 de 16 KB.
- Al iniciar no había dispositivos conectados por ADB.

La evidencia final de compilación, ejecución y disponibilidad del servicio se
registra en docs/phase-0.md; la presencia de una imagen no demuestra por sí sola
que el Testing Agent o la orquestación funcionen.
