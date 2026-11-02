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
.\gradlew.bat :core-domain:test :core-data:test :core-platform:testDebugUnitTest :core-presentation:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :core-platform:connectedDebugAndroidTest
.\gradlew.bat :core-presentation:connectedDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
```
Instrumentadas secuenciales. Tests crean calendarios LOCAL únicos con eventos
ficticios y eliminan solo esos IDs. No hace falta cuenta ni sincronización. El
APK de producción nunca escribe eventos. Pruebas de proveedor falso requieren
API29 por ContentResolver.wrap (solo tests); POC y proveedor real minSdk26.
Con calendario sin sincronizar la lista puede estar vacía; no crea compromisos ficticios.
