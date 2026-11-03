# Contrato del consumidor
python scripts/interop_flow.py --departure AAAA-MM-DDTHH:MM --zone UTC
Por defecto solo muestra preview; referencia por getpass o stdin. --interactive
habilita pregunta humana en TTY: ENVIAR <code>. No --yes, no envío con stdin no TTY.
Contact query Ana por defecto; varias coincidencias abortan y requieren consulta
más precisa en otra ejecución. No parámetro endpoint directo ni texto opaco del modelo.
Código y texto visibles se vuelven a verificar; modificación invalida confirmación.
Antes del IPC se consume intento. Resultado incierto requiere comprobar ChatApp y
nunca reintentar automáticamente. El consumidor no promete entrega remota.
