# Validación prevista · UC-001

Estado: guía para después de implementar. Hoy la pantalla base no permite guardar.

1. En emulador API 26+, abrir app sin configuración y elegir Configurar.
2. Guardar Origen de prueba / Destino de prueba / 35 y comprobar el resumen.
3. Cerrar y relanzar el proceso sin borrar datos: recuperar los tres valores.
4. Editar a 40, guardar y relanzar: recuperar 40 con el mismo origen/destino.
5. Probar los inválidos de spec.md, cancelar una edición y repetir después de rotación.
6. Pruebas con fallo inyectado: lectura no disponible y escritura fallida sin éxito falso.

Comandos previstos desde el repositorio, con JDK/SDK configurados:

```powershell
.\gradlew.bat :core-domain:test :core-platform:testDebugUnitTest :core-presentation:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :core-presentation:connectedDebugAndroidTest
.\gradlew.bat :app:installDebug
adb shell am force-stop com.cercafunctions.app
adb shell am start -W -n com.cercafunctions.app/.MainActivity
```

Los tests nuevos se crearán al implementar. No ejecutar borrados de datos sobre
un dispositivo del usuario; usar emulador de pruebas. Añadir evidencia real,
entorno y resultados a verification.md al cierre. Ningún comando de esta guía
se ha ejecutado como prueba de la funcionalidad futura.
