# Probar UC-008
En este equipo se han compilado/instalado ChatApp y Testing Agent standardDebug.
Copias de esta ejecución: `C:\Users\rrva9\Documents\Codex\2026-11-01\qu\work\uc008-official`.
Puedes abrir sus subcarpetas ChatApp y agent como proyectos separados en Android Studio.
No hay que incorporarlas como módulos de Cercanías Copilot.

## 1. Instalación reproducible si necesitas repetirla
Requisitos: Android Studio/JBR, SDK37, Git, Python3 y emulador API36+ compatible;
probado Pixel_10_Pro API37.2. Internet para dependencias. No necesitas Bash/WSL ni
clave Gemini para la demo determinista. En PowerShell, desde C:\dev\cercafunctions:

```powershell
python scripts/setup-interop.py
```

Clona commit0bb367a2b7ef276218ac37f8eb45f6c9a81ca0cd en external/appfunctions,
añade Ana POC/ana.poc@example.invalid/uc008-ana y compila/instala APK oficiales.
Es el único cambio funcional del ejemplo: contactos de fixture; mensajes sin cambios.
No hace reset de directorios ajenos. `--samples-dir RUTA` permite reutilizar una
copia en ese pin; `--sdk RUTA` y `--java-home RUTA` adaptan instalaciones distintas.
Gradle puede descargar el JDK21 solicitado por el proyecto ChatApp; no cambies
el JDK global de Copilot. Si `python` no existe, instala Python3 o usa su ruta local.
En este equipo también puedes reutilizar el intérprete con el que se ha verificado:

```powershell
$python = 'C:\Users\rrva9\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
& $python scripts/interop_flow.py --help
```

En los comandos siguientes sustituye `python` por `& $python` si usas esa opción.

## 2. Preparar Copilot y una ocurrencia propia
Ejecuta Copilot desde Android Studio. Configura origen/destino de prueba y duración35.
Pulsa **Autorizar consulta del trayecto** (UC-006). Crear calendario de demostración:

```powershell
New-Item -ItemType Directory -Force external | Out-Null
python scripts/calendar-demo-fixture.py create --state external/uc008-calendar.json
```

Usa fecha/UTC impresas por el comando. En Calendario concede READ_CALENDAR, elige
esa fecha, zona UTC y la ocurrencia ficticia de09:00. En Compartir evaluación,
pulsa **Autorizar esta evaluación temporal** y **Copiar referencia**. Solo vale
30min o hasta el comienzo; nueva autorización reemplaza la anterior. El permiso
de calendario, opt-in de trayecto y autorización de evaluación son independientes.

## 3. Demo completa con confirmación real
Sustituye la fecha de ejemplo por la futura de tu fixture. Primero preview:

```powershell
python scripts/interop_flow.py --departure 2026-11-04T08:40 --zone UTC
```

Pega referencia cuando la pida (entrada sin eco). Consulta ambas capacidades,
resuelve Ana y presenta texto/destinatario/hora. No envía. Después:

```powershell
python scripts/interop_flow.py --departure 2026-11-04T08:40 --zone UTC --interactive
```

Revisa el destinatario **Ana POC**, su endpoint/correo de prueba y el texto exacto.
Enter, NO, cualquier código distinto o cerrar la entrada cancelan. Para aceptar,
escribe exactamente `ENVIAR ` seguido del código mostrado. No hay flag --yes;
--interactive rechaza stdin no interactivo. El consumidor vuelve a comprobar
evidencia/contacto antes de un único intento y verifica un mensaje por searchMessages.
Si hay timeout o UNKNOWN, no reintenta: comprueba ChatApp antes de decidir otra acción.
La protección de doble confirmación corresponde a esa sesión, no a reinicios del script.

Para ver el efecto abre ChatApp y Ana POC antes de matar su proceso. Los mensajes
viven en memoria del ejemplo y no se entregan por red. Reiniciar ChatApp los pierde.
08:40+35=09:15,15min tarde frente09:00, **fuenteREFERENCE**, no predicción de Renfe.
Con08:00 no se prepara mensaje. `--scenario DEMO_UNKNOWN` tampoco lo prepara;
usa mañana en Europe/Madrid con el reloj inyectado y no comparte horarios de reunión. `--contact-query Bob`
demuestra ambigüedad: exige aclarar en otra ejecución, nunca escoge el primero.

## 4. Testing Agent oficial en Windows
Después de instalarlo, ejecuta en PowerShell7:

```powershell
pwsh -ExecutionPolicy Bypass -File scripts/start-testing-agent.ps1
```

Reproduce el launcher oficial: allowlist acotada a paquete/firma y am instrument -w
vivo con EXECUTE_APP_FUNCTIONS. No basta abrir desde el icono, pm grant ni un manifest
de una app normal. `-Serial` y `-Adb` permiten elegir dispositivo/ruta. No ejecuta
set-temporary-caller ni convierte una app ordinaria en consumidor autorizado.

En **Debugger**, busca ChatApp, expande searchContacts, introduce query=Ana y
contactType=INDIVIDUAL; pulsa Run una sola vez. Debe devolver Ana POC. Selecciona
Cercanías Copilot para comprobar las dos funciones. queryJourney requiere su opt-in;
evaluateAuthorizedCommitment exige además la referencia UI y fecha/zona explícitas.
Los IDs generados usan BaseChatAppFunctionService; no copies los nombres antiguos
send/filterType del README de ChatApp: el contrato real es sendMessage/contactType.

Para una consulta manual de queryJourney, departure contiene year/month/dayOfMonth/
hour/minute/second/nanoOfSecond, zone contiene zoneID=UTC. El resto puede quedar null.
El Debugger permite invocar sendMessage directamente: no lo uses para la demo de
confirmación, porque no garantiza pausa humana ni ejecución única. Usa la CLI anterior.
No ejecutar UIAutomator u otra instrumentación mientras el agente está activo:
pueden desplazar su UiAutomation y perder la identidad adoptada. Para pararlo:

```powershell
pwsh -ExecutionPolicy Bypass -File scripts/start-testing-agent.ps1 -Stop
```

La entrada de allowlist del paquete de pruebas puede permanecer; no concede por sí
sola la identidad shell una vez detenida la instrumentación. El launcher oficial
está enlazado en research.md para inspeccionar esa configuración de laboratorio.

## 5. LLM opcional y limpieza
standardDebug puede configurarse con una clave Gemini en Settings, por el usuario.
No se ha configurado ni usado una clave en esta entrega. No conectes ChatApp de
escritura a una cadena LLM suponiendo que un prompt impide enviar: su Confirm action
solo cubre PendingIntent, no sendMessage. El flujo completo verificado usa fallback
determinista; una demo LLM de lectura se puede realizar solo con Copilot conectado.

Al terminar revoca ambas autorizaciones desde Copilot y, si procede, READ_CALENDAR.
Limpia únicamente la fixture creada por tu archivo de estado:

```powershell
python scripts/calendar-demo-fixture.py cleanup --state external/uc008-calendar.json
python -m unittest discover -s tests -p test_interop_flow.py
```

El almacén de ChatApp no es persistente. No borres datos del paquete ni calendarios
ajenos para limpiar esta POC. Evidencia y aceptación real en verification.md.
