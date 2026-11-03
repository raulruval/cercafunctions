# Empieza aquí
Estado al2026-11-03: UC-001–008 Verified; ocho snapshots uc-001-v1…uc-008-v1.
POC Android de seis módulos; proveedores Copilot y ChatApp oficial, consumidor de
laboratorio determinista y Testing Agent oficial. El usuario confirmó una escritura
sintética, que se verificó por IPC y en la UI de ChatApp. No entrega por red.

## Probar lo construido
En este equipo ChatApp y Testing Agent ya están instalados en Pixel_10_Pro API37.2.
Copias oficiales en C:\Users\rrva9\Documents\Codex\2026-11-01\qu\work\uc008-official.
Abre ChatApp→Ana POC para ver la prueba mientras siga vivo su proceso. Ambas
autorizaciones de Copilot y READ_CALENDAR se dejaron revocados; agente detenido.

1. [Guía UC-008](specs/008-cross-app-demo/quickstart.md): instalación reproducible,
   fixture/calendario, preview, confirmación exacta y Testing Agent Windows.
2. [Evidencia UC-008](specs/008-cross-app-demo/verification.md).
3. [Conclusiones Rocket-Beer](docs/agent-ready-conclusions.md).
4. [Arquitectura](docs/architecture.md) y [Android desde Flutter](docs/android-for-flutter.md).
5. [Tablero SDD](specs/README.md).

## Recorrido rápido
Configura trayecto35, autoriza consulta UC-006, elige ocurrencia ficticia09:00 UTC
futura y autoriza evaluación UC-007; copia referencia. En terminal:

```powershell
python scripts/interop_flow.py --departure 2026-11-04T08:40 --zone UTC
python scripts/interop_flow.py --departure 2026-11-04T08:40 --zone UTC --interactive
```

Sustituye fecha por la de tu fixture futura. Primero preview sin envío; luego revisa
destinatario/texto exactos y escribe el código solicitado solo si decides enviar.
La referencia se pide sin eco, no en argumentos. Referencia no es predicción Renfe.
Testing Agent standardDebug no necesita Gemini para Debugger. El flujo LLM de
escritura no garantiza gate de confirmación y no se ha ejecutado.

Spec/plan UC-008 en e5604a5;18 pruebas nuevas del consumidor más evidencia real.
Checklist humano8/10 se conserva; los tests/documentos no atribuyen revisión al usuario.
