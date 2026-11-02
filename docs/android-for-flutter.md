# Android nativo desde Flutter y BLoC

## La primera pantalla

| Android en esta base | Equivalencia aproximada en Flutter | Diferencia relevante |
| --- | --- | --- |
| `@Composable HomeScreen` | Widget que recibe estado y callbacks | Funciones que describen UI; la recomposición puede ejecutar de nuevo su cuerpo. Evitar efectos y consultas ahí |
| `Column`, `Modifier.padding`, `Scaffold`, Material 3 | Column, Padding, Scaffold, Theme | Modifier encadena tamaño, layout e interacción; el orden modifica el resultado |
| `HomeRoute` | Widget contenedor con BlocBuilder | Recoge el flujo respetando el lifecycle Android y delega en la pantalla sin estado |
| `HomeViewModel` | BLoC/Cubit | Sobrevive a cambios de configuración, pero no a la muerte del proceso. No guarda Activity ni referencias a vistas |
| `StateFlow<HomeUiState>` | Stream de estado / BehaviorSubject | Flujo caliente con valor actual y conflación por igualdad; no es una cola de eventos. Exponer `StateFlow`, conservar `MutableStateFlow` privado |
| `viewModelScope.launch` | Inicio de tarea asíncrona desde BLoC | Scope asociado al ViewModel con cancelación estructurada automática al destruirlo |
| `suspend fun` | Función que devuelve Future | Suspend no crea un hilo ni mueve trabajo a IO. La cancelación es cooperativa y debe propagarse |
| `Flow` | Stream | Flow ordinario suele ser frío; StateFlow es caliente. No todas las variantes comparten semántica |
| Hilt `@Provides`, `@HiltViewModel` | Provider/RepositoryProvider o GetIt | Generación y verificación de grafo en compilación, scopes asociados a componentes Android |
| Módulo Gradle JVM / Android Library | Paquete Dart / plugin de plataforma | Frontera de compilación y dependencias real; Android Library produce AAR con manifest y recursos |

`HomeUiState` es un estado inmutable con contenido de lectura, borrador y
confirmación de guardado. `JourneyContent` es una sealed interface con Loading,
NotConfigured, Ready y Unavailable; se parece a variantes de estado de Cubit. El caso de uso no
sabe qué significa mostrar un spinner. Tampoco conoce Hilt o AppFunctions.

## Activity, Application, Context y lifecycle

`CopilotApplication` inicializa el contenedor Hilt por proceso. `MainActivity`
es una entrada del sistema, aloja Compose y aplica edge-to-edge; no es un caso
de uso. Android puede recrearla por rotación, configuración o recuperación de
proceso. No equivale exactamente a un `main()` ni al estado de un widget.

`Context` permite acceder a recursos y servicios del sistema. Un Context de
Activity está unido a su ciclo de vida; conservarlo en un singleton puede
provocar fugas. Los futuros adaptadores usarán Application context cuando
corresponda. El dominio no recibe ninguno.

`collectAsStateWithLifecycle` evita mantener la recogida activa en un estado de
pantalla inadecuado. El ViewModel conserva el borrador al recrear la Activity,
como ocurre al rotar. La configuración confirmada se recupera desde DataStore
tras morir el proceso; el borrador no confirmado puede perderse.

## Gradle, Hilt y compilación

`settings.gradle.kts` declara módulos y repositorios. El catálogo
`gradle/libs.versions.toml` fija plugins y librerías. Cada build de módulo
declara lo que puede importar; Gradle no es solo el equivalente de `pubspec.yaml`,
también produce variantes, recursos, manifest y tareas de test/lint/empaquetado.

AGP 9 integra Kotlin en módulos Android; aplicar además `kotlin-android`
duplica su configuración. Los módulos JVM sí usan `kotlin-jvm`.
El plugin Compose compila composables; KSP ejecuta los procesadores de Hilt.
En la fase AppFunctions también generará rutas, DTO y metadatos del servicio.

