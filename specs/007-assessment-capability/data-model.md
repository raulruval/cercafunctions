# Modelo UC-007
GrantTarget(selection,expectedOccurrence) solo local; emisión exige revalidación
igual a detalles que vio usuario. GrantRecord: referenceHash,versionHmac,selection,
issuedWall,expiresWall,issuedElapsedMs,expiresElapsedMs,bootCount. Expira <=1800s
y <=BEGIN. Ningún rawtoken/título/calendario se persiste. Hash/HMAC64hex.
IssuedGrant(reference,expiresAt,meetingStart) solo memoria UI; GrantSummary solo
expiresAt/meetingStart. Token43 caracteres Base64URL de32 bytes; no normalizar.

GrantRepository: available,read,publish,revokeIf(hash),revokeAll. Publish sistema
antes de guardar; revocar datos antes de sistema. Error/cancelación no concede
nuevo grant. CAS evita invalidar grant sustituido por petición antigua.
GrantClock: snapshot(wall:Instant,elapsedMillis:Long,bootCount:Int). Monotónico y
boot no negativos; drift permitido5s, sin ampliar vencimiento.

AssessmentResponse: status,reason?,signedMarginMinutes?,departure?,arrival?,arrivalZone?,
meetingStart?,meetingZone?,source?,scenario?,observedAt?,assessedAt. Compared incluye
horarios mínimos; indeterminado no incluye horario de reunión. Sin origin/destination,
IDs de agenda, títulos, notas, participantes, correos, nombre de calendario o token.
