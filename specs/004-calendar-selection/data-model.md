# Modelo UC-004
- CalendarDay(date:LocalDate,zone:ZoneId): inicio local e inicio del siguiente día
  convertidos a Instant. Duración no fija24h; día omitido por zona puede ser vacío.
- OccurrenceKey(eventId:Long,begin:Instant): eventId>0. Identidad local de una
  ocurrencia, no de una serie entera ni de un título.
- CalendarOccurrence(key,end,title,calendarName): end>=begin; título/nombre solo UI.
- CalendarSelection(key,expectedEnd,day): begin dentro de intervalo del día,
  expectedEnd>=begin. Datos mínimos de sesión; no incluye título ni asistentes.
- CalendarRead: Data(occurrences), PermissionRequired, Unavailable(reason).
  CalendarFailure: PROVIDER_UNAVAILABLE, QUERY_FAILED, INVALID_DATA.
- SelectionValidation: Valid(currentOccurrence), MissingOrChanged,
  PermissionRequired, Unavailable(reason). Cancelación se propaga.

UI: acceso requerido/idle → consulta manual → loading → data(vacío/lista) o error.
Elegir lista → selección explícita sin validar → revalidar → válida o retirada.
Editar día/zona o perder acceso elimina lista/selección y cancela trabajo previo.
Estado en memoria de ViewModel, sin SavedStateHandle/DataStore.
