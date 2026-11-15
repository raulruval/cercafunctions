# Plan UC-009
Se conservan seis módulos. Domain incorpora EvaluateDemoJourney compartido por UI y
AppFunction, DemoSettingsRepository y CompareArrivalTiming extraído del comparador
existente. Reutiliza GetCalendarDay y CalendarContract reales. No necesita red.
Platform persiste escenario y consentimiento en DataStore (mismo singleton, claves nuevas).
Presentation añade DemoViewModel/Screen con permiso Android, selección de escenario,
tarjeta de reunión real, resultado, interruptor de divulgación y acceso a Avanzado.
App compone dependencias y muestra la demo por defecto; no instancia ViewModels
avanzados hasta abrirlos. Appfunctions añade evaluateDemoJourney sin parámetros,
habilitada por defecto y limitada a consumidores Android trusted, con guards de
consentimiento antes/después y DTO mínimo. Mantiene IDs y controles anteriores.

## Revisión de supuestos
El usuario exige calendario real. Se simplifica la autorización cambiando su alcance
a próxima reunión de mañana continuamente, explicado en UI; nunca se elimina.
La salida de demo se ajusta al compromiso real para reproducir puntual/tarde/desconocido
sin editar el calendario. El título solo aparece localmente. No se crean ni borran eventos.
No hay hooks de extensión registrados. No se atribuye revisión humana a documentos.

## Validación
Pruebas de dominio: tres escenarios, mañana/DST, ausencia de reuniones, errores y
revocación de permiso. Persistencia: reinicio, defecto seguro, escenario inválido.
ViewModel: refresco, selección y consentimiento. Frontera: DTO mínimo, denegación
y cancelación. Compilar suites JVM, APK, lint y tests Android; verificar registro e
IPC real sin enviar mensajes y comprobar visualmente la pantalla en el emulador.
