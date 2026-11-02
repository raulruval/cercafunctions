# Verificación · UC-001

Fecha: 2026-11-02. Rama: `feat/001-habitual-journey`.
Snapshot de cierre: etiqueta `uc-001-v1`.

## Resultado

UC-001 permite configurar, guardar, recuperar y editar un único trayecto.
Estado de aceptación: Verified. 38 pruebas distintas aprobadas y lint sin errores.
La base y SDD se publicaron previamente en main/chore/sdd-foundation con sus etiquetas.

## Autorización y alcance

El usuario pidió «Haz push al repo de lo que llevamos hasta ahora y comienza con
el primer caso de uso» el 2026-11-02. Esa instrucción autoriza construir el caso
propuesto. Las dos casillas de revisión previas se conservan sin modificar:
no se afirma que el usuario haya leído documentos o realizado una revisión formal.

Un trayecto editable, nombres libres normalizados y duración de referencia 1–1440.
Sin horarios, red ferroviaria, permisos de calendario o AppFunctions nuevas.

## Pruebas ejecutadas

| Suite | Cantidad | Resultado |
| --- | --- | --- |
| GetHabitualJourneyTest | 3 | 0 fallos |
| SaveHabitualJourneyTest | 8 | 0 fallos |
| DataStoreJourneyRepositoryTest | 10 | 0 fallos |
| HomeViewModelTest | 8 | 0 fallos |
| HomeScreenTest, en emulador | 8 | 0 fallos |
| JourneyFlowTest, app real | 1 | 0 fallos |
| Total | 38 | 0 fallos, 0 omitidos |

JBR 25, Gradle 9.6.1, SDK 37, AVD Pixel_10_Pro API 37.2 x86_64 de 16 KB.
Se verificó esa imagen; no se ha ejecutado una matriz de dispositivos/API.

```powershell
.\gradlew.bat :core-domain:test :core-platform:testDebugUnitTest :core-presentation:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :core-presentation:connectedDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest :app:lintDebug
```

Las instrumentaciones se ejecutaron en secuencia, no simultáneamente.
Informes de tests bajo `build/test-results` y `build/outputs/androidTest-results`
de sus módulos; lint en `app/build/reports/lint-results-debug.html`.
Lint: **0 errores, 9 avisos de versiones más recientes**. Sin baselines o supresiones.
El compilador avisa de los constructores de reglas Compose anteriores a v2,
todavía soportados; no es un fallo de las pruebas. No se oculta el aviso.

## Evidencia por escenario

| Escenario | Evidencia |
| --- | --- |
| S1 · guardar | Caso de uso + ViewModel + UI + JourneyFlowTest; confirmación solo tras persistir |
| S2 · recuperar al reiniciar | Archivo real cerrado/recreado y proceso de app 13752 → 14049; recupera origen/destino y 40 min |
| S3 · nombres inválidos | SaveHabitualJourneyTest y errores de campo en HomeScreenTest |
| S4 · duración inválida | Límites 0/1/1440/1441, enteros fuera de rango, vacío, texto, decimal y overflow |
| S5 · escritura fallida | IOException inyectada, sin mutación/éxito; ViewModel conserva 40 y permite reintentar; UI muestra el error |
| S6 · lectura fallida/parcial | Archivo corrupto/Preferences parciales, IOException y estado Unavailable, sin acceso a formulario vacío |
| S7 · actualizar | Reemplazo transaccional y edición 35 → 40 con app/Hilt/DataStore reales |
| S8 · abandonar edición | Pruebas de VM/UI y Atrás en la Activity real; recreación conserva el borrador, cancelar recupera 35 |

Los fallos de E/S se prueban mediante inyección y corrupción de archivos temporales;
no se simula llenar físicamente el almacenamiento del emulador.

## Persistencia y revisión visual

Gradle/UTP desinstala los APK al limpiar la ejecución instrumentada en esta
configuración. Para comprobar el proceso sin esa limpieza se instalaron app y
APK de test con `adb install -r`, se ejecutó JourneyFlowTest mediante
`adb shell am instrument -w -e class com.cercafunctions.app.JourneyFlowTest com.cercafunctions.app.test/androidx.test.runner.AndroidJUnitRunner`
y después se usó `am force-stop` / `am start -W`.

Se confirmó ausencia de proceso tras cerrar, nuevo PID al abrir y jerarquía de
pantalla con Origen de prueba, Destino de prueba y duración 40 min. Los valores
son datos de prueba, no estaciones/tiempos reales de Renfe.

[Resumen tras reiniciar](../../docs/evidence/uc001-summary.png) y
[formulario de edición](../../docs/evidence/uc001-editor.png).
Revisados visualmente en vertical: textos legibles y campos/acciones sin desbordamientos.

## Desarrollo y correcciones

Las pruebas de dominio fallaron inicialmente (8/11) y las de almacenamiento
(9/10) antes de implementar sus reglas/adaptador. Las de ViewModel fallaron
(7/8) antes de su implementación. Esos fallos eran de comportamiento.

El primer intento de UI no fue una señal red válida de comportamiento: el
harness falló antes por Espresso 3.5.0 transitivo y una API InputManager retirada.
Se fijó Espresso 3.7.0 solo para androidTest en app/presentación. Es la corrección
documentada en las [notas oficiales de AndroidX Test](https://developer.android.com/jetpack/androidx/releases/test).
Las ocho pruebas UI y la integral pasan después de corregir la infraestructura.

## Cierre de requisitos

FR-001–FR-008, SC-001–SC-003 y S1–S8 se contrastan con código y evidencia anterior.
No se añadieron módulos, backend o capacidades fuera de UC-001. La clase de
repositorio Android vive en plataforma, con DataStore singleton creado en app;
dominio/data siguen sin importar Android. La implementación en memoria es un fixture.

Los cambios preexistentes de Android Studio en `gradle.properties` y
`gradle/gradle-daemon-jvm.properties` se mantienen fuera del commit.

## Revisión de convergencia

Skill oficial speckit-converge: 8 requisitos funcionales, 3 criterios de éxito,
8 escenarios, 6 decisiones de diseño y 5 principios contrastados con los artefactos
y el código actual. Resultado: 0 hallazgos accionables; no se añaden tareas.
La revisión dejó tasks.md intacto; el cierre de T012 se registra después como
parte de la entrega. No hay hooks de extensión configurados.
