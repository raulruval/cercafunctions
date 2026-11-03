# Contrato externo
evaluateAuthorizedCommitment(reference:String,departure:LocalDateTime,zone:ZoneId,
scenario:String?=null,utcOffset:String?=null):AssessmentResponse.
REFERENCE por defecto; demos explícitos conservan toda su fecha/reloj fijos.
Ninguna emisión/listado de autorizaciones o IDs de evento por IPC. Reference se
emite en UI y llega al agente solo por acción del usuario. Caducidad30min/comienzo.

Uniforme AppFunctionDeniedException para referencia incorrecta/revocada/caducada,
permiso/calendario/versión/autorización no disponibles; sin datos. Formato de
salida inválido tras token válido: InvalidArgument sin horarios. Cancelación
se propaga. Resultado UC-005 indeterminado de llegada conserva causa/fuente mínima,
sin divulgar reunión. Comparación de llegada a estación: tramo y margen adicional0.
Autorización propia, no reutilizar flag de trayecto UC-006. No garantiza instante
atómico global del proveedor ni revoca información ya entregada.
