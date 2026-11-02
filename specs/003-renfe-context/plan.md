# Implementation Plan: UC-003 · Contexto de avisos
Branch:`feat/003-renfe-context` ·2026-11-02 ·[spec](spec.md)

## Summary / Technical Context
GET HTTPS de avisos Renfe, selector explícito de estaciones y contexto cualitativo
separado de referencia UC-002. Kotlin2.4.10/JVM17, Android26+, Compose/Material3,
Hilt/coroutines existentes. Añadir únicamente OkHttp5.5.1 y Gson2.15.0 en core-data.
INTERNET es permiso normal de manifest; no diálogo runtime. Sin backend, módulos
nuevos ni cambios en persistencia. Pruebas JUnit/coroutines, HTTP loopback JVM,
Compose y Activity real; red pública solo como evidencia separada no determinista.

## Constitution Check
PASS antes/después de diseño: spec previa a código, dominio JVM, seis módulos,
caso de uso común, procedencia/frescura explícitas, cero minutos inferidos,
cancelación y límites comprobados. Checklist8/10 documental no se altera; la
petición actual autoriza implementar sin atribuir lectura al usuario.

## Project Structure / decisions
- core-domain/src/main/kotlin/com/cercafunctions/domain/rail/: modelos/contratos,
  QueryRailContext, selección y coincidencia temporal. No cambia ArrivalEstimate.
- core-data/src/main/kotlin/com/cercafunctions/data/rail/: parser JSON a dominio,
  fuente OkHttp cancelable, repositorio, catálogo de IDs/nombres snapshot en
  src/main/resources/renfe-stations.tsv. Fallos HTTP/red/formato/tamaño tipados.
- core-presentation/src/main/kotlin/com/cercafunctions/presentation/rail/:
  RailContextViewModel y panel stateless, recursos strings. Selector buscable
  con máximo5 sugerencias y confirmación por pulsación, sin fuzzy autoasociación.
- ArrivalPanel/ArrivalRoute: slot solo tras Estimated; muestra panel informativo
  en demo sin ofrecer red. Al cambiar referencia, invalidar/cancelar contexto.
- app/MainActivity y di/JourneyModule ensamblan; manifest declara INTERNET.
- scripts/update-renfe-stations.py reproduce snapshot; docs/evidence y spec del caso.

## Contract and limits
Selección válida requiere IDs distintos presentes en catálogo. Referencia exacta
se entrega al caso y nunca se modifica. Sin IDs: cobertura no resuelta, cero HTTP.
Feed reciente: devolver avisos de extremos con impacto confirmado o sin confirmar,
publicación y efecto textual. Cobertura sigue parcial aun sin coincidencias.
Caducado/futuro/sin timestamp: sin avisos presentados como actuales; causa y metadatos.
Sin demo local nueva: reutilizar fixtures UC-002 mediante selección explícita.
HTTPs hasta1MiB/2000 entidades, call15s, read10s, connect5s. Parser acepta camelCase
y snake_case, timestamps enteros numéricos o strings, FULL_DATASET únicamente.
Fallo completo ante payload inválido, no descartar silenciosamente entidades.
Texto plano acotado; sin interpretar instrucciones remotas ni abrir URLs del payload.

## Validation and complexity
Dominio/matching primero, data/HTTP después, VM/panel y grafo final. Pruebas
antes de lógica. Fixtures JSON sintéticos acotados para éxito/no relacionado/
caducado/fallo/malformed, más consulta pública real con hash. Sin cobertura de
servicio/trip ni horarios: S5 condicional no activado. Cero excepciones constitucionales.
