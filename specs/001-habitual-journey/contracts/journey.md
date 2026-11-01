# Contratos propuestos · UC-001

## Dominio

`JourneyRepository` ofrecerá lectura y guardado suspendidos con los resultados
de [data-model.md](../data-model.md). `GetHabitualJourney` y `SaveHabitualJourney`
son las entradas de negocio. Save recibe nombres y duración ya interpretada
como entero; valida y normaliza antes de llamar al repositorio. Los errores de
formato numérico pertenecen al formulario, sin duplicar las reglas de rango.

No filtrar Context, Preferences, Cursor, Activity ni anotaciones de serialización
al dominio. Un repositorio de pruebas puede simular errores con esos mismos
resultados. Cancelación se propaga, sin convertirla en Error de pantalla.

## UI

Sin configuración: acción Configurar. Configurada: resumen y acción Editar.
Edición: tres campos, Guardar y Cancelar. Guardar se bloquea durante la operación;
no cerrar por éxito hasta Saved. Error de validación muestra el campo afectado;
fallo de almacenamiento conserva el borrador. Fallo de lectura ofrece Reintentar
y no presenta un formulario vacío como si el usuario no hubiera configurado nada.

No hay contrato público AppFunctions en UC-001. Esta API interna se podrá
adaptar después sin exponer la configuración de escritura a un agente.