`app/di/JourneyModule` construye el repositorio y el caso de uso con `@Provides`.
`@Singleton` conserva el repositorio en el componente de aplicación. Las clases
de dominio y datos reciben parámetros por constructor, sin depender de Hilt.
`by viewModels()` obtiene la instancia generada para la Activity.

## Permisos, CalendarContract y servicios: próxima fase

`ContentResolver` accede a un ContentProvider mediante URI y proyecciones;
CalendarContract define las tablas y columnas del calendario. No necesitamos
un canal Flutter: este es el acceso nativo que un plugin envolvería.
La consulta bloqueante se moverá a IO y cerrará el Cursor con `use`.

La petición READ_CALENDAR se hará en UI mediante Activity Result API. El usuario
puede rechazarla o revocarla después; el adaptador debe comprobarlo cada vez.
Una AppFunction ejecutada sin interfaz no puede asumir que la Activity exista.

AppFunctions es una entrada IPC declarada al sistema, con servicio, permisos,
esquema y serialización propios. Una analogía útil es un adaptador externo que
llama al mismo caso de uso que BLoC, pero aquí otro proceso Android descubre y
ejecuta la capacidad. No se instancia un ViewModel desde el servicio y no se
accede a Compose para obtener resultados. El agente externo orquesta proveedores.

## Cómo inspeccionar lo construido

1. Abrir `MainActivity`, seguir `HomeRoute` y `HomeViewModel` hasta `GetHabitualJourney`.
2. Comprobar que `JourneyRepository` solo contiene tipos Kotlin.
3. Seguir el enlace Hilt hasta `DataStoreJourneyRepository` en `core-platform`.
4. Abrir el Preview de `HomeScreen`: funciona sin contenedor Hilt ni dispositivo.
5. Ejecutar tests JVM; después instalar el APK y rotar la pantalla para observar
   la separación entre Activity y ViewModel. `JourneyFlowTest` ya verifica recreación,
   conservación del borrador y cancelación de edición con la Activity real.


## Lo aprendido en UC-001

En Flutter podrías combinar un Cubit de formulario con un repositorio y
`shared_preferences`. Aquí ViewModel cumple ese papel y StateFlow publica el
estado. Compose recibe valores/callbacks; no conoce DataStore ni ejecuta escritura.

Preferences DataStore es almacenamiento asíncrono y transaccional. Escribimos los
tres campos en un solo `edit`: varios guardados concurrentes no mezclan origen
de uno y destino de otro. La UI confirma el éxito solo después de que termine.
Una instancia por archivo se mantiene con `@Singleton` en Hilt; `@ApplicationContext`
evita conservar una Activity. El dominio no conoce ninguna de esas APIs.

El permiso para guardar preferencias privadas lo aporta el sandbox de la app:
UC-001 no necesita un diálogo runtime ni acceso al calendario. Reiniciar el proceso
es distinto de recrear la Activity: la primera comprobación exige persistencia;
la segunda comprueba la continuidad del ViewModel y del borrador.

Los resultados tipados separan «nunca configurado» de «no se puede leer». No hay
un `catch` que devuelva vacío para cualquier fallo. La cancelación de una coroutine
se propaga y no se muestra como error del usuario ni éxito. El dominio normaliza y
valida; la presentación solo convierte texto a entero y traduce errores a recursos.

Al ejecutar UI tests se encontró Espresso 3.5.0 transitivo desde Compose. Se fijó
3.7.0 en los dos módulos de pruebas porque usa el acceso soportado a InputManager.
La BOM de Compose no versiona todas las bibliotecas externas de AndroidX Test.
Ese fallo de infraestructura se registra aparte de los fallos de comportamiento.

## Lo aprendido en UC-002

Dart `DateTime` agrupa fecha/hora con UTC o zona local del dispositivo. java.time
separa `LocalDate` (día), `LocalTime` (hora), `LocalDateTime` (sin zona), `ZoneId`
(reglas IANA), `ZoneOffset` (desplazamiento concreto) e `Instant` (punto temporal).
Una hora local puede no existir o repetirse con dos offsets. Por eso consultamos
`getValidOffsets`, exigimos elección si hay dos y sumamos duración en `Instant`.
Mostrar solo HH:mm perdería la fecha al llegar después de medianoche.

