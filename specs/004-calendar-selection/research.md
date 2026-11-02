# Investigación UC-004 · 2026-11-02

## Proveedor y recurrencias
Decisión: consultar CalendarContract.Instances con rango en URI, y filtrar además
BEGIN>=inicio y BEGIN<inicio del día siguiente; excluir ALL_DAY, STATUS_CANCELED
y calendarios no visibles. Instantes UTC y día/zona explícitos permiten DST.
Alternativa descartada: Events.DTSTART apunta al comienzo de la serie, no a cada
ocurrencia. Investigación de solo lectura delegada conforme a speckit-plan.
[Instances](https://developer.android.com/reference/android/provider/CalendarContract.Instances).

Decisión: EVENT_ID+BEGIN identifica ocurrencia; título/nombre de calendario solo
para UI local. Guardar clave/fin esperado/día para revalidar mediante nueva consulta.
Si fin cambia o falta clave, retirar selección. No elegir automáticamente aunque
solo haya un evento. Proyección no incluye ubicación, descripción ni asistentes.
[Proveedor calendario](https://developer.android.com/identity/providers/calendar-provider).

## Permisos y cancelación
Decisión: READ_CALENDAR en manifest, launcher RequestPermission tras botón de UI;
rechazo explícito, opción Ajustes y comprobación de permiso al reanudar. Conceder
no autoriza compartir ni escribir. Permiso comprobado de nuevo en adaptador.
[Permisos runtime](https://developer.android.com/training/permissions/requesting).

Decisión: Dispatchers.IO + CancellationSignal conectado a cancelación coroutine;
Cursor.use incluso ante fallo. Null/fallo de proveedor distinto de Data(emptyList).
Alternativa: shortcut Instances.query no ofrece la señal requerida; no consultar
en Main ni confundir suspend con cambio de hilo.
[ContentResolver](https://developer.android.com/reference/android/content/ContentResolver).

## Pruebas y tamaño
Sin módulos ni librerías de producción nuevos. Añadir runner/dependencias de
pruebas instrumentadas a core-platform. Proveedor fake envuelto para fallos/races
y proveedor real en emulador con calendarios LOCAL sintéticos únicos. Escrituras
solo en tests mediante identidad shell y limpieza por IDs creados, nunca agenda ajena.
Rechazo/concesión/revocación también se verifican desde la app instalada; revocar
puede terminar el proceso. No declarar continuidad del VM tras muerte del proceso.
[UiAutomation](https://developer.android.com/reference/android/app/UiAutomation),
[Calendars](https://developer.android.com/reference/android/provider/CalendarContract.Calendars).
