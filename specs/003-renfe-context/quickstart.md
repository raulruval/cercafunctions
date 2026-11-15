# Probar UC-003
Abrir repo, sincronizar Android Studio, ejecutar app con red. Configurar trayecto
y consultar una salida en Referencia. En Avisos Renfe buscar estaciones por
nombre o ID y pulsar las sugerencias correspondientes: no basta escribir texto.
Ejemplo: Vicálvaro70100 y Coslada70108. Confirmar asociación al trayecto guardado.
Pulsar Consultar avisos Renfe: ver fuente, consulta, timestamp y estado. Si feed
sigue en2026-10-07 y su estructura es válida, aparece caducado. En la consulta
verificada, además había IDs duplicados contradictorios: se rechaza como formato
no compatible, conservando timestamp. Ningún caso se presenta como servicio puntual.
La cobertura es solo de extremos; no confirma que el trayecto sea directo.
Seleccionar demo UC-002: panel informa que no consulta Renfe para esos datos.
Cambiar hora/modo/estaciones elimina contexto anterior. Red apagada→error explícito,
referencia conserva su etiqueta. Reiniciar conserva trayecto, no asociación temporal.

```powershell
.\gradlew.bat :domain:test :data:test :platform:testDebugUnitTest :presentation:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :presentation:connectedDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
```
Instrumentadas secuenciales con emulador iniciado (validación API37.2 existente).
Red real no decide tests: respuestas deterministas, Clock fijo y servidor loopback.
Regenerar snapshot solo intencionadamente: `python scripts/update-renfe-stations.py`.
