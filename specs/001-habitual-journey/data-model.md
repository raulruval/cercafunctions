# Modelo y estados · UC-001

Implementado en UC-001 el 2026-11-02; ver verification.md para su validación.

| Elemento | Datos / invariantes |
| --- | --- |
| HabitualJourney | origen/destino no vacíos y distintos ignorando mayúsculas tras trim; duración entera 1–1440 |
| JourneyReadResult | NotConfigured, Configured(journey), Unavailable(reason) |
| JourneySaveResult | Saved(journey), InvalidInput(fields), Unavailable(reason) |
| JourneyDraft | cadenas introducidas y errores por campo; estado saving para evitar doble envío |

Unavailable distingue fallo de lectura/escritura y datos almacenados inválidos
con códigos acotados, sin rutas o excepciones de plataforma. Persistir campos
normalizados en una única actualización. Ningún campo → NotConfigured;
algunos campos o valores inválidos → Unavailable, nunca una configuración válida parcial.

Transición visible: Loading → NotConfigured / Configured / Unavailable.
Edición: Draft → Saving → Saved / InvalidInput / Unavailable.
Cancelar edición vuelve al valor confirmado; fallo permite reintentar el borrador.
Cierre durante escritura puede dejar versión anterior o nueva completa; la lectura
posterior determina cuál. No se garantiza persistencia de cambios sin confirmar.
