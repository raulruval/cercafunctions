# Análisis de referencias

Consulta realizada el 1 de noviembre de 2026. Se han leído los repositorios
públicos en copias de investigación independientes, sin modificar sus originales.
Esta base no depende de sus paquetes Maven privados ni necesita tokens GitHub.

## Rocket-Beer

| Repositorio | Commit inspeccionado | Archivos relevantes |
| --- | --- | --- |
| [rocket-core-domain](https://github.com/Rocket-Beer/rocket-core-domain) | `54be723cc1bff4d413903a5b4c7b11093859b449` | `core-domain/.../usecase/UseCase.kt`, `functional/Either.kt`, `error/Failure.kt`, `di/CoreProvider.kt`, Gradle y README |
| [rocket-core-data](https://github.com/Rocket-Beer/rocket-core-data) | `db61206161c0f3099f0b884069d27c86539ab2ee` | `core-data-permissions/.../Permissions.kt`, `PermissionError.kt`, `di/CoreDataProvider.kt`, estructura y README |
| [rocket-core-presentation](https://github.com/Rocket-Beer/rocket-core-presentation) | `37aa4f7db6212d36637945cc8a5360acc3a83910` | `core-viewmodel/.../BaseViewModel.kt`, `Result.kt`, `core-navigation-compose/.../NavigatorManager.kt`, estructura y README |

La revisión está centrada en estos tres repositorios; no supone una auditoría
de todas las aplicaciones o repositorios de la organización.

### Qué conservamos y qué cambia

| Hallazgo concreto | Adaptación a esta POC |
| --- | --- |
| Dominio ya es biblioteca JVM y ofrece `UseCase`, `UseCaseSuspend`, `UseCaseFlow` con `operator invoke` | Conservamos invocación y contratos independientes; usamos clases concretas sin herencia obligatoria ni parámetros anulables universales |
| `Either` modela éxito/error y `Failure` permite extensiones | Mantener errores explícitos cuando haya fallos de negocio; evitar `Any?`, códigos en texto y dobles jerarquías genéricas antes de necesitarlas |
| `CoreProvider` usa un mapa mutable global de propiedades | Composición tipada con Hilt fuera de dominio; dependencias por constructor |
| `BaseViewModel` mantiene, cancela y reinicia un scope propio con dispatcher | `ViewModel` oficial y `viewModelScope`; estado inmutable expuesto mediante `StateFlow`, respetando cancelación |
| `Result` de presentación combina Loading/Success/Error | `HomeUiState` específico de pantalla; no llevar estados visuales al dominio ni a los DTO AppFunctions |
| `core-data-permissions` envuelve Dexter y callbacks con `suspendCoroutine` | Permiso solicitado desde UI mediante Activity Result API en la futura integración; comprobación defensiva y revocación en el adaptador Android |
| `CoreDataProvider` recibe Context para permisos | Context permanece en `core-platform`, con Application context; dominio nunca recibe Context ni Activity |
| Presentación contiene navegación y mapas GMS/HMS | Una sola pantalla, sin mapas ni navegación; se conservan las fronteras y se evita copiar módulos innecesarios |
| README requiere credenciales para paquetes GitHub y versiones antiguas | Referencia conceptual, código propio y dependencias públicas fijadas en catálogo |

Rocket-Beer aporta abstracciones de base, no una integración Renfe/calendario ya
resuelta. En particular, `rocket-core-data` inspeccionado se centra en permisos:
no se le atribuye un cliente ferroviario ni una capa de red inexistentes.

Para una evolución empresarial propondríamos contratos de negocio sin Android,
DI por constructor, adapters independientes para UI y AppFunctions, errores
estables, procedencia temporal de los datos y pruebas que ejecuten los mismos
casos de uso desde ambos consumidores. No se ha cambiado Rocket-Beer.

## Skill oficial de Google

[android/skills · device-ai/appfunctions](https://github.com/android/skills/tree/main/device-ai/appfunctions),
commit `6daa53ef27fa2aa87827d183a85c06cedceca833`, skill actualizada el 28-10-2026.
Se han leído `SKILL.md` y las referencias `context.md`,
`feature-discovery-analysis.md`, `implementation-configuration.md`,
`kdoc-refinement-optimization.md` y `adb-interaction-testing.md`.
Instalada mediante el instalador de skills de Codex en su directorio personal
de skills como `appfunctions`; no se incorpora una copia modificada al proyecto.

El descubrimiento selecciona dos futuras consultas ricas: estado/estimación del
trayecto y evaluación del compromiso. Su implementación, KDoc generado y prueba
de descubrimiento se aplazan hasta que la UI ofrezca esas capacidades.
Esta pausa sigue el alcance expresamente solicitado para la primera fase.

La documentación mezcla ejemplos anteriores con texto actualizado. Algunos
snippets aún muestran `@AppFunction` y `appfn:description`; el contrato actual
es `@AppFunctionDeclaration`, `appfn:instructions` e `isDescribedByKDoc` explícito.
Se contrastó con las [notas de AppFunctions beta01](https://developer.android.com/jetpack/androidx/releases/appfunctions).
No se introduce `AppFunctionConfiguration.Provider` ni `AppFunctionContext`.

## Ejemplos oficiales

[android/appfunctions](https://github.com/android/appfunctions), commit
`0bb367a2b7ef276218ac37f8eb45f6c9a81ca0cd`.

Leídos ChatApp README, catálogo Gradle, builds `app`/`shared`,
`BaseChatAppFunctionService`, `RecipientsRepository`, `MessageRepository`,
y Agent README, `run_privileged.sh`, `startAppFunctionTestingAgent`,
`ShellIdentityInstrumentation`, manifest y partes del orquestador.
El código actual prima frente al README: la API de ChatApp es `searchContacts`
con `contactType`, `fetchSendMessageDetails` y `sendMessage`; el README conserva
ejemplos antiguos llamados `send` y `filterType`.

Los scripts y JAR del wrapper Gradle se tomaron de ese ejemplo oficial, con sus
cabeceras de licencia. La distribución queda fijada a 9.6.1 con SHA-256 obtenido
del servicio oficial de Gradle. El resto del código de la POC es propio.
Se conserva la licencia Apache 2.0 del wrapper en `docs/licenses/gradle-wrapper-LICENSE.txt`;
esto no establece una licencia global para el código propio del proyecto.

La base usa las versiones del catálogo del ejemplo, salvo AGP actualizado de
9.3.1 a 9.3.3 por correcciones publicadas, y coroutines 1.10.2 fijada explícitamente.
Ver [compatibilidad AGP 9.3](https://developer.android.com/build/releases/agp-9-3-0-release-notes)
y [Kotlin integrado](https://developer.android.com/build/migrate-to-built-in-kotlin).
