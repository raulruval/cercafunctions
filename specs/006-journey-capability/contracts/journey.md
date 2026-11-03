# Contrato de capacidad
Una sola función queryJourney; identidad final se obtiene del XML/KSP/descubrimiento,
no se adivina nombre de servicio. Acceso ANDROID_TRUSTED, compat enforcement true,
isEnabled=false, autorización de compartir persistida y comprobada dos veces.
Inputs/DTO en data-model.md. Sin permisos nuevos para proveedor, sin READ implícito
para agentes. Descripción y metadatos no son controles de autorización.

Errores: denied si falta consentimiento; invalid argument para enum/offset/IDs/
gap/overlap no resuelto/salida pasada; element not found si no hay trayecto;
app unknown acotado si falla almacenamiento/configuración o excepción inesperada.
CancellationException se propaga. Fallo ferroviario produce summary UNAVAILABLE
con fallo público conocido; no inventa llegada operativa ni cambia cálculo UC-002.
No exportar exception.message, valores de inputs, agenda ni contactos en errores.
Con mismos repositorios/reloj/salida coincide ArrivalEstimate de UI; summary
conserva status/fechas/recuentos del mismo QueryRailContext. Fixtures solo explícitos.
