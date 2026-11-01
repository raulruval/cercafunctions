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

`HomeUiState` es una sealed interface con Loading, NotConfigured, Ready y Error;
es comparable a estados de Cubit con variantes exhaustivas. El caso de uso no
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
pantalla inadecuado. El ViewModel sobrevive a la rotación; para recuperar datos
tras morir el proceso hará falta persistencia. El almacén vacío de esta base
todavía no proporciona esa persistencia.

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
3. Ver la implementación vacía en `core-data` y su enlace Hilt en `app`.
4. Abrir el Preview de `HomeScreen`: funciona sin contenedor Hilt ni dispositivo.
5. Ejecutar tests JVM; después instalar el APK y rotar la pantalla para observar
   la separación entre Activity y ViewModel. La rotación aún no tiene prueba automatizada.
