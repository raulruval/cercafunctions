# Probar UC-006
Usar Android Studio/Gradle JDK integrado. Compilar e instalar APK. En la app,
configurar trayecto y autorizar Compartir trayecto con asistentes. Desactivado
por defecto y separado de calendario. En API<36/servicio ausente no se puede activar.

```powershell
.\gradlew.bat :core-domain:test :core-data:test :core-platform:testDebugUnitTest :core-presentation:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:installDebug
.\gradlew.bat :core-appfunctions:connectedDebugAndroidTest
adb shell cmd app_function help
adb shell cmd app_function list-app-functions --package com.cercafunctions.app
```
Leer metadata e ID descubierto antes de ejecutar. Los tipos java.time tienen el
formato nativo generado por Jetpack: consultar evidencia/guion SDK del caso para
payloads exactos y escaping Windows. No pasar fechas locales sin zona.
Comparar llegada/referencia con UI; demos usan todo su fixture fijo2026-11-02.
Sin IDs no hay consulta de red; con18000/17000 usa contexto Renfe cualitativo.
Pruebas instrumentadas secuenciales: library testhost con UID distinto necesita
APK app instalado para probar denegación real. Shell positivo es privilegiado.
Revocar compartir y comprobar error; deshabilitar con set-enabled debe impedir
ejecución. Al terminar las pruebas sintéticas se deja compartir desactivado.
