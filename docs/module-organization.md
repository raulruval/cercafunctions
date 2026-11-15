# Organización y renombrado de módulos

Decisión del 2026-11-04, solicitada por el usuario. Cambio de nombres y rutas;
se conservan seis módulos, sus responsabilidades y el grafo de dependencias.

## Plan y criterios

1. Renombrar core-domain → domain, core-data → data, core-platform → platform,
   core-presentation → presentation y core-appfunctions → appfunctions.
2. Actualizar settings, dependencias, herramientas, comandos y documentación de uso.
3. Mantener namespace, applicationId, paquetes Kotlin e IDs publicados AppFunctions.
4. Conservar las evidencias y planes históricos de UC-001–008 con sus nombres de entonces.
5. Verificar tests JVM de los cinco módulos, assembleDebug y lintDebug. Comparar
   fuentes Kotlin antes/después e IDs del servicio generado. Registrar resultados abajo.

## Nombres técnicos de las dos organizaciones

| Organización | Nombre habitual | Primera división |
| --- | --- | --- |
| features/journeys/{presentation,domain,data} | Feature-first / package-by-feature, con capas internas | Capacidad funcional; después responsabilidad técnica |
| {domain,data,presentation}/journeys | Layer-first / package-by-layer, con paquetes funcionales internos | Responsabilidad técnica; después capacidad funcional |

Si esas fronteras son módulos Gradle se habla de modularización por funcionalidad
o por capas, respectivamente. Si solo son carpetas/paquetes, es organización de
paquetes: no impone aislamiento de compilación. Vertical slicing describe la
orientación funcional; tener carpetas por feature no demuestra por sí solo una
Vertical Slice Architecture completa. Ambas organizaciones pueden aplicar
Clean Architecture y la misma inversión de dependencias.

Esta POC utiliza **modularización por capas/responsabilidades con organización
funcional dentro de los módulos**. El renombrado no la convierte en feature-first.
appfunctions y platform son adaptadores técnicos, no nuevas funcionalidades.

core suele señalar componentes compartidos. No es obligatorio ni expresa una capa
concreta. En esta POC no se conserva porque no distingue una familia transversal
de otras familias de módulos. No se crean módulos adicionales para justificarlo.

## Qué demuestra Rocket-Beer

La revisión pública se limita a los repositorios y commits de [research.md](research.md).
rocket-core-domain publica bases de casos de uso; rocket-core-data incluye permisos;
rocket-core-presentation agrupa bases de ViewModel, navegación y otros componentes.
Son bibliotecas reutilizables: ahí core tiene sentido como denominación de base.
No equivalen al dominio, datos y presentación completos de Cercanías.

En los README y settings inspeccionados no se encontró una norma que obligue a las
apps consumidoras a organizar su raíz por capas o por features. La agrupación de
esas bibliotecas por responsabilidad no prueba una prescripción feature-first o
layer-first para toda una aplicación. No se atribuye esta conclusión a repositorios
privados ni a toda la organización.

Referencias: [Rocket presentación](https://github.com/Rocket-Beer/rocket-core-presentation),
[Rocket dominio](https://github.com/Rocket-Beer/rocket-core-domain),
[patrones Android](https://developer.android.com/topic/modularization/patterns).

## Verificación

Validación ejecutada el 2026-11-04: Gradle BUILD SUCCESSFUL en 1 min 15 s, 196 tareas (167 ejecutadas y 29 up-to-date).

```powershell
.\gradlew.bat :domain:test :data:test :platform:testDebugUnitTest :presentation:testDebugUnitTest :appfunctions:testDebugUnitTest :app:assembleDebug :app:lintDebug --console=plain
```

| Módulo | Pruebas JVM | Fallos / errores |
| --- | --- | --- |
| domain | 77 | 0 / 0 |
| data | 18 | 0 / 0 |
| platform | 21 | 0 / 0 |
| presentation | 53 | 0 / 0 |

appfunctions:testDebugUnitTest devuelve NO-SOURCE: sus pruebas propias están en androidTest; no se contabilizan como ejecutadas. No se reejecutó instrumentación ni se reinstaló la APK en el dispositivo para este renombrado.

89 archivos de fuentes y recursos de los módulos coinciden con HEAD tras normalizar finales de línea. Los dos IDs públicos generados se mantienen y la APK contiene el XML de capacidades. assembleDebug y lintDebug completados. Se actualizó también el guion local y se comprobaron sus enlaces. git diff --check sin errores.

Los avisos de API nativa del JDK y de else redundante en ArrivalPanel no bloquean el build y no se modificó código Kotlin para ocultarlos.
