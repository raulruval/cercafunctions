# Cercanías Copilot

Esta fase crea solo el repositorio base. No iniciar SDD, nuevas funcionalidades,
calendario, Renfe, AppFunctions publicadas ni interoperabilidad sin una petición posterior.

- Mantener seis módulos y dependencias descritas en docs/architecture.md.
- core-domain y core-data son Kotlin/JVM; no importar Android, Compose, Hilt ni AppFunctions.
- UI y adaptadores de capacidades invocan los mismos casos de uso de dominio.
- No modificar Rocket-Beer ni incorporar sus artefactos privados.
- AppFunctions: leer la skill oficial android/skills, device-ai/appfunctions y sus referencias.
  Seguir API beta01 o posterior; contrastar ejemplos antiguos con las notas de versión.
- No publicar capacidades que la UI aún no ofrece. No conceder permisos desde un servicio.
- Identificar explícitamente fixtures; nunca extrapolar alertas actuales a mañana.
- No enviar mensajes sin confirmación explícita de destinatario y texto.
- No publicar credenciales, calendarios reales, datos privados o local.properties.
- Verificación base: ./gradlew :core-domain:test :app:assembleDebug :app:lintDebug.
- Explicar los cambios y las particularidades Android desde la experiencia Flutter/BLoC.
