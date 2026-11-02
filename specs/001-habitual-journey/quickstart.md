# Ejecutar y comprobar · UC-001

Requisitos: JDK/SDK de docs/setup.md y un emulador de prueba para instrumentación.

```powershell
.\gradlew.bat :core-domain:test :core-platform:testDebugUnitTest :core-presentation:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :core-presentation:connectedDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:installDebug
```

Ejecutar las dos instrumentaciones secuencialmente. Gradle puede desinstalar
los APK de instrumentación al terminar; usar installDebug para volver a abrir la app.

1. Configurar Origen de prueba / Destino de prueba / 35 y guardar.
2. Confirmar resumen y etiqueta de duración de referencia.
3. Editar a 40, cancelar y comprobar que sigue en 35.
4. Editar a 40 y guardar; cerrar y relanzar sin borrar datos:

```powershell
adb shell am force-stop com.cercafunctions.app
adb shell am start -W -n com.cercafunctions.app/.MainActivity
```

5. Recuperar 40 min y los mismos nombres. Probar nombres iguales, duración vacía,
   decimal, cero y 1441: no se sustituye la configuración válida.
6. Recrear la Activity durante edición: se mantiene el borrador; si muere el
   proceso sin guardar, puede perderse ese borrador, pero no el dato confirmado.

No borrar datos en dispositivos personales. [verification.md](verification.md)
registra el entorno, pruebas ejecutadas y escenarios de errores inyectados.
