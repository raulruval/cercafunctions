# Contrato UC-003
QueryRailContext(reference,selection): suspend; reference debe ser REFERENCE,
IDs deben estar en catálogo y ser diferentes. Si falta selección: UNRESOLVED
sin HTTP; demo: DEMO_SKIPPED sin HTTP. Publica catálogo por stations() sin Android.
Repo consulta un único endpoint público fijo y transforma JSON, preservando cancelación.
Clock capturado tras respuesta; TTL10min, skewfuturo60s. Fallos no son lista vacía.
Solo se prueban selectores de extremos, nunca línea/trip/ruta intermedia. Si hay
campos adicionales no demostrados, no dar match positivo. Rangos [start,end).
Sin impact: avisos potenciales con afectación sin confirmar; no calcula retrasos.
Sin coincidencias: texto explícito sin avisos de extremos, no «servicio puntual».
UI muestra fuente Renfe, consulta/actualización, estado, cobertura y periodos UTC.
Resultado no persiste ni refresca automáticamente. No divulga ruta a servidor:
GET global sin query de estaciones; filtrado local. Solo permiso INTERNET normal.
