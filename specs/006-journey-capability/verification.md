# Verificación UC-006 ·2026-11-03
Estado: Verified ·15/15 tareas completadas.206 pruebas correctas:141 unitarias y65 instrumentadas;
178 previas y28 nuevas (5 política,7 VM,4 Compose,10 adaptador,1 UI/Hilt,1 IPC).
Sin fallos, errores ni omitidas. APK/lint correctos:0 errores,9 avisos de versiones.
Pixel10Pro Android17/API37, imagen37.2. Instrumentadas secuenciales.
[Inventario JUnit](test-results.json). No se cuentan invocaciones SDK como tests JUnit.

## Trazabilidad ejecutada
| Contrato | Evidencia |
| --- | --- |
| S1 / FR-004 / FR-006 | Descubrimiento cmd app_function con una función; XML empaquetado incluye KDoc de función,6 parámetros,2 obligatorios y descripción de DTO; instructions verificada en res/xml del APK |
| S2 / FR-001–003 / SC-001 | JourneyFunctionTest compara mismo QueryJourneyArrival con Clock/repos reales controlados; UID propietario UI consulta misma llegada08:35 UTC que llamada IPC para trayecto sintético35 y salida08:00 |
| S2 / SC-003 | UI cerrada, proceso debug propio9463 creado tras matar PID7665; queryJourney recupera trayecto/consent de DataStore sin Activity, diálogos ni READ_CALENDAR. Registros PIDs en cold-process.json |
| S3 / FR-002 / FR-005 | Falta de configuración→1500; fuente real fallida conserva referencia y contextUNAVAILABLE/NETWORK. Tests cubren almacenamiento fallido, demos normal/+15/desconocida y errores sin mensajes privados |
| S4 / FR-005 / SC-002 | UnauthorizedCallerTest verifica UID diferente y EXECUTE_APP_FUNCTIONS no concedido; framework execute devuelve ERROR_DENIED1000 sin respuesta, sin adoptar shell |
| S4 / FR-005 | Pareja de IDs incompleta→1001; pruebas directas enum/offset/IDs/gap/overlap/pasado. Función desactivada→1002. Tras revocar desde UI, ADB enable no sustituye consentimiento:1000, sin datos |
| FR-002 / incertidumbre | Demos IPC preservan salida2026-11-02, reloj y fuenteFIXTURE aunque input sea2026-11-04; unknown omite llegada/retraso, nunca cero. ContextDEMO_SKIPPED sin red |
| FR-003 / lifecycle | Servicio Hilt depende de casos y repo de consentimiento, sin VMs. Settings false inicial; UI autoriza/revoca, Activity.recreate conserva estado. Callback/almacenamiento ordenados y cancellation propagada |
| FR-004 / SC-002 | DTO no incluye calendario/contactos/avisos textuales; no escritura de trayectos. APK solo INTERNET/READ_CALENDAR más permiso interno de librería; no EXECUTE ni WRITE nuevos para proveedor |

## Evidencia real SDK y UI
Función descubierta:
`com.cercafunctions.appfunctions.BaseJourneyAppFunctionService#queryJourney`.
Se usa la constante generada, no el nombre adivinado del servicio concreto.
Metadatos completos en [discovered](discovered.json); contrato/schema/proxies
inspeccionados dentro del APK, SHA256 en [SDK](sdk-results.json).
La indexación tras instalar fue asíncrona; el cliente reintenta hasta10s.

Instalación inicial desactivada produjo1002. Opt-in contextual desde UI con
READ_CALENDAR revocado, sin trayecto, produjo1500. Se guardó únicamente trayecto
sintético POC Atocha→POC Chamartin35. Consulta de referencia2026-11-04 08:00 UTC
devolvió08:35 UTC, igual que pantalla. [Respuesta](reference.json).
Los observedAt de comprobaciones separadas pueden diferir; el test controlado
con mismo Clock demuestra equivalencia de evidencia, sin afirmar timestamps iguales
en consultas reales hechas en momentos distintos.

