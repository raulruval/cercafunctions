# Modelo UC-008
Proposal inmutable: contact(name,endpoint,display),message,proposedAtUTC,basis
(status,margin,arrival,meetingStart,source,scenario),code SHA256 de representación
canónica de esos campos. El código no es contraseña; liga confirmación a datos visibles.
OneShotSend: PENDING→CANCELLED o ATTEMPTED→SENT/UNKNOWN. Marcar ATTEMPTED antes de IO;
no transición de regreso a PENDING, no retry automático. Revalidación fallida→CANCELLED.
SDK read-only previo: QueryJourney,AuthorizedAssessment,searchContacts. Escritura
solo sendMessage tras confirmación exacta. SearchMessages cuenta coincidencias por
endpoint y texto exactos para verificar efecto; no persistir agenda/token en reportes.
