# Verificación UC-004 · 2026-11-02
Estado: Verified ·14/14 tareas completadas.149 pruebas correctas:
108 unitarias y41 instrumentadas (12 plataforma,25 Compose,4 Activity/Hilt).
107 de incrementos anteriores y42 nuevas. Ningún fallo/error/skip.
AssembleDebug correcto; lint0 errores/9 avisos de versiones existentes.
Windows/JBR25/Gradle9.6.1, Pixel_10_Pro x86_64 Android17/API37 imagen37.2.
Instrumentadas de los tres módulos ejecutadas secuencialmente.

## Suites
| Suite | Pruebas | Fallos | Errores |
| --- | --- | --- | --- |
| com.cercafunctions.domain.calendar.CalendarUseCasesTest | 12 | 0 | 0 |
| com.cercafunctions.domain.estimate.QueryJourneyArrivalTest | 12 | 0 | 0 |
| com.cercafunctions.domain.journey.GetHabitualJourneyTest | 3 | 0 | 0 |
| com.cercafunctions.domain.journey.SaveHabitualJourneyTest | 8 | 0 | 0 |
| com.cercafunctions.domain.rail.QueryRailContextTest | 13 | 0 | 0 |
| com.cercafunctions.data.estimate.ReproducibleJourneyFixturesTest | 5 | 0 | 0 |
| com.cercafunctions.data.rail.OkHttpAlertsSourceTest | 3 | 0 | 0 |
| com.cercafunctions.data.rail.RenfeRailRepositoryTest | 10 | 0 | 0 |
| com.cercafunctions.platform.journey.DataStoreJourneyRepositoryTest | 10 | 0 | 0 |
| com.cercafunctions.presentation.calendar.CalendarViewModelTest | 10 | 0 | 0 |
| com.cercafunctions.presentation.estimate.ArrivalViewModelTest | 8 | 0 | 0 |
| com.cercafunctions.presentation.home.HomeViewModelTest | 8 | 0 | 0 |
| com.cercafunctions.presentation.rail.RailContextViewModelTest | 6 | 0 | 0 |
| core-platform instrumentation | 12 | 0 | 0 |
| core-presentation instrumentation | 25 | 0 | 0 |
| app instrumentation | 4 | 0 | 0 |

## Trazabilidad
| Requisito | Evidencia ejecutada |
| --- | --- |
| S1 / FR-001 | CalendarPanelTest botón contextual; VM sin consultas en init/ON_RESUME; diálogo real rechazado/concedido en emulador |
| S2 / FR-004 / SC-001 | No selección automática con una ocurrencia; claves EVENT_ID+BEGIN; AppFlow elige entre títulos iguales, conserva selección tras Activity.recreate |
| S3 / FR-006 | RRULE/EXDATE en proveedor real y recurrencia durante DST; muestra ocurrencia del día, no DTSTART de serie |
| S4 / SC-002 | VM denegación/revocación, adaptador SecurityException antes/durante consulta, null proveedor vs vacío; revocación real mata proceso y al volver no hay selección |
| S5 / FR-005 | Revalidate exacto: fin cambiado, movido, borrado; no sustituye por otra reunión. Estado solo en memoria, sin SavedState/DataStore |
| S6 / SC-001 | Dos calendarios/títulos iguales con claves distintas; editar día/zona cancela y retira consulta; formato inválido no lanza permiso |
| FR-002 / SC-003 | Día23/25h, límites [inicio,siguienteinicio), comienzo anterior solapado y siguiente medianoche excluidos; cambio de zona y evento creado en New York |
| FR-003 | Proveedor y mapper excluyen all-day/cancelado/no visible. Proyección limitada; no asistentes/descripción/cuenta/ubicación |
| FR-006 / privacidad | Ninguna publicación AppFunctions, escritura de producción ni copia persistente; APK verificado declara READ_CALENDAR y no WRITE_CALENDAR |
| Cancelación | CancellationSignal cancela proveedor bloqueado; Cursor cerrado también en fallo; VM no publica respuestas antiguas |

## Evidencia real de UI/permisos
APK reinstalado tras AGP/UTP. Se creó un calendario LOCAL propio con UUID y dos
eventos ficticios; serie diaria original2026-11-02 y ocurrencia seleccionada
2026-11-03 09:00+01:00 Europe/Madrid. No había trayecto configurado: UC-004 funciona
independientemente. Rechazo real no lee agenda; segunda petición concede y lee.
Se seleccionó y revalidó ocurrencia, después se revocó READ_CALENDAR mediante ADB.
Proceso PID22433→24905: al volver, sin datos/selección y acceso requerido.
El permiso se dejó revocado al terminar esa comprobación. El calendario de
evidencia se eliminó por ID/propietario y se verificó ausencia. Ningún calendario
ajeno fue modificado. [Detalles sintéticos](manual-result.json).

Capturas inspeccionadas: [rechazo](../../docs/evidence/uc004-denied.png),
[selección recurrente](../../docs/evidence/uc004-selected.png),
[revocación](../../docs/evidence/uc004-revoked.png).
Utilidad opcional scripts/calendar-demo-fixture.py probada create/cleanup con
fecha2026-11-05 y zonaUTC: solo calendario LOCAL propio, limpieza verificada.
Sus metadatos locales se ignoran en Git; no añade permisos al APK.

## Ejecución y límites
Comandos regenerables en quickstart.md. Red/green: dominio9 fallos esperados
(3 tests de modelos ya pasaban), adaptador6, VM10, panel7, antes de implementar.
Después se ejecutó regresión completa y flujo real. No se añaden supresiones de
lint. Las reglas Compose junit4 heredadas siguen avisando de migración a v2.

La primera prueba de modificación de fin detectó un límite real del proveedor:
actualizar solo DTEND puede dejar Instances.END anterior en esta imagen/AOSP.
La fixture temporal actualiza DTSTART original+DTEND nuevo. Se revalida la vista
Instances publicada, sin prometer detectar divergencias internas con Events ni
congelar modificaciones posteriores. Investigación/fuentes en research.md.
UC-005 deberá revalidar otra vez antes de evaluar. No se ha iniciado UC-005.

Pruebas instrumentadas de fixtures/fake usan API29+ por UiAutomation/Resolver.wrap;
APK minSdk26, pero solo se verificó esta imagen37.2, sin hardware físico ni matriz
completa de proveedores. Vacío puede significar falta de sincronización; no se
presupone cuenta Google ni AppFunctions públicas de Google Calendar.
Checklist humano8/10 se conserva; implementar no atribuye lectura al usuario.


## Convergencia SDD y snapshots
15 criterios revisados (6FR+3SC+6escenarios),9 decisiones de diseño,5 principios
y14 tareas contrastados con código actual, sin comparación de ramas como evaluación.
Cero hallazgos missing/partial/contradicts/unrequested; ninguna tarea adicional.
Sin hooks. tasks.md se conservó byte por byte durante converge:
SHA256 41DB1136C79807EFE75A60BE13B9D44BD944698FB8F8B5B8C3BE12DEAA526D9E.
La casilla T014 se marcó después en el cierre de implementación.
`421cd04` conserva especificación/plan iniciales; rama feat/004-calendar-selection,
etiqueta uc-004-v1 para entrega. No se fusiona con main ni se inicia UC-005.
