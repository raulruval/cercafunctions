# Modelo UC-005
ComparedArrival: evidence ArrivalEstimate renovado, MeetingTiming(start,end,zone),
status ON_TIME/LATE, signedMarginMinutes:Long, assessedAt:Instant. ON_TIME>=0;
LATE<0. El signo se decide por Instant antes de cuantizar.
AssessmentResult: Compared(value) o Indeterminate(reason,evidence?,assessedAt).
Reasons: MISSING_ARRIVAL, MISSING_SELECTION, UNKNOWN_ARRIVAL, ARRIVAL_CHANGED,
ARRIVAL_UNAVAILABLE, DEPARTURE_PASSED, CALENDAR_CHANGED, CALENDAR_PERMISSION,
CALENDAR_UNAVAILABLE. No título ni asistentes en resultado.
Evidence conserva source/scenario/observedAt/zone/departure/arrival/delayMinutes.
No añade calidad REAL inexistente ni garantía de puntualidad; sin TTL inventado.
UI conserva resultado solo hasta cambio de entrada/retorno a primer plano. No
SavedState ni DataStore. Revalidación no garantiza atomicidad del proveedor.
