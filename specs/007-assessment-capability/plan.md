# Plan UC-007
Rama feat/007-assessment-capability ·2026-11-03
## Context / constitution
Seis módulos actuales, Kotlin JVM17, Compose/Hilt/Coroutines/DataStore y AppFunctions
beta01. Sin dependencias/módulos nuevos. PASS: dominio puro, scopes separados,
datos mínimos, no minutos inferidos ni envío, tests/lifecycle y autorización explícita.
Checklist humano8/10 conserva revisión pendiente; petición autoriza implementar.
## Structure / decisions
1. domain/authorization: modelos GrantRecord/GrantTarget/IssuedGrant/GrantSummary,
   GrantClock snapshot civil/monotónico/boot y GrantRepository privado; crypto JVM
   CSPRNG256bit/hash/HMAC, sin Android. Una referencia exacta por ocurrencia.
2. AssessmentAuthorizations: emitir/status/revocar/resolver, tiempos/versión y CAS.
   AuthorizedAssessment compone QueryJourneyArrival+AssessArrival con doble validación
   completa, sin duplicar comparación. Invalidación durable ante fallo observado.
3. platform/authorization: DataStoreGrantRepository en store existente, claves mínimas
   tipadas y atómicas; AndroidGrantClock usa SystemClock/BOOT_COUNT. app/di enlaza
   callback habilitación segunda función y casos; no introducir store paralelo.
4. presentation/authorization: AssessmentSharingViewModel/Panel; HomeRoute entrega
   snapshot selección/detalles, sin VMs cruzados. Emisión contextual/revocar/status;
   copia explícita. Rawtoken solo memoria, se retira ON_STOP; reautorizar rota.
5. core-appfunctions: AssessmentResponse con java.time/KDoc inline; segundo método
   evaluateAuthorizedCommitment en mismo servicio, IO/trusted compat/disabled.
   No depende de JourneySharingRepository para autorización de evaluación. Metadata
   distingue consulta de trayecto de evaluación temporal autorizada.
6. Tests de dominio tiempo/tokens/fingerprint/permiso/concurrencia; DataStore y
   clock, VM/Compose, DTO/adaptador, grafo/proveedor fixture e IPC entre UIDs.
7. scripts/appfunctions-smoke.py incorpora assessment con token por entrada stdin
   para no dejarlo en argumentos/historial; evidencia sin tokens/hash/eventIDs.
   Proceso frío real, permiso revocado y referencia revocada/incorrecta; regresión,
   guía mentor, converge y snapshot. UC-008 no se inicia.
