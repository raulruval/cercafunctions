# Ejecutar UC-004
Ejecutar app en Android Studio. El panel Calendario funciona sin trayecto guardado.
Elegir fecha y zona, pulsar Consultar calendario y permitir lectura. Elegir una
ocurrencia de la lista; no hay selección automática ni con una sola. Revalidar
selección vuelve a leer proveedor y retira una ocurrencia borrada/movida/cancelada.
Solo se consultan eventos con hora que comienzan ese día en calendarios visibles.

Rechazar permiso muestra explicación y Ajustes; no consulta. Conceder en Ajustes
no lee automáticamente: volver y pulsar Consultar. Revocar desde Ajustes retira
datos al volver (Android puede matar el proceso). No se solicita WRITE_CALENDAR.
Recrear Activity conserva selección; reiniciar proceso no. Cambiar fecha/zona la retira.

```powershell
.\gradlew.bat :domain:test :data:test :platform:testDebugUnitTest :presentation:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :platform:connectedDebugAndroidTest
.\gradlew.bat :presentation:connectedDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
```
Instrumentadas secuenciales. Tests crean calendarios LOCAL únicos con eventos
ficticios y eliminan solo esos IDs. No hace falta cuenta ni sincronización. El
APK de producción nunca escribe eventos. Pruebas de proveedor falso requieren
API29 por ContentResolver.wrap (solo tests); POC y proveedor real minSdk26.
Con calendario sin sincronizar la lista puede estar vacía; la app no inventa eventos.

## Demo opcional sin cuenta
Con emulador iniciado y Python3/ADB disponibles:
```powershell
python scripts/calendar-demo-fixture.py create
# Introducir la fecha impresa y zona UTC en el panel; conceder lectura y elegir.
python scripts/calendar-demo-fixture.py cleanup
```
`--adb` permite indicar ruta del SDK. `--date AAAA-MM-DD` fija el escenario.
El script crea solo su propio calendario LOCAL con dos eventos ficticios (serie
iniciada el día anterior) y valida propietario/ID antes de borrarlo. Su JSON local
no se versiona. No usarlo para escribir calendarios personales. El APK no incluye
Python ni permisos de escritura. Mantener el JSON hasta ejecutar cleanup.
