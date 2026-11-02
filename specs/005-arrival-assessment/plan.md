# Implementation Plan: UC-005 · Evaluar llegada
Branch:`feat/005-arrival-assessment` ·2026-11-02 ·[spec](spec.md)

## Summary / Technical context
Kotlin/JVM17, Compose/Material3, StateFlow, Coroutines y Hilt existentes; seis
módulos, sin dependencias nuevas. Comparar instantes con reloj inyectado y
casos de lectura UC-002/004, conservando referencia/fixture e incertidumbre.

## Constitution Check
PASS antes/después de diseño: spec previa a código, dominio puro y compartido,
sin minutos inferidos de alertas, agenda mínima, pruebas y cancelación, un caso
activo. Checklist8/10 se conserva; petición actual autoriza construir, no revisión humana.

## Structure / decisions
- core-domain/src/main/kotlin/com/cercafunctions/domain/assessment/:
  AssessmentModels.kt y AssessArrival.kt. Resultado Compared o Indeterminate con
  causa, evidencia y assessedAt. MeetingTiming solo inicio/fin/zona, sin títulos.
- AssessArrival recompone salida exacta y refresca QueryJourneyArrival. Contrasta
  trayecto/salida/llegada/zona/fuente/escenario/retraso; observedAt se puede renovar.
  RevalidateCalendarSelection justo antes de comparar. Sin llegada o selección no
  lee agenda; fuente no disponible/pasado/cambio/permiso tienen causas específicas.
- Clasificar llegada>reunión antes de redondear. Margen firmado Long: positivo
  floor, igualdad0, negativo −ceil. Sin Double ni APIs java.time de API superior.
- core-presentation/.../calendar/CalendarViewModel.kt: recibe AssessArrival y
  snapshot con setArrival; nuevo estado evaluación, job/token independientes.
  Editar/cambiar selección/revalidar/ON_RESUME retira evaluación. Error de calendario
  borra detalles/selección y conserva indeterminado en la misma transición.
- core-presentation/.../assessment/AssessmentPanel.kt: stateless, botón explícito,
  horas completas, evidencia, redondeo y simplificación estación–reunión=0.
  HomeRoute enlaza llegada al VM; HomeScreen slot después de llegada. Inputs se
  sincronizan también durante edición de trayecto. No inyectar un VM dentro de otro.
- app/di/CalendarModule.kt proporciona fachada de dominio; no nuevos módulos.
- core-platform/data/appfunctions no cambian lógica. Tests de dominio/VM/Compose
  y app con proveedor real/ficticio; no dependencia de Renfe ni agenda personal.

## Verification and complexity
Casos S1–S7, redondeos de1ns/59s/60s, zonas/medianoche, fuente fixture y desconocido,
configuración cambiada, permiso/agenda fallidos y cancelación. UI/grafo con fixture
LOCAL de SDK. Reutilizar utilidad calendario UTC y pruebas previas de proveedor.
Sin simulación oculta de fechas; demo fija no se mueve a mañana. Sin nueva capa
genérica ni persistencia/evaluación automática. UC-006 sigue sin iniciar.