[Proceso frío](cold-process.json): HOME y kill del proceso debug mediante su UID
propio, sin force-stop ni modificar paquete stopped; pid vacío antes de invocar,
nuevo PID después y ninguna Activity proveedora retomada. No se necesitó LLM.
El shell SDK positivo tiene privilegios; no representa a una app Android ordinaria.
El testhost de core-appfunctions usa otro UID y solo visibilidad del paquete en
manifest de test, sin permiso EXECUTE_APP_FUNCTIONS ni identidad UiAutomation.
Con app instalada/habilitada obtuvo1000. No se usó set-temporary-caller.

[Revocación](revocation-results.json): retirada por UI→1002; enable de laboratorio
con consentimientofalse→1000; finalmente default restaurado y isEnabled=false.
Consentimiento de prueba revocado y READ_CALENDAR siguefalse. No se consultó agenda
para la capacidad, ni se crearon eventos/contactos/mensajes en este incremento.
Capturas inspeccionadas: [autorización](../../docs/evidence/uc006-authorized.png),
[llegada UI](../../docs/evidence/uc006-ui-reference.png),
[revocación](../../docs/evidence/uc006-revoked.png). Solo datos sintéticos; no se
versionan XML de listados personales. El guion visual debió cerrar el teclado
que interceptaba sus gestos; no fue un fallo de Compose o del producto.

## Red/green y límites
Se observaron5 fallos de política,10 del adaptador,7 VM y4 UI antes de implementar.
Se corrigió un fake Flow que fijaba el fallo al construirlo, en lugar de evaluarlo
al recogerlo. Regresión completa posterior correcta. También se corrigió una
aserción del guion que buscaba instructions en XML de función, cuando pertenece
al recurso de aplicación; el APK lo contiene y aapt2 lo verificó.
Comandos reproducibles en [quickstart](quickstart.md). AppFunctionTestRule no se usa:
sus fakes/Robolectric no sustituyen estas pruebas de sistema. Reglas Compose junit4
heredadas recomiendan v2; no se introducen supresiones ni actualizaciones ajenas.

La prueba real Renfe de este emulador dio UNAVAILABLE/NETWORK. No demuestra estado
actual del trayecto ni predicción para mañana. Sin IDs el contexto es UNRESOLVED;
con IDs explícitos se cubren solo extremos. Nada modifica los minutos de UC-002.
La autorización corresponde a asistentes permitidos por Android, no a cualquier
app. La UI refleja consentimiento del usuario; un disable externo de sistema puede
impedir ejecutar aun con consentimiento vigente. No hay grant por consumidor propio.
Si una escritura de revocación falla no se afirma éxito: UI conserva estado/error
y exige reintento; no se retractan resultados ya entregados. Un check no congela
consentimiento/agenda entre todos los procesos ni sustituye permisos de Android.

Se probó API37.2, no matriz completa16/17, API26 ni hardware físico. minSdk26 de UI
se mantiene; servicio y habilitación requieren sistema compatible36+.
ChatApp/TestingAgent se estudiaron en commit0bb367a2b7ef276218ac37f8eb45f6c9a81ca0cd;
su integración y LLM quedan UC-008. UC-007 no se inicia. Checklist humano8/10 intacto.


## Convergencia SDD y snapshots
13 criterios (6FR+3SC+4escenarios),7 decisiones del plan,5 principios y15 tareas
contrastados con código actual y evidencia, sin comparación Git como evaluación.
Cero hallazgos missing/partial/contradicts/unrequested; sin hooks ni tareas nuevas.
tasks.md quedó byte por byte sin cambio durante converge: SHA256
F5A6925FFB3979DBA63A78DDCCA73D7FC61DF878E3276DAB7AFE42F7A9E6551D.
T015 se marcó después en el cierre. dc44f8d conserva spec/plan antes de código;
entrega en feat/006-journey-capability y uc-006-v1, sin fusionar main.
UC-007/008 permanecen Draft. Los cuatro pasos de skill Google (descubrimiento,
implementación/configuración, KDoc y testing) se ejecutaron; migración legacy no aplica.
