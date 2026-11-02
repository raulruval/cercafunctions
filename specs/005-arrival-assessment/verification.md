# Verificación UC-005 · 2026-11-02
Estado: Verified ·13/13 tareas completadas.178 pruebas correctas:129 unitarias y49 instrumentadas.
149 de incrementos anteriores y29 nuevas (13 dominio,8 ViewModel,7 Compose,1 app).
Sin fallos, errores ni omitidas. APK y lint correctos:0 errores,9 avisos de versiones.
Pixel10Pro x86_64 Android17/API37, imagen37.2; instrumentadas secuenciales.
Inventario obtenido de XML JUnit: [resultados](test-results.json).

## Trazabilidad ejecutada
| Contrato | Evidencia |
| --- | --- |
| S1–3 / SC-001 / FR-001–002 | AssessArrivalTest compara +10/0/−15; AssessmentPanelTest y AssessmentFlowTest muestran los tres resultados con el grafo Hilt real y CalendarContract |
| S4 / FR-005 / SC-003 | Falta de entradas, fixture desconocido y agenda inválida dan Indeterminate con causa; no hay llamadas de mensajería ni servicio publicado |
| S5 / FR-001 | Mismos instantes con zonas Europe/Madrid, UTC y America/New_York dan mismo margen; medianoche conserva fecha y zonas completas |
| S6 / FR-003 | Trayecto guardado cambiado exige refrescar; reconsulta exactamente salida/offset/escenario. observedAt puede renovarse sin TTL arbitrario; salida pasada sigue rechazo UC-002 |
| S7 / FR-005 | Dominio distingue permiso/fallo/cambio; CalendarAssessmentViewModelTest retira selección/detalles y mantiene explicación. AppFlow elimina solo su evento y verifica retirada real |
| FR-006 | Fracciones antes/después de minuto exacto y ±1ns: floor disponible, −ceil tardío. UI muestra 09:00:00.000000001 con retraso1; no redondea a puntualidad |
| FR-003–004 / SC-002 | Referencia y fixture conservan etiqueta, fecha fija, observedAt y assessedAt. Demo con reloj actual2099 sigue fija en2026; desconocido no se convierte en cero ni lee agenda |
| Lifecycle/cancelación | VM cancela/respuestas antiguas suprimidas al editar, seleccionar, revalidar y volver a primer plano; app Activity.recreate conserva selección pero retira evaluación; CancellationException se propaga |
| Privacidad/arquitectura | Resultado sin título/calendario/participantes/IDs de agenda; dominio JVM, seis módulos sin dependencias nuevas; resultado no es autorización de divulgación |

## Comprobaciones y red/green
Comandos reproducibles en [quickstart](quickstart.md). Se observaron13 fallos
de dominio con la implementación pendiente y7 de UI antes de construir el panel.
La primera ejecución de tests del coordinador falló por compilar su helper fuera
de TestScope; se corrigió y sus7 tests pasaron. No se atribuye red conductual a
esa ejecución. Después pasaron regresión, integración, APK y lint. Un ajuste
final del texto Próximamente requirió recompilar APK/lint, también correctos.
Avisos heredados de reglas Compose junit4 recomiendan v2; no hay supresiones.

## Límites y decisiones
La referencia usa duración guardada: no predice circulación real. Avisos Renfe
son cualitativos y no añaden minutos. Margen extra y estación–reunión son0,
visibles en pantalla. Demo UC-002 conserva fecha fija2026-11-02 y su reloj;
no se mueve a mañana ni pierde su etiqueta al comparar con calendario real.
Se revalida Instances publicada, sin congelar cambios posteriores ni prometer
detectar divergencias internas Events/Instances descritas en UC-004.
Solo se verificó este emulador, sin matriz de proveedores/hardware físico.
No AppFunctions ni mensajes en UC-005; UC-006 queda Draft. Checklist humano8/10
sin cambios: tests y autorización de implementar no significan lectura del usuario.


## Evidencia visual con calendario ficticio
Se seleccionó una ocurrencia09:00 UTC del2026-11-03 del calendario LOCAL propio
creado por la utilidad SDK, con duración de trayecto35. Desde los controles de
la app se consultaron08:15/08:25/08:40 y se evaluó cada una: +10/0/retraso15.
La demo desconocida dio indeterminado conservando su fecha2026-11-02 y reloj fijo.
La automatización inicial cerró el editor con Back antes de guardar; se corrigió
el guion SDK, se retomó la prueba y se verificaron los cuatro resultados.
No se versionan listados de agenda: solo pantallas de comparación y campos
filtrados del resultado. El calendario ficticio propio se eliminó verificando
propiedad y ausencia; ningún calendario ajeno se modificó. Permiso READ dejado
revocado e instalada la compilación final. [Registro](manual-result.json).

Capturas: [margen10](../../docs/evidence/uc005-early.png),
[igualdad](../../docs/evidence/uc005-equal.png),
[retraso15](../../docs/evidence/uc005-late.png),
[desconocido](../../docs/evidence/uc005-unknown.png).
Se tomaron antes del ajuste final del pie Próximamente; ese texto antiguo del
pie no describe el estado final del APK. El cálculo mostrado sí corresponde a UC-005.


## Convergencia SDD y snapshots
Primera revisión:16 criterios (6FR+3SC+7escenarios),7 decisiones,5 principios y12
tareas. F1 partial/MEDIUM: el alcance pide retirar evaluación al salir de primer
plano, pero solo se invalidaba ON_RESUME. Se añadió T013 append-only, se implementó
ON_STOP y se verificó cancelación/retirada con selección conservada:8 tests del
coordinador y Activity real movida a CREATED, retornada y recreada. Se corrigió
un import del helper de esa prueba antes de su ejecución satisfactoria.

Segunda revisión: mismos16 criterios,7 decisiones,5 principios y13 tareas.
Cero hallazgos missing/partial/contradicts/unrequested, sin hooks; tasks.md quedó
byte por byte igual durante esta segunda convergencia: SHA256
0619898500511CDDF577F42B0EEFEBC5B951BFAC87D06D7101C8E61AC66C442C.
T012 se marcó después del resultado. No se usaron diferencias Git como evaluación.
Se comprobó APK final: INTERNET/READ_CALENDAR, sin WRITE_CALENDAR.
`0558900` conserva especificación/plan antes de producto. Entrega en rama
feat/005-arrival-assessment, etiqueta uc-005-v1. No se fusiona main ni se inicia UC-006.
