# Investigación UC-003 · 2026-11-02

## Fuentes y comprobación real
Decisión: [Incidencias y avisos](https://data.renfe.com/dataset/incidencias-avisos)
mediante https://gtfsrt.renfe.com/alerts.json, sin autenticación. Catálogo anuncia
actualización cada20s, pero el 2026-11-02T11:46:52Z respondió HTTP200/69 entidades
con header.timestamp=1791358372 (2026-10-07T07:32:52Z). Se clasifica caducado.
HTTP Date no sustituye timestamp del contenido. La comprobación del APK detectó
además69 entidades/68 IDs únicos: AVISO_518187 repetido con contenido diferente.
La validación estructural rechaza ese feed completo como INVALID_FORMAT, conservando
la fecha de cabecera. No se decide last-wins ni se oculta la inconsistencia.
La antigüedad y la incompatibilidad son hallazgos separados; no se presenta vigente. Ver live-probe.json con hashes.

Alternativa cuantitativa: [trip updates](https://data.renfe.com/dataset/horarios-viaje-cercanias)
sí respondió fresco (353 entidades, timestamp1793619997); no prueba qué tren usa
el usuario. [GTFS estático](https://data.renfe.com/dataset/horarios-cercanias)
descargado:16.5MB comprimidos,138395 trips,1147 estaciones. Calendar/stop_times
permitirían un incremento de horarios, pero implicaría verificación de servicio,
fecha, secuencia, zona y horas >24. No se añade ahora. Se extraen solo ID/nombre
de estaciones, no una ruta o cobertura inexistente. El snapshot se regenera con
scripts/update-renfe-stations.py; sin descargar ZIP en cada consulta móvil.

## Semántica
Decisión: unión de selectores, conjunción de campos dentro de cada selector.
Solo stopId conocido y sin otras restricciones demuestra coincidencia de extremo.
Otros campos preservan incertidumbre; no basta conocer una parte del selector.
Periodos [start,end) abiertos; vacío/invertido es inválido. impact_period permite
confirmar coincidencia temporal, active_period/communication_period se muestran
como publicación. Sin impacto no se extrapola certeza temporal.
Fuente: [GTFS-RT](https://gtfs.org/documentation/realtime/reference/).
Investigación de solo lectura delegada conforme a speckit-plan, contrastada con feed.

Decisión:10min de frescura y tolerancia60s futura (política POC, no SLA Renfe).
Alternativa descartada: HTTP200/ausencia de alertas implica puntualidad.
Referencia: [buenas prácticas GTFS](https://gtfs.org/documentation/realtime/realtime-best-practices/).

## Implementación
OkHttp5.5.1 y Gson2.15.0 fijados, disponibles en Maven Central y releases oficiales.
Callbacks HTTP adaptados a suspensión con cancelación de Call; parseo fuera de Main.
Gson árbol explícito: sin reflexión sobre modelos Kotlin ni plugin adicional.
1MiB/2000 entidades, timeout15s, sin redirects, caché persistida ni polling.
Validación estructural del subconjunto consumido, no un validador GTFS completo.
Alternativas: Retrofit innecesario para un GET; protobuf añade generación/bindings;
JDK HttpClient no está disponible uniformemente en Android26. Sin nuevos módulos.
Fuentes: [OkHttp](https://github.com/square/okhttp), [Gson](https://github.com/google/gson/releases).

## Atribución
Origen de los datos: Renfe Operadora. Licencia CC BY4.0 indicada en catálogo.
Se conservan fuente y actualización en UI/documentos. No se implica respaldo.
[Condiciones publicadas](https://data.renfe.com/legal).
