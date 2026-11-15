# Contrato de consulta

`QueryJourneyArrival(input, scenario)` es suspend y público desde core-domain.
Lee repositorio antes de calcular: ausencia/fallo retorna estado específico.
REFERENCE respeta entrada y duración guardada, captura Clock una vez; demos usan
exclusivamente catálogo sintético, con salida mañana08:00 Europe/Madrid según Clock inyectado; entrada temporal visible y no editable en UI.
`tomorrow(zone)` devuelve siguiente fecha local; `fixture(scenario)` permite
mostrar datos demo sin dependencia de presentation hacia data.

Validación temporal: hueco → Gap; solapamiento sin offset → Ambiguous; offset
inválido → InvalidOffset; instante anterior al reloj aplicable → PastDeparture.
Sumar duración/retraso como minutos transcurridos. No fallback a cero ni datos reales.
Toda Estimated incluye procedencia e instante de consulta, incluso desconocida.

UI disponible solo tras configuración. «Consultar salida» confirma hora visible.
Botón Mañana se aplica en zona válida escrita; errores de formato no invocan dominio.
Mostrar salida/llegada con fecha y offset, zona IANA, consulta y horizonte.
Sin permisos nuevos ni AppFunctions hasta sus casos propios.
