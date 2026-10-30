# Interfaz e ilustraciones de FinanQuest

El estilo compartido está en `ui/components/QuestVisualTheme.kt`. La selección de ilustraciones y sus componentes están en `QuestArtwork.kt`. Los archivos originales se empaquetan en `res/drawable-nodpi` y se cargan sin internet.

Pantallas actualizadas: Inicio, lista/creación/detalle de Metas, Cuentas, Movimientos, Misiones, Recompensas, Niveles, Mascota, Bienvenida, Inicio de sesión y Registro.

Los tipos de metas conservan sus identificadores originales en Firestore. La interfaz muestra una ilustración en lugar del emoji, de modo que las metas guardadas anteriormente continúan siendo compatibles. La creación y el detalle están separados en sus archivos dentro de `ui/metas`.

El resumen de Inicio consulta el período vigente de cada presupuesto mediante los mismos cálculos de la pantalla de Presupuestos.

Las pruebas `QuestVisualScreensTest` usan únicamente Auth y Firestore locales, por túneles `adb reverse` en los puertos 9099 y 8080. La comprobación del aporte necesita también el backend local en el puerto 3001, con su autenticación conectada al mismo emulador Auth y los datos conectados al emulador Firestore. No ejecutes estas pruebas contra la base real.

Las capturas opcionales se generan con el argumento de instrumentación `capturas=true` y se guardan en `Pictures/FinanQuest` del emulador. Se revisaron en resolución de pantalla 1080 × 1920, incluido el tema oscuro de Metas.

Los permisos de Firestore de producción se administran por separado. Este cambio de diseño no publica reglas ni cambia el proyecto Firebase de la APK.
