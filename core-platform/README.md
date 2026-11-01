# core-platform

Biblioteca Android sin adaptadores ejecutables en esta fase. Alojará la lectura
acotada de CalendarContract.Instances mediante ContentResolver y la implementación
del contrato de calendario definido en dominio. No depende de presentación ni
de AppFunctions. La UI solicitará READ_CALENDAR; el adaptador comprobará su
concesión y manejará revocación. No declaramos ni solicitamos permisos todavía.
