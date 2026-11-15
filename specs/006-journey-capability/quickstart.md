# Probar UC-006
Usar Android Studio/Gradle JDK integrado. Compilar e instalar APK. En la app,
configurar trayecto y autorizar Compartir trayecto con asistentes. Desactivado
por defecto y separado de calendario. En API<36/servicio ausente no se puede activar.

```powershell
.\gradlew.bat :domain:test :data:test :platform:testDebugUnitTest :presentation:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:installDebug
.\gradlew.bat :appfunctions:connectedDebugAndroidTest
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


## Cliente SDK probado
```powershell
python scripts/appfunctions-smoke.py discover
python scripts/appfunctions-smoke.py state
python scripts/appfunctions-smoke.py query --departure 2026-11-04T08:00 --zone UTC
python scripts/appfunctions-smoke.py query --departure 2026-11-04T08:00 --zone UTC --scenario DEMO_DELAYED
python scripts/appfunctions-smoke.py query --departure 2026-11-04T08:00 --zone UTC --origin-id 18000 --destination-id 17000
```
Usar fecha futura si se repite otro día en REFERENCE. La demo conserva fecha fija.
El script espera indexación hasta10s, descubre ID, usa proxies nativos (departure
objeto year/month/dayOfMonth/hour/minute/second/nanoOfSecond; zone objeto zoneID)
y escapa JSON para la segunda interpretación de adb shell. Retorna error no cero
para fallos impresos por el comando del sistema. No cambia consentimiento ni habilitación.
Otros SDK paths: --adb ruta. No hacen falta API keys ni permiso de calendario.

Para muerte de proceso debug sin semántica force-stop: cerrar con HOME, obtener
PID mediante adb shell pidof com.cercafunctions.app, y matar ese PID con
adb shell run-as com.cercafunctions.app kill -9 PID. Comprobar pid vacío y consultar
de nuevo sin abrir Activity. Solo APK debuggable propio. Ver registro cold-process.json.
No usar los tests directos del servicio para atribuirse esa cobertura IPC.

AGP/UTP puede desinstalar el APK al terminar las instrumentadas. Reinstalar y
configurar/autorizar desde la app antes del smoke SDK. Para repetir la negativa
con proveedor habilitado, autorizar tras installDebug y antes del testhost de
appfunctions. Al terminar, revocar compartir desde la UI.
