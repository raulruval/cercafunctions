# Implementation Plan ·UC-006
Rama feat/006-journey-capability ·2026-11-03
## Summary / Technical context
Seis módulos actuales, Kotlin/JVM17, SDK37/min26, laboratorio API36+; AppFunctions
beta01, KSP2.3.10 y Hilt2.60.1 existentes. Sin framework de agente/Robolectric nuevo.
## Constitution Check
PASS: spec previa, dominio JVM, casos compartidos, datos mínimos, no minutos de
alertas, permiso IPC y consentimiento explícito, pruebas negativas y cancelación.
Solo UC-006 activo; no ChatApp ni calendario externo. Checklist8/10 se preserva;
petición de continuar autoriza construcción sin atribuir lectura al usuario.
## Structure / decisions
1. core-domain/sharing/JourneySharingRepository.kt: Flow<Boolean>, available y
   setter suspend, sin Android. Representa consentimiento de compartir trayecto.
2. core-platform/sharing/DataStoreJourneySharingRepository.kt: flag false por
   defecto en store existente, callback de sistema inyectado; orden fail closed.
3. core-appfunctions/BaseJourneyAppFunctionService.kt: único entry point abstracto
   Hilt; queryJourney ejecuta IO, valida inputs, consulta mismos casos y revalida
   consentimiento. No ViewModels, Activity ni diálogos. Errores tipados constantes.
4. core-appfunctions/JourneyResponse.kt: DTO anotados, KDoc inline y java.time;
   retorno acotado: llegada/evidencia y resumen cualitativo, sin título/agenda/contactos.
5. core-appfunctions Gradle aplica Hilt+KSP ambos compiladores; manifest registra
   servicio generado, assetsv2/schema y metadata instructions. Acceso trusted explícito
   con compat, disabled default. No AppFunctionContext/Provider legacy.
6. SharingViewModel/SharingPanel en presentation; HomeRoute recibe VM desde Activity,
   interfaz stateless botón contextual, disponible/cargando/error/activo explícitos.
   No referencias entre VMs. app/di/SharingModule aporta callback Manager nullable.
7. Tests: política DataStore, estado/UI, adaptador con casos reales reloj fijo,
   metadata generada, UI con grafo y IPC negativo entre UIDs. ADB descubre/invoca
   con proceso frío, defaults/disabled/consent/invalid/missing y fixture fijo.
## Verification / complexity
Red antes de implementar política/adaptador cuando comprobable; tests directos de
servicio no sustituyen IPC. Instrumentadas secuenciales; instalar app antes de
negativa del testhost. No tomar calendarios reales ni credenciales en evidencia.
Regresión de los cinco casos, APK/lint, guía mentor Flutter, converge y snapshot.
