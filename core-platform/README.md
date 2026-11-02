# core-platform

Adaptadores Android detrás de contratos de dominio. UC-001 implementa
`DataStoreJourneyRepository`, con Preferences DataStore y actualización
transaccional de un único trayecto. Distingue ausencia, error de lectura y
configuración inválida; no borra datos ante un error. Hilt crea una instancia
de DataStore por archivo/proceso en `app/di/JourneyModule`.

La futura lectura CalendarContract se añadirá en UC-004. Este módulo no depende
de presentación o AppFunctions ni declara permisos de calendario actualmente.
