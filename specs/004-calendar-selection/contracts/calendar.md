# Contrato calendario
CalendarRepository.hasReadAccess comprueba permiso actual; read(day) es suspend,
de solo lectura, no pide permisos ni depende de Activity. Data(empty) significa
consulta válida sin compromisos elegibles, no proveedor ausente ni rechazo.
GetCalendarDay(date,zone) devuelve inicios dentro del día, ordenados, sin duplicar
claves; defiende límites aunque repositorio devuelva solapamientos anteriores.
El adaptador excluye all-day/cancelado/no visible antes de mapear.
RevalidateCalendarSelection(selection) usa el mismo GetCalendarDay, busca clave
exacta y compara fin; nunca sustituye por otra ocurrencia. Si solo cambian título
o calendario legible se devuelve detalle actualizado. No calcula llegada.

Route registra RequestPermission y consulta solo tras botón o concesión de ese
botón. ON_RESUME comprueba acceso pero no dispara lectura. Rechazo ofrece Ajustes;
revocación y SecurityException retiran datos. Las funciones futuras no podrán
pedir permisos sin UI ni divulgar detalle sin autorización adicional.
