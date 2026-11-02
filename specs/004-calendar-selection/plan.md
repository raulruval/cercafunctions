# Implementation Plan: UC-004 · Selección de calendario
Branch:`feat/004-calendar-selection` ·2026-11-02 ·[spec](spec.md)

## Summary / Technical Context
Panel Compose independiente que consulta un día local mediante CalendarContract,
selecciona una ocurrencia y la revalida. Kotlin/JVM17, Android26+, Hilt/StateFlow,
coroutines, Activity Result API existentes. Sin dependencias nuevas de producción.
core-platform añade instrumentación con runner AndroidX ya fijado en catálogo.
Solo READ_CALENDAR en APK de producción; acceso WRITE/fixtures solo tests.

## Constitution Check
PASS antes/después de diseño: spec previa, seis módulos y dominio puro, misma
API de casos para futuros consumidores, sin compartir/escribir/persistir agenda,
cancelación y pruebas reales, un incremento activo. Checklist8/10 preservado;
la petición actual autoriza construir, no representa revisión humana de documentos.

## Structure and decisions
- core-domain/src/main/kotlin/com/cercafunctions/domain/calendar/: modelos,
  CalendarRepository, GetCalendarDay y RevalidateCalendarSelection. Rango local
  [inicio,siguienteinicio), selección por clave+fin esperado+día. Duplicados de
  clave/inconsistencia son error, nunca otra reunión elegida silenciosamente.
- core-platform/src/main/kotlin/com/cercafunctions/platform/calendar/:
  AndroidCalendarRepository(ContentResolver, comprobación de permiso, dispatcherIO).
  URI Instances con rango, proyección mínima/filtro visible; defensa adicional en
  mapper y dominio. Cursor cerrado y CancellationSignal conectado a coroutine.
- core-presentation/src/main/kotlin/com/cercafunctions/presentation/calendar/:
  CalendarViewModel, CalendarPanel stateless, CalendarRoute permisos/lifecycle.
  Campos ISO de fecha/zona, consulta explícita, lista y selección/revalidación.
  Edición cancela y retira datos; retorno sin permiso borra lista/selección.
- home/HomeScreen: slot de calendario fuera de dependencia del trayecto, oculto
  durante edición de trayecto. app/MainActivity y di/CalendarModule ensamblan.
- app/manifest READ_CALENDAR; core-data y core-appfunctions no cambian.
- scripts/calendar-demo-fixture.py: utilidad opcional SDK para crear/limpiar un
  calendario LOCAL sintético UTC, identificado por cuenta única. Estado local
  ignorado por Git. No añade WRITE al APK ni modifica calendarios ajenos.

## Session and safety
ViewModel conserva selección al recrear Activity; sin persistencia tras proceso.
Revalidación vuelve al proveedor y requiere clave/fin sin cambios; título puede
actualizarse localmente. Al volver a primer plano se retira marca de revalidada;
no se afirma atomicidad con modificaciones posteriores. UC-005 deberá revalidar
antes de evaluar. No se añade una autorización de compartir implícita.
Proveedor inaccesible/query fallida/datos inválidos/permiso faltante distintos de vacío.
No abrir intents de edición, consultar asistentes ni enviar datos de agenda a red.

## Verification
Pruebas dominio con Clock/fechas fijas, Madrid23/25h y cambio de zona. Adaptador
instrumentado: cursor/races/cancelación y proveedor real con RRULE, EXDATE,
eventos mismo título, all-day/cancelado, límites del día y selección borrada/movida.
VM/panel: no autoelegir, permiso denegado/revocado, selección explícita y estados.
App/Hilt: consulta local real, recreación y revalidación sin necesitar una cuenta.
Instrumentadas secuenciales en emulador API37.2. Capturas solo fixtures ficticios.
No pruebas que dependan de calendarios personales o red. Sin sobrecarga de arquitectura.
