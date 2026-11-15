# Ejecutar UC-002

Abrir raíz en Android Studio, sincronizar y ejecutar `app` (minSdk26).
Configurar trayecto (UC-001), bajar al panel Llegada y elegir Referencia.
Confirmar zona, usar Mañana, indicar hora HH:mm y Consultar salida: duración 35
con salida 08:00 debe dar 08:35. No representa horario ni predicción de Renfe.

Seleccionar Demo normal, Demo +15 y Demo desconocido: campos bloqueados con
salida mañana a las08:00 Europe/Madrid y ruta demo35. Consultar cada modo:
08:35, 08:50 y llegada desconocida. Instante de consulta visible; Clock fijo en tests, sin consulta de red.
Editar una entrada/modo/trayecto debe retirar el resultado anterior.
Recrear Activity conserva borrador; reiniciar proceso reinicia consulta, conserva trayecto.

En PowerShell con JAVA_HOME apuntando a JBR Android Studio:
```powershell
.\gradlew.bat :domain:test :data:test :platform:testDebugUnitTest :presentation:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :presentation:connectedDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
```
Ejecutar instrumentadas secuencialmente en un dispositivo/emulador iniciado.
Las pruebas unitarias fijan reloj para pasado, medianoche y DST Madrid
2026-03-29 02:30 (inexistente) / 2026-10-25 02:30 (dos offsets).
La fecha actual no permite consultar esas salidas históricas en referencia;
esas ramas se verifican con reloj fijo en pruebas, sin alterar el dispositivo.
