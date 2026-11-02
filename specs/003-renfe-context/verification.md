# Verificación UC-003 · 2026-11-02
Estado: Verified ·14/14 tareas completadas, alcance acotado de avisos de extremos.
107 pruebas correctas:86 unitarias,18 Compose,3 Activity/Hilt. Incluyen69 de
UC-001/002 y38 nuevas. APK compilado, lint0 errores/9 avisos de versiones.
Entorno Windows/JBR25, Gradle9.6.1, Pixel_10_Pro x86_64 Android17/API37 (imagen37.2).
Instrumentadas secuenciales. Sin nuevas supresiones. APIs junit4 Compose heredadas
siguen avisando de migración a v2; funcionan con sincronización explícita.

## Pruebas
| Suite | Pruebas | Fallos | Errores |
| --- | --- | --- | --- |
| com.cercafunctions.domain.estimate.QueryJourneyArrivalTest | 12 | 0 | 0 |
| com.cercafunctions.domain.journey.GetHabitualJourneyTest | 3 | 0 | 0 |
| com.cercafunctions.domain.journey.SaveHabitualJourneyTest | 8 | 0 | 0 |
| com.cercafunctions.domain.rail.QueryRailContextTest | 13 | 0 | 0 |
| com.cercafunctions.data.estimate.ReproducibleJourneyFixturesTest | 5 | 0 | 0 |
| com.cercafunctions.data.rail.OkHttpAlertsSourceTest | 3 | 0 | 0 |
| com.cercafunctions.data.rail.RenfeRailRepositoryTest | 10 | 0 | 0 |
| com.cercafunctions.platform.journey.DataStoreJourneyRepositoryTest | 10 | 0 | 0 |
| com.cercafunctions.presentation.estimate.ArrivalViewModelTest | 8 | 0 | 0 |
| com.cercafunctions.presentation.home.HomeViewModelTest | 8 | 0 | 0 |
| com.cercafunctions.presentation.rail.RailContextViewModelTest | 6 | 0 | 0 |
| core-presentation instrumentation | 18 | 0 | 0 |
| app instrumentation | 3 | 0 | 0 |

## Trazabilidad
| Criterio | Evidencia |
| --- | --- |
| S1 / FR-001 | QueryRailContextTest matching endpoint+impact; OR/AND y límites temporales. RailContextPanelTest selección ID explícita. Cobertura parcial de extremos, nunca tren/línea completa |
| S2 / SC-001 / FR-003 | publicación sin impacto no prueba mañana; ninguna API devuelve minutos nuevos. Referencia conserva valor; regresión ArrivalFlowTest |
| S3 / FR-004 | IDs ausentes/duplicados/desconocidos retornan UNRESOLVED sin red; route+stop queda incierto; no matches no significa puntualidad |
| S4 / FR-002/004 | STALE/FRESHNESS_UNKNOWN/FUTURE_TIMESTAMP/UNAVAILABLE distintos; fuente y tiempos; parser inválido no es lista vacía; timestamp válido se conserva incluso con registros incompatibles |
| S5 condicional | No se usan horarios ni evidencia cuantitativa. Fuentes static/trip_updates investigadas; llegada sigue referencia. Horas>24 fuera de este parser, sin normalización silenciosa |
| S6 | snapshot1147 IDs/nombres únicos; sugerencia pulsada; asociación se conserva al recrear Activity y se invalida al cambiar referencia |
| S7 / FR-005 | demo no ofrece acción HTTP; dominio rechaza sourceFIXTURE; edición cancela e impide publicar respuestas antiguas |
| FR-006 / SC-002 | JSON sintético camel/snake, traducción, timestamps/periodos/IDs inválidos, >2000 entidades; HTTP503/redirect/tamaño conocido/chunked/timeout/cancelación; todos los estados UI tienen fuente y disponibilidad |
| SC-003 | live-probe.json incluye URLs/HTTP/fechas/bytes/SHA256 y observación real separada del APK |

