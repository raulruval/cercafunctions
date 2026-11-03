# Probar UC-007
API36+ compatible; imagen probada37.2. Instalar APK. Crear fixture propia de calendario
con scripts/calendar-demo-fixture.py create; elegir fecha/UTC impresas y ocurrencia09:00.
READ_CALENDAR desde panel Calendario; configurar trayecto35. Autorizar evaluación
de esa ocurrencia desde nuevo panel y copiar referencia explícitamente.
La autorización de trayecto UC-006 puede permanecerfalse.

Cliente SDK: scripts/appfunctions-smoke.py assessment --departure 2026-11-04T08:40 --zone UTC.
Pegar referencia por stdin, no guardarla en args/archivos. Resultado esperado15min
tarde respecto09:00, con fuenteREFERENCE; mismo que UC-005 local. Revocar y repetir:
Denied sin horarios. Token erróneo/expirado idem. Nueva autorización rota anterior.
No versionar token/clipboard/capturas con referencia; limpiar fixture propia al acabar.

Pruebas: dominio/data/plataforma/VM; instrumentadas secuenciales presentación,
app, reinstalar app, core-appfunctions (testhost otro UID). UTP puede desinstalar.
SDK positivo es privilegiado; no afirmar que cualquier app puede ejecutar.
Reloj/reboot/TTL se verifican inyectados, sin alterar reloj real del dispositivo.

Fecha del comando solo ilustrativa: sustituir por la fecha UTC futura que imprimió
la fixture. Ejecutar el cliente en terminal interactivo; solicita referencia sin eco.
No pasar tokens como argumento PowerShell. UTP puede desinstalar el proveedor:
reinstalar APK antes del smoke SDK y configurar/autorizar otra vez si es necesario.
El sistema puede devolver1002 al estar deshabilitada; autorización inválida dentro
del método produce1000. Portapapeles conserva la copia explícita según Android;
revocar invalida la referencia aunque esa copia permanezca.
