# Verificación UC-002 · 2026-11-02

Estado: Verified. 12/12 tareas, 69 pruebas correctas (54 unitarias, 13 Compose,
2 Activity/Hilt), assembleDebug correcto, lint 0 errores y 9 avisos de versiones.
Entorno: Windows, JBR25, Gradle9.6.1, Pixel_10_Pro Android17/API37, imagen37.2,
x86_64. Instrumentadas de presentation y app se ejecutaron secuencialmente.
Sin supresiones nuevas. Las reglas Compose junit4 heredadas avisan de migración
a v2; los tests usan sincronización waitUntil/idle y completan correctamente.

## Resultados
| Suite | Pruebas | Fallos | Errores |
| --- | --- | --- | --- |
| com.cercafunctions.domain.estimate.QueryJourneyArrivalTest | 12 | 0 | 0 |
| com.cercafunctions.domain.journey.GetHabitualJourneyTest | 3 | 0 | 0 |
| com.cercafunctions.domain.journey.SaveHabitualJourneyTest | 8 | 0 | 0 |
| com.cercafunctions.data.estimate.ReproducibleJourneyFixturesTest | 5 | 0 | 0 |
| com.cercafunctions.platform.journey.DataStoreJourneyRepositoryTest | 10 | 0 | 0 |
| com.cercafunctions.presentation.estimate.ArrivalViewModelTest | 8 | 0 | 0 |
| com.cercafunctions.presentation.home.HomeViewModelTest | 8 | 0 | 0 |
| app instrumentation | 2 | 0 | 0 |
| core-presentation instrumentation | 13 | 0 | 0 |

## Trazabilidad
| Criterio | Evidencia real |
| --- | --- |
| S1 / FR-001 | QueryJourneyArrivalTest reference + ArrivalFlowTest: 35 min/08:00 →08:35; referencia identificada |
| S2 / SC-001 | ReproducibleJourneyFixturesTest + ArrivalFlowTest: +15 →08:50; ruta demo independiente de duración real |
| S3 / FR-002 | tomorrow local-date y frontera UTC; ArrivalViewModelTest y ArrivalFlowTest confirman fecha/hora/zona y botón de consulta |
| S4 / FR-005 | gap/overlap Madrid, offsets inválidos y ambos cambios DST; VM elige offset; ArrivalPanelTest muestra opciones sin autoelección |
| S5 | dominio MissingJourney y fallo lectura; HomeScreen solo incorpora panel con Ready |
| S6 / FR-003/004 / SC-002/003 | catálogo fijo normal/+15/unknown; resultado igual con inputs/reloj externo diferentes; null nunca se convierte en cero; UI con fuente/reloj/fecha/horizonte |
| FR-006 | 12 nuevas pruebas dominio, 5 catálogo, 8 VM, 5 panel y 1 grafo real, además de 38 anteriores |
| Lifecycle/cancelación | VM cancelación/edición en vuelo; Activity.recreate conserva consulta; guardar otra duración invalida resultado |

## Ejecución y red/green
Comandos completos en [quickstart](quickstart.md). Primero se ejecutaron contratos
con cuerpos TODO: dominio12 fallos esperados; catálogo4 (ausencia pasa sin catálogo);
ViewModel7; panel5. Tras implementación, todas las suites pasan.
Una comprobación adicional directa con `am instrument` (ArrivalFlowTest, OK1)
preparó el APK instalado para evidencia visual porque AGP/UTP desinstala la app.
No se suma esa repetición a las 69 pruebas distintas.

Logs y XML completos se entregan también en `outputs/uc002` del workspace de Codex.
No se versionan logs temporales ni datos privados. Los XML originales permanecen
en build/test-results y build/outputs/androidTest-results, regenerables.

## Límites
Sin Renfe real, calendario ni capacidades publicadas: siguen UC-003–008.
Referencia calcula duración del usuario sin retraso operativo conocido. Las demos
usan ruta/fecha/reloj completos fijos, no estiman mañana ni usan incidencias actuales.
Consulta efímera; persistencia del trayecto ya cubierta por UC-001. Inputs manuales
ISO/IANA son una decisión pequeña de POC; sin planificador de horarios.
No se ejecutó en hardware físico ni en todas las versiones Android soportadas.

Checklist de requisitos conserva 8/10 checks documentales: no representan aprobación
humana. La instrucción de continuar autoriza construcción, no atribuye lectura al usuario.

## Convergencia SDD
Ejecutada tras implementación: 15 criterios (6 FR + 3 SC + 6 escenarios),
7 decisiones de diseño, 5 principios y 12 tareas revisados contra código actual,
sin comparación de ramas. Cero hallazgos missing/partial/contradicts/unrequested.
Cero tareas nuevas. Sin hooks registrados. Durante converge tasks.md se conservó
byte por byte (SHA256 0B72D75076CD7A1143D6BC757A8ECACEF99FC1E4F7D816F7100F2EE1882E9543).
La marca final T012 se realizó después, en el cierre de implementación.

## Snapshots
`8829fc6`: contrato y plan antes del commit de producto. Rama
`feat/002-journey-estimate`; etiqueta `uc-002-v1` para entrega verificada.

## Evidencia visual
Capturas del APK real en emulador, inspeccionadas. [Demo +15](../../docs/evidence/uc002-delayed.png)
y [desconocido](../../docs/evidence/uc002-unknown.png). Datos enteramente sintéticos.


Comprobación manual adicional de proceso: force-stop cambió PID1089→4320.
Tras relanzar se conservan Origen de prueba/Destino de prueba/40 min y se vuelve
a Referencia con fecha 2026-11-03 y sin la consulta demo anterior. Se mantuvo el
emulador que ya estaba iniciado; no se alteró la hora del dispositivo.
