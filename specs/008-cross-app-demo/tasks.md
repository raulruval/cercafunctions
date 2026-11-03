# Tasks UC-008
Petición de ejecutar autoriza construcción; checklist humano8/10 intacto. Sin hooks.
## Phase 1: Setup
- [x] T001 Investigar contratos/privilegios/confirmación oficiales en specs/008-cross-app-demo/research.md.
- [x] T002 Refinar spec/plan/modelo/contrato/quickstart en specs/008-cross-app-demo/ antes de código.
## Phase 2: Foundation
- [x] T003 Escribir pruebas red de propuesta inmutable y envío único en tests/test_interop_flow.py (PENDING→CANCELLED o ATTEMPTED→SENT/UNKNOWN, sin retry).
- [x] T004 Implementar Proposal/OneShotSend y preparación mínima en scripts/interop_flow.py con código ligado a destinatario y texto.
## Phase 3: US1 · Interoperabilidad revisada (P1)
Independiente: LATE15→Ana POC→preview; rechazo cero mensajes, confirmación exacta un mensaje local verificable.
- [x] T005 [US1] Implementar descubrimiento/transporte/CLI en scripts/interop_flow.py; preview por defecto, --interactive exige TTY y revalidación, stdin protegido sin --yes.
- [x] T006 [US1] Crear scripts/setup-interop.py y scripts/start-testing-agent.ps1 con pin oficial, fixture Ana añadida e instrumentación oficial Windows.
- [x] T007 [US1] Compilar/instalar APK oficiales y comprobar contratos descubiertos en specs/008-cross-app-demo/verification.md.
- [x] T008 [US1] Verificar SDK real ON_TIME/unknown/ambigüedad/ausencia/rechazo sin envíos y equivalencia de evidencia en specs/008-cross-app-demo/verification.md.
- [x] T009 [US1] Lanzar Testing Agent oficial y verificar discovery/consulta manual; documentar fallback LLM sin barrera/clave en specs/008-cross-app-demo/verification.md.
- [x] T010 [US1] Solicitar confirmación humana exacta y verificar un único sendMessage real y mensaje local mediante searchMessages/UI en specs/008-cross-app-demo/verification.md; si falta aprobación registrar pendiente.
## Phase 4: Polish
- [x] T011 Ejecutar pruebas proporcionales y completar guía Windows/manual y evidencia en specs/008-cross-app-demo/{quickstart,verification}.md.
- [x] T012 Documentar conclusiones Rocket-Beer y aprendizaje consumidor/proveedor en docs/agent-ready-conclusions.md, docs/android-for-flutter.md, docs/architecture.md y actualizar README/START_HERE/AGENTS/specs/README.md.
- [x] T013 Aplicar converge y cerrar snapshot con estado real en specs/008-cross-app-demo/verification.md.
## Dependencies / delivery
T001→T002→T003(red)→T004→T005→T006→T007→T008→T009→T010→T011→T012→T013.
Un US1/MVP; sin [P] entre operaciones del emulador. Investigación documental independiente
de build. No modificar módulos Android ni implementar mensajería propia. Envío requiere humano.
