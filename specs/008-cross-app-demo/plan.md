# Plan UC-008
## Technical context
Seis módulos Android sin cambios. Consumidor de laboratorio Python3 stdlib/unittest,
PowerShell para build/launch Windows y APK oficiales AppFunctions beta01. API37.2
existente, SDK/JBR Android Studio. Sin claves o dependencias Python nuevas.
## Constitution check
PASS antes/después: spec/plan previos; mismos dominios UC-006/007 por IPC; fixtures
y referencia explícitas; divulgación mínima/confirmación humana; pruebas trazables;
sin backend/framework genérico. Checklist8/10 intacto, petición autoriza construir.
## Decisions / scope
1. scripts/setup-interop.py clona pin fuera de Git producto en external/appfunctions,
   añade fixture Ana con guardia de contenido, configura SDK y compila/instala ambos APK.
2. scripts/start-testing-agent.ps1 replica privilegios oficiales en Windows y mantiene
   instrumentación en segundo plano oculto; documenta parar/restaurar laboratorio.
3. scripts/interop_flow.py separa Proposal/OneShotSend de transporte Sdk; CLI preview
   por defecto, --interactive exige TTY y código exacto. Referencia getpass/stdin,
   discovery usa DEVNULL. Sin guardar token, propuesta o agenda en archivos por defecto.
4. Descubrimiento de los métodos necesarios (sin calls/wallpaper). QueryJourney requiere
   opt-in propio; evaluación exige referencia UI propia. Llegadas deben coincidir;
   revalidación antes de envío evita usar evidencia alterada durante la confirmación.
5. tests/test_interop_flow.py protege no retraso/unknown/ambiguo/rechazo/edición/
   cambio de evidencia/dobleconfirm/errorincierto; SDK real comprueba efectos del ejemplo.
6. Evidencia saneada y quickstart con pasos de instalación, demo determinista y Debugger
   oficial. LLM limitado por credenciales/barrera; no prometer autonomía no verificada.
7. docs/agent-ready-conclusions.md cierra recomendaciones Rocket-Beer; guías y tablero
   registran estado real. Converge antes de commit/tag; aceptación humana pendiente se
   distingue de tests automáticos y no se marca Verified hasta comprobar efecto real.
## Files / phases
Setup/research → tests/consumidor → instaladores/ejemplos → SDK/agente → documentación
y cierre. Un US1 P1; emulador secuencial, sin paralelizar instrumentaciones.
