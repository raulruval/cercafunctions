# Contrato compartido
AssessArrival(estimate?,selection?) es suspend sin Android/Hilt. Inputs nullable
devuelven causa explícita, no excepción de negocio ni reuniones inventadas.
Recalcula llegada misma salida/offset/escenario y compara evidencia excepto
observedAt. Un cambio obliga a refrescar la consulta visible. No modifica UC-003.
Revalida calendario antes de cálculo; fallo no presenta horarios obsoletos como
compromiso válido. Cancelación se propaga. No salida de mensajería ni red.
Margen +10/0/−15 para08:50/09:00/09:15 ante reunión09:00. Positivos floor;
negativos ceil de magnitud (1ns tarde→−1min). UI conserva fracciones en horarios.
UI: actualizar inputs cancela y retira evaluación; pérdida/cambio de calendario
retira además selección/detalles y deja explicación indeterminada. Resultado
no persiste. Margen extra0 y tramo estación–reunión0 siempre explícitos.