`Clock` es una dependencia, equivalente a inyectar un proveedor de tiempo en un
Cubit en vez de usar `DateTime.now()` por todas partes. El reloj real se construye
en Hilt; pruebas y demos usan tiempo fijo. Mañana es siguiente fecha local, no
`Duration(hours: 24)`. Ver [Clock oficial](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Clock.html)
y [ZoneRules](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/zone/ZoneRules.html).

`ArrivalViewModel` equivale a otro Cubit del panel. `viewModelScope` cancela al
destruir definitivamente su propietario; además cancelamos consultas al editar.
Un contador impide publicar respuestas antiguas. `collectAsStateWithLifecycle`
adapta StateFlow al estado Compose según lifecycle, parecido a la suscripción
que un BlocBuilder administra, con las reglas de Activity de Android.

`LaunchedEffect` se inicia de nuevo al recrear composición: no prueba que haya
cambiado el trayecto. El ViewModel compara el trayecto observado para conservar
consulta al rotar y retirarla tras guardar una duración distinta. Tras morir el
proceso se recupera solo el trayecto persistido, no la consulta efímera.

Sigue `MainActivity` → `HomeRoute` → `ArrivalRoute` → `ArrivalViewModel` →
`QueryJourneyArrival`. El dominio no importa Android ni Hilt; la futura función
podrá ejecutar el mismo caso sin componer pantalla o instanciar un ViewModel.
Ejecuta `QueryJourneyArrivalTest` para observar DST con Clock fijo y
`ArrivalFlowTest` para ver ensamblaje Hilt, formularios y recreación reales.


## Lo aprendido en UC-003
Un `Future` de HTTP en Dart y una función `suspend` no garantizan por sí solos
cancelar una conexión. `suspendCancellableCoroutine` adapta los callbacks de
OkHttp; `invokeOnCancellation { call.cancel() }` conecta el lifecycle con el socket.
Equivale a propagar una cancelación explícita al cliente desde un Cubit; cerrar
una suscripción de estado no basta. `viewModelScope` y el token de generación
impiden publicar una respuesta antigua después de cambiar estación o consulta.

HTTP es asíncrono en hilos de OkHttp. El parser corre en Dispatchers.IO; declarar
`suspend` no mueve trabajo bloqueante fuera del hilo principal automáticamente,
igual que `async` en Dart no crea un isolate para cálculos intensivos. En esta
POC el JSON acotado no justifica un motor adicional de procesamiento.

La frontera JSON tiene nombres camelCase/snake_case y épocas en strings; el
dominio recibe Instant, selectores y periodos tipados. Gson se usa como árbol,
no para instanciar por reflexión las data classes Kotlin: validamos presencia,
rangos y tamaño. Esto cumple el mismo papel que DTO + mapper desde un repositorio
Dart, manteniendo HTTP y formato remoto fuera del caso de uso.

`INTERNET` es un permiso normal declarado en Manifest; Android lo concede al
instalar sin diálogo runtime. READ_CALENDAR del siguiente caso sí requerirá
flujo de autorización de usuario. No hacen falta Activity ni Context en el
repositorio HTTP; por eso permanece JVM en core-data.

La selección explícita de IDs evita tratar nombres libres como identidad.
`FeedHeader.timestamp` informa cuándo se produjo el contenido; HTTP200 no lo
rejuvenece. Frescura del feed y periodo de afectación son cosas distintas. El
resultado permanece cualitativo y no altera ArrivalEstimate, por lo que UI y
futuros adaptadores de capacidades podrán consumir el mismo QueryRailContext.
Pruebas locales cubren HTTP200/503/redirect/tamaño/timeout/cancelación con servidor
loopback; las instrumentadas no dependen del estado del proveedor externo.
