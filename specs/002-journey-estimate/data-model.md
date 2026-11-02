# Modelo de datos UC-002

- `ArrivalScenario`: REFERENCE, DEMO_NORMAL, DEMO_DELAYED, DEMO_UNKNOWN.
- `DepartureInput`: local `LocalDateTime`, `ZoneId`, offset opcional. Un offset
  proporcionado debe pertenecer a los válidos de esa fecha/hora/zona.
- `JourneyFixture`: trayecto completo fijo, salida, reloj y retraso opcional.
  Retraso nulo significa estimación desconocida, nunca cero.
- `ArrivalEstimate`: trayecto usado, departure/arrival `Instant` (arrival nullable),
  zone, source REFERENCE/FIXTURE, scenario, observedAt, delayMinutes nullable.
  Referencia tiene arrival pero retraso real desconocido. Horizon: solo salida solicitada.
- `ArrivalResult`: Estimated, MissingJourney, StorageUnavailable, Gap,
  Ambiguous(offsets), InvalidOffset, PastDeparture. Cancelación no es resultado de negocio.

UI: borrador de fecha/hora/zona → consultar → resultado o error → cualquier edición
lo invalida. Modo demo bloquea campos y usa datos fijos. Selección de offset no
consulta automáticamente. Resultado no persiste; trayecto sí (UC-001).