## Evidencia de fuente pública
El2026-11-02T11:46:52Z: alerts.json HTTP200,99718 bytes,69 entidades,
header.timestamp1791358372=2026-10-07T07:32:52Z. ZIP GTFS1147 estaciones y
138395 trips; trip_updates353 entidades con timestamp reciente. Hashes y metadatos
en [live-probe.json](live-probe.json). No se versiona el ZIP ni todo el texto remoto.

Al consumir desde APK se detecta además69 entidades/68 IDs: AVISO_518187 aparece
con contenido diferente. La respuesta se rechaza completa como INVALID_FORMAT;
no se elige una versión arbitraria ni se muestra ausencia de incidencias. La
cabecera válida se conserva en el fallo. La política de10min daría STALE para
una respuesta estructuralmente válida con aquella fecha; no se confunden ambos hallazgos.
Fixture sintético reproduce duplicados conflictivos con timestamp conservado.

## Ejecución
Comandos regenerables en [quickstart](quickstart.md). Antes de implementar se
ejecutaron cuerpos TODO: dominio11 fallos (2 pruebas de rangos ya pasaban),
parser8 fallos, HTTP2 fallos, VM6 y panel5. Después pasaron todas las suites.
Se corrigieron fallos de sintaxis de preparación de pruebas antes de evaluar red.
Servidor loopback inicialmente cerraba antes de completar respuesta grande;
la fixture HTTP se corrigió para transferir contenido y comprobar el límite real.
La comprobación del APK añadió la preservación de timestamp de fallo por formato,
verificada de nuevo con dominio/data/VM, assemble y lint.
AGP/UTP desinstala APK tras tests; se reinstaló y ejecutó RailContextFlowTest
directamente una vez para preparar datos sintéticos de evidencia (OK1, no suma
una prueba distinta). La consulta posterior desde APK sí usa HTTPS real.

## Límites y revisión
No se verifican línea/tren/sentido/estaciones intermedias/servicio directo. El
catálogo es snapshot, no garantía de operación actual. Sin horario ni cálculo
cuantitativo real. No hay caché, polling, backend, calendario ni AppFunctions.
No se valida toda la norma GTFS; se valida el subconjunto estructural consumido
y se conserva incertidumbre donde faltan restricciones. No se abre texto/URLs
remotos ni se interpreta texto como instrucciones o minutos.
Solo emulador, sin hardware físico ni matriz completa de versiones Android.
Checklist humano8/10 inalterado; petición del usuario autoriza construir sin
atribuirle lectura. UC-004–008 permanecen Draft.


## APK real y captura
Consulta desde APK final:2026-11-02T12:13:27.734206Z, estaciones elegidas
Vicálvaro70100/Coslada70108. Resultado UNAVAILABLE/INVALID_FORMAT con cabecera
2026-10-07T07:32:52Z preservada. HTTPS/permiso INTERNET y empaquetado de recursos
comprobados en el grafo real. [Captura inspeccionada](../../docs/evidence/uc003-live.png)
y [texto de resultado](apk-live-result.json). Se conserva emulador previamente iniciado.
No se convierte este fallo de fuente en una predicción ni en una demo automática.

## Convergencia y snapshots
Converge tras implementar:16 criterios revisados (6FR+3SC+7escenarios, con S5
condicional no activado),8 decisiones de diseño,5 principios y14 tareas.
Cero hallazgos missing/partial/contradicts/unrequested; cero tareas añadidas.
No hooks registrados. tasks.md se conservó byte por byte durante converge:
SHA256 F33BE15999DA764F21FB3EF555B603F7367A607B1B5E063CAEEE759E188F8289.
T014 se marcó después al cerrar implementación; se aclaró el supuesto inicial
para reflejar el alcance de extremos ya definido al activar, sin cambiar producto.
`8c2834c` conserva plan/spec previos al producto; rama feat/003-renfe-context y
etiqueta uc-003-v1 conservan la entrega verificada. No se fusiona con main.
