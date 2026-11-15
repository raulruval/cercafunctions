# Conclusiones: Rocket-Beer hacia proveedores de capacidades

UC-001–007 construyen comportamiento y una frontera de publicación; UC-008 es un
consumidor externo de laboratorio. No se han modificado los repositorios Rocket-Beer.
La referencia de capas sigue siendo útil si el dominio puede ejecutarse sin pantalla.

| Frontera | Reutilizar / evolucionar |
| --- | --- |
| rocket-core-domain | Mantener modelos/contratos/casos independientes. Aportar Clock y resultados explícitos; ninguna dependencia de ViewModel, Activity o SDK AppFunctions |
| rocket-core-data | Mantener mapeo de fuentes/repositorios. Hacer explícita procedencia, vigencia y ausencia de evidencia; un error de red no significa cero retrasos |
| rocket-core-presentation | Conservar estado UI y bindings de casos. Evitar que la regla de negocio o selección de repositorios viva exclusivamente en BLoC/ViewModel |
| Platform | Encapsular CalendarContract, permisos y reloj del sistema. Solicitar permiso en UI; comprobarlo también al ejecutar sin UI |
| appfunctions | Publicar contratos mínimos y estables mediante KSP, Hilt y errores acotados. Reutilizar dominio; no serializar StateFlow ni importar presentation |
| Consumidor/agente | Descubrir contratos, resolver ambigüedad y confirmar efectos. La autorización de Android y el consentimiento del proveedor son controles diferentes |

El módulo appfunctions de esta POC demuestra el patrón de adaptador, pero sus
funciones y DTO ferroviarios pertenecen al producto. Para reutilizar en la compañía,
extraer solo convenciones de build/KDoc, mapeo de errores y pruebas de contrato cuando
existan al menos dos proveedores reales. Evitar un framework genérico de permisos,
un mega-DTO o una dependencia transversal que reúna todos los dominios empresariales.

Una capacidad debe describir resultado, campos necesarios y limitaciones. Mantener
IDs publicados estables, revisar compatibilidad del esquema generado y probar
descubrimiento real. AppFunctions no convierte cada método interno en API pública.
El consumidor no debe adivinar que applicationId coincide con el paquete Kotlin.

La consulta rica se separa del efecto. READ_CALENDAR no es autorización de divulgar
agenda; una referencia temporal limita una ocurrencia y permite revocación. Una
confirmación de envío debe vincular destinatario y contenido exactos. Un prompt que
pida confirmar no impide que una herramienta se ejecute: hace falta una barrera
programática en el consumidor o proveedor. El agente oficial sirve para depurar;
su protección PendingIntent no es una confirmación general de mensajes String.

Las escrituras necesitan estados explícitos y reconciliación. La demo consume su
único intento antes de IPC y no reintenta un resultado incierto. Eso no constituye
exactly-once distribuido: un producto necesitaría idempotency keys aceptadas por el
proveedor y una consulta durable del efecto. No añadir un backend solo para fingirlo
en una POC cuyo ChatApp oficial conserva mensajes en memoria y simula red.

Pruebas recomendadas: reglas puras con reloj inyectado, fallos de adaptadores,
lifecycle/permisos, contratos generados, consumidor no privilegiado rechazado,
proceso frío y efecto real tras consentimiento. Separar tests deterministas de
evaluaciones LLM. Versionar spec/plan/tareas/evidencia y describir qué no se probó.
Métricas futuras: fallos por capacidad y versión, denegaciones/cancelación/latencia;
no registrar tokens, títulos de calendario o cuerpos de mensajes como telemetría.
