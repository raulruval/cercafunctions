# Investigación UC-002 · 2026-11-02

## Tiempo y pruebas
Decisión: `Clock` inyectado y una captura de `instant()` por consulta; mañana es
la fecha local + un día. Razón: reproducibilidad y cambios de hora. Alternativas:
reloj global (no controlable) y sumar 24 horas (no equivale al siguiente día local).
Fuentes primarias: [Clock](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Clock.html),
[LocalDate](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/LocalDate.html).

Decisión: `ZoneRules.getValidOffsets` antes de construir el instante. Cero offsets
rechaza hora inexistente; dos requieren elección; offset inválido se rechaza.
Duración transcurre en `Instant`, presentación conserva zona y fecha completa.
Alternativa descartada: `atZone` sobre entrada local, que resuelve conflictos automáticamente.
Fuentes: [ZoneRules](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/zone/ZoneRules.html),
[Instant Android](https://developer.android.com/reference/java/time/Instant).
Investigación delegada de solo lectura conforme a la skill de planificación.

## Procedencia y tamaño
Decisión: catálogo de fixtures en core-data tras un contrato pequeño de dominio.
Cada demo fija ruta, salida, zona y reloj; nunca aplica un retraso ficticio al trayecto real.
Referencia usa exclusivamente la duración guardada. Desconocido conserva llegada nula.
Sin nuevas dependencias/módulos: java.time disponible desde minSdk 26. Sin Renfe hasta UC-003.
Alternativa: reloj real para demos haría caducar escenarios y rompería reproducibilidad.

## Estado UI
Decisión: ViewModel propio para consulta, panel Compose dentro del scroll existente.
Entradas fecha ISO, hora HH:mm y zona IANA; botón Mañana y consulta explícita.
Cambiar entrada, modo o trayecto invalida resultado y cancela consulta anterior.
ViewModels sobreviven recreación de Activity; la consulta no se persiste tras muerte de proceso.
Sin nuevos destinos de navegación ni selectores complejos para esta POC.
