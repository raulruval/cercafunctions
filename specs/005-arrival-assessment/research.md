# Investigación UC-005 ·2026-11-02
Decisión: comparar Instant antes de redondear. Duración positiva temprana→minutos
completos floor; duración positiva tardía→ceil si segundos restantes o nanos.
Alternativas descartadas: Double, convertir todo a nanos (overflow) y clasificar
por minutos ya truncados (−1ns se convertiría incorrectamente en cero).
[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html).
Investigación read-only delegada conforme a speckit-plan y contrastada con modelos.

Decisión: fachada suspend AssessArrival recompone misma consulta temporal desde
Instant+zona/offset del snapshot; ejecuta QueryJourneyArrival y valida que no cambió
la evidencia salvo timestamp de recálculo. Después RevalidateCalendarSelection.
Así reutiliza reglas vigentes, incluidos pasado/configuración/cancelación, sin
confiar en datos de UI ni inventar TTL de una referencia. Fixture fija no expira.

Decisión: CalendarViewModel coordina selección y evaluación del mismo compromiso
recibiendo llegada desde HomeRoute. Mantener un único dueño de selección permite
retirar detalles e informar indeterminado atómicamente cuando evaluar detecta
revocación/cambio. No añadir otro ViewModel con referencias cruzadas ni event bus.
Cálculo/validación reside en dominio; ViewModel solo traduce resultados a estado.

Decisión: salida pública de dominio con horarios/zonas/evidencia, sin título ni
calendario legible. No hay acciones de envío ni permisos de divulgación nuevos.
Límite de Instances documentado en UC-004 sigue aplicando; no se congela proveedor.
Horas completas ISO_OFFSET_DATE_TIME preservan segundos/fracciones y medianoche.

## Estaciones solicitadas
Catálogo GTFS extraído el2026-11-02:18000 Madrid-Atocha Cercanías y17000
Madrid-Chamartín-Clara Campoamor. Son los IDs usados en el selector de avisos,
no una afirmación de horario o cobertura integral. Fuente de snapshot:
[GTFS Renfe](https://data.renfe.com/dataset/horarios-cercanias), stops.txt.
