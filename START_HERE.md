# Empieza aquí

La POC termina con **UC-001–009 implementados**. Empieza por el [README](README.md)
para conocer el producto y por la [guía de demo](docs/demo-simple.md) para ensayarlo.

## Recorrido actual

1. Abre Copilot, concede lectura del calendario y comprueba la primera reunión con
   hora de mañana. La zona es la del dispositivo; no hay fechas fijas que copiar.
2. Selecciona Puntual, +15 min o Sin datos. Solo el tren y su salida son sintéticos;
   el calendario se consulta mediante CalendarContract real.
3. Activa el interruptor de consulta del agente para compartir horarios y resultado.
4. Ejecuta `evaluateDemoJourney()` sin parámetros desde Debug del Testing Agent.
   Para lenguaje natural, configura Gemini API y utiliza Agent Demo.

El interruptor persiste hasta apagarlo. No comparte títulos, notas, asistentes ni
IDs de calendario. El agente debe ser un consumidor autorizado por Android.
No hacen falta scripts de eventos, códigos de estaciones ni referencias temporales
para este recorrido. Si no hay reunión, créala desde tu app de calendario.

## Guías según lo que quieras revisar

- [Demo actual, fuentes y alternativas del agente](docs/demo-simple.md).
- [UC-009: contrato](specs/009-simple-demo/spec.md) y [evidencia](specs/009-simple-demo/verification.md).
- [Avanzado / UC-008](specs/008-cross-app-demo/quickstart.md): referencias acotadas,
  consumidor determinista, revisión exacta y ChatApp oficial.
- [Arquitectura](docs/architecture.md), [nombres de módulos](docs/module-organization.md)
  y [equivalencias con Flutter](docs/android-for-flutter.md).
- [Tablero SDD](specs/README.md) y [conclusiones Rocket-Beer](docs/agent-ready-conclusions.md).

## Reproducir el laboratorio

Consulta [setup](docs/setup.md). Instala ChatApp y Testing Agent desde el repositorio
oficial `android/appfunctions`, separado de este APK. Se fija la revisión utilizada
en la guía UC-008. Crea el contacto ficticio Ana POC según esa guía si quieres probar
la búsqueda/mensajería; el ejemplo guarda sus mensajes en memoria local.

Después de reiniciar el emulador, desde la raíz de este proyecto:

```powershell
.\scripts\start-testing-agent.ps1 -Serial emulator-5554
```

Para detenerlo, añade `-Stop`. No ejecutes pruebas instrumentadas simultáneamente.
No hay garantía de que un modelo complete cada intento: la evidencia distingue
pruebas deterministas, ensayo LLM y pasos no comprobados. La app no envía mensajes.

## Historial

Las etiquetas `phase-0-base`, `sdd-inception-v1` y `uc-001-v1`…`uc-008-v1`
conservan los incrementos previos. Los módulos actuales son `app`, `domain`,
`data`, `platform`, `presentation` y `appfunctions`; las referencias históricas
no se reescriben. Fecha de cierre documental: 2026-11-15.
