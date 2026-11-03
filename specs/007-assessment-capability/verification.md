# Verificación UC-007 ·2026-11-03
Estado: Verified. 17/17 tareas;245 pruebas correctas,169 unitarias y76 instrumentadas,
sin fallos, errores u omitidas.39 nuevas frente a UC-006. APK/lint correctos.
[Inventario JUnit](test-results.json), [SDK real](sdk-results.json),
[contrato empaquetado](artifact-audit.json). El JSON registra avisos lint actuales.
Pixel10Pro Android17/API37.2; instrumentadas secuenciales, sin orquestación LLM.

## Criterios y evidencia
| Contrato | Comprobación ejecutada |
| --- | --- |
| S1 / FR-001–003 | AuthorizationFlowTest: calendario LOCAL ficticio propio, selección explícita, emisión, sustitución, Activity.recreate sin recuperar rawtoken y revocación. Autorización de evaluación independiente del opt-in UC-006 |
| S2 / FR-004 / SC-001 | AssessmentFunctionTest y dominio: mismo QueryJourneyArrival+AssessArrival,35min y08:40 frente a09:00→LATE/-15; DTO mínimo sin textos, nombres de trayecto, IDs o referencia |
| S2 / FR-006 | Comparaciones temprana/igual/tarde y unknown indeterminado. Evidencia fija controlada prueba equivalencia; invocaciones reales separadas pueden diferir en observedAt |
| S3 / SC-002 | Tests de límite exacto30min/comienzo, token malformado/incorrecto/sustituido/revocado; SDK confirma referencia incorrecta sin invalidar la válida y revocación explícita sin datos |
| S4 / FR-005 | SDK cambia título de su propia fixture→Denied; requiere nueva emisión. READ revocado observado→Denied durable, reconceder READ y forzar enable de laboratorio no recupera referencia anterior |
| SC-003 / proceso frío | HOME, kill del proceso debug por su UID, pid vacío, SDK crea nuevo proceso y evalúa sin abrir Activity ni pedir permisos; PIDs saneados en sdk-results |
| FR-006 / privacidad | XML del APK incluye segunda función,5 parámetros (reference/departure/zone obligatorios),12 campos DTO y KDoc. Ambas disabled-default y trusted compat. Manifest mantiene BIND; no permisos EXECUTE/WRITE nuevos |
| Consumidor no autorizado | UnauthorizedCallerTest: framework real, UID distinto, sin EXECUTE_APP_FUNCTIONS ni adoptar shell; ambas capacidades devuelven Denied1000 sin respuesta |
| Fallos / concurrencia | Persistencia parcial/corrupta/fallida deniega. Cancelación se propaga. CAS y mutex preservan sustitución. Test red adicional detectó revocación durante última lectura; resolve ahora relee grant tras consultar proveedor |
| Lifecycle / UI |6 tests VM y4 Compose: estado/error, cancelación, alcance, copiar solo explícitamente y retirada ON_STOP incluso durante edición. Clipboard marcado sensible para ocultar vista previa, sin atribuirle autorización |

## Ejecución y límites
Unitarias: :core-domain:test :core-data:test :core-platform:testDebugUnitTest
:core-presentation:testDebugUnitTest. Instrumentadas, por separado y en secuencia:
:core-platform:connectedDebugAndroidTest, :core-presentation:connectedDebugAndroidTest,
:app:connectedDebugAndroidTest; reinstalar proveedor, :core-appfunctions:connectedDebugAndroidTest.
APK/lint: :app:assembleDebug :app:lintDebug. Guía reproducible en [quickstart](quickstart.md).

Se observaron fallos red antes de implementar:15 dominio,6 persistencia,6 VM,
4 Compose y6 adaptador. La prueba adicional de revocación durante última consulta
también falló antes de corregir. Tests existentes de instalación/rotación se ajustaron
para establecer su estado inicial y esperar la confirmación observable antes de rotar;
conservan las aserciones. Ninguna supresión para ocultar fallos.

Se corrigió también el cliente SDK: adb heredaba y consumía stdin en discovery,
por lo que Python leía una referencia vacía. DEVNULL en comandos sin entrada
preserva el token para la invocación. La llamada directa siempre devolvía la
evaluación correcta; tras corregir el cliente pasó todo el flujo de proceso frío.
El guion espera am start -W y evita enviar Back según indicadores IME históricos.

El SDK shell positivo es privilegiado; no demuestra acceso desde una app ordinaria.
No se necesita TestingAgent para estas comprobaciones deterministas. ChatApp,
privilegios del agente y confirmación de envío permanecen UC-008 sin iniciar.
Ningún mensaje enviado. Solo calendario sintético propiedad del guion; limpieza
verificada, READ retirado y función restaurada desactivada. Sin tokens, hashes de
tokens ni eventos personales en evidencia versionada. No capturas con referencias.

La versión cubre EVENT_ID/BEGIN/END/título/nombre de calendario observados por
Instances; no se leen notas o asistentes ni se promete detectar campos no proyectados.
El límite Events/Instances documentado en UC-004 continúa. No puede observar una
revocación y reconcesión ocurridas íntegramente entre comprobaciones con proceso muerto.
Un fallo de autorización deniega. Si el sistema desactiva antes de invocar puede
devolver1002; dentro de la capacidad, autorización inválida usa1000 uniforme.

Vigencia min(30min,comienzo), doble límite wall/elapsed y bootCount. Drift mayor5s,
reboot o rollback invalidan. Los límites se prueban con relojes inyectados; no se
cambia el reloj real ni se espera30min. Sin alarmas: registro/habilitación pueden
quedar hasta siguiente comprobación, pero no permiten acceso válido tras vencer.
Revocación no retracta resultados ya entregados ni congela proveedor entre procesos.
Rawtoken solo memoria; DataStore guarda hash/HMAC y selección mínima. Clipboard
explícito es otra copia: ocultar UI o revocar no borra el portapapeles del usuario.
No se probó matriz completa Android16/17, API26 o hardware físico.

## Cierre SDD
Spec/plan previos al código en5fd05e2. Checklist humano8/10 intacto.
Primera convergencia:13 criterios,7 decisiones,5 principios y16 tareas. Un hallazgo
partial/LOW de documentación T015 produjo T017: un script de cierre leyó UTF-8
con la codificación Windows. Se repararon acentos/estado y se fijó encoding explícito
en los guiones locales. Código/APK no afectados; no se repiten tests Android por
esta corrección documental. Segunda convergencia:13 criterios (6FR+3SC+4escenarios),7 decisiones,5 principios
y17 tareas; cero hallazgos missing/partial/contradicts/unrequested. Sin hooks.
tasks.md permaneció byte por byte intacto durante la revisión: SHA256
50A689DF9E415A381C7064B5FA526220638C30CBF8C946E4939191391CEF623F.
T016 se marca después, fuera de converge. Entrega en feat/007-assessment-capability
y snapshot uc-007-v1, sin fusionar main. UC-008 sigue Draft.
Los cuatro pasos de la skill Google se aplicaron; sin migración legacy.
Checklist humano8/10 intacto: no se atribuye revisión al usuario.

Revisión final de cierre (incluido signo menos Unicode en la guía histórica):
UTF-8 íntegro,17 tareas checked y cero brechas. Converge sin cambios de tareas:
0C1030DB2C2362CC7FAA35864D7CCCF52900A1D4CFB14934DC98FA11281DBD44.
