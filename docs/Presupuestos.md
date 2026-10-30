## Presupuestos y categorías

Las pantallas están en `ui/presupuestos`, la persistencia en `PresupuestoRepository` y el estado en `PresupuestoViewModel`.

- Categorías: creación con nombre único, edición de límite de referencia y umbral, activación de alertas y eliminación confirmada. El nombre se conserva para mantener la relación con los movimientos históricos.
- Presupuestos: uno por categoría, semanal (desde el lunes) o mensual (desde el día 1), con detalle, edición y eliminación confirmada. El límite del presupuesto es independiente del límite de referencia de la categoría.
- El gasto utiliza solo movimientos de tipo Gasto de esa categoría y del período actual, sin fechas futuras. El interruptor y el umbral actual de la categoría controlan los avisos en pantalla.
- Para eliminar una categoría primero elimina su presupuesto. Los movimientos históricos no se borran.

Pruebas unitarias: `./gradlew :app:testDebugUnitTest`.

Pruebas de integración: inicia `firebase emulators:start --only auth,firestore --project demo-presupuestos --config scripts/firebase-presupuestos.json` y un emulador Android; luego ejecuta `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.finanquest.sv.PresupuestosIntegrationTest`. La clase configura Auth y Firestore locales antes de crear sus usuarios de prueba. Las reglas de `scripts` son únicamente para ese entorno local.


La interfaz incluye tarjetas adaptadas a tema claro y oscuro, resumen de gasto e ilustraciones 3D transparentes guardadas en drawable-nodpi. Las imágenes se cargan desde la APK y no requieren internet.
