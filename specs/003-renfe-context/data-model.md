# Modelo UC-003
RailStation(id,name); RailSelection(originId,destinationId): IDs distintos del catálogo.
AlertPeriod(start?,end?): al menos un extremo y start<end cuando ambos existen.
Selector(stopId?,hasOtherConstraints): AND conservado, OR en lista. Si se conoce
otro stop no coincide; solo stop conocido coincide; resto no resuelto.
RailAlert(id,text,effect,selectors,publication,impact). Fechas Instant; ausencia
no equivale a cero. publicación no implica afectación al viaje.
RailFeed(publishedAt?,alerts); FeedRead.Success o Failure(reason).
RailContext(status,consultedAt,sourceUrl,publishedAt?,alerts,unresolvedCount,failure?).
Status:AVAILABLE,UNRESOLVED,UNAVAILABLE,STALE,FRESHNESS_UNKNOWN,FUTURE_TIMESTAMP,DEMO_SKIPPED.
RelevantAlert contiene alerta y flag impactConfirmed. Sin campo minutos ni llegada
cuantitativa nueva: la referencia recibida permanece independiente.

UI: referencia→asociación explícita→consulta→contexto. Cambiar selección/referencia
cancela y elimina contexto. Mismo input tras recreación conserva VM. Reinicio de
proceso borra consulta/asociación efímera, conserva trayecto UC-001.
