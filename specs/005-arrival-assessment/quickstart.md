# Probar UC-005
Ejecutar app. Crear demo opcional `python scripts/calendar-demo-fixture.py create`;
usar fecha impresa y zona UTC para calendario y salida de Referencia. Elegir
reunión09:00. Configurar trayecto35 y consultar salidas08:15,08:25 y08:40:
llegadas08:50/09:00/09:15 →margen10/0/retraso15. Pulsar Evaluar llegada en cada caso.
La llegada se reconsulta y el compromiso se revalida antes de comparar.
Cambiar hora/fecha/zona/selección retira evaluación. Eliminar evento y evaluar
da indeterminado y retira selección. Revocación no pide permiso desde evaluación.
Al terminar `python scripts/calendar-demo-fixture.py cleanup`.

Fixture UC-002 tiene fecha fija2026-11-02, Europe/Madrid: para comparar demo +15
08:50 contra reunión09:00 del mismo día hay que preparar/seleccionar esa ocurrencia.
Nunca se desplaza la demo a mañana. Demo desconocido siempre indeterminado.
No se garantiza llegar físicamente desde estación a reunión: tramo/margen extra0.

```powershell
.\gradlew.bat :core-domain:test :core-data:test :core-platform:testDebugUnitTest :core-presentation:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :core-platform:connectedDebugAndroidTest
.\gradlew.bat :core-presentation:connectedDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
```
Instrumentadas secuenciales. Para avisos Atocha–Chamartín:18000 y17000; pulsar las
sugerencias. Su cobertura sigue siendo parcial y no añade retraso cuantitativo.
