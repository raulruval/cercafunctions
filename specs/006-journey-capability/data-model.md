# Modelo público
queryJourney(departure:LocalDateTime,zone:ZoneId,scenario:String?=null,
utcOffset:String?=null,originStationId:String?=null,destinationStationId:String?=null).
scenario null→REFERENCE; valores REFERENCE/DEMO_NORMAL/DEMO_DELAYED/DEMO_UNKNOWN.
offset null o ZoneOffset válido; ambos IDs null o ambos válidos/distintos del catálogo.
Validar enum/sintaxis de offset/IDs antes de leer trayecto; no normalizar opciones.
Gap/solapamiento/salida pasada se validan en el caso de dominio compartido.

JourneyResponse: origin/destination,referenceDurationMinutes,departure:Instant,
arrival:Instant?,zone:ZoneId,source,scenario,observedAt:Instant,delayMinutes:Int?,
rail:RailSummary. Referencia no equivale horario/predicción ni retraso0.
RailSummary: status,consultedAt,sourceUrl,publishedAt?,relevantAlertCount,
unresolvedCount,coverage=ENDPOINTS_ONLY,failure?. No textos ni IDs del calendario.

JourneySharingRepository: available:Boolean, allowed:Flow<Boolean>,setAllowed.
False inicial; true solo tras habilitar sistema; revocación persistefalse primero.
Errores de configuración o lectura no abren gate. La revocación protege consultas
posteriores a su comprobación; no retracta datos ya entregados. Sin TTL inventado.
