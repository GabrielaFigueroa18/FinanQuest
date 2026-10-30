# Corregir acceso a categorías y presupuestos

Proyecto de la APK: `finanquestsv`.

Consola: https://console.firebase.google.com/project/finanquestsv/firestore

En la pestaña Reglas, agrega estos bloques DENTRO de `match /databases/{database}/documents { ... }`, conservando las reglas de las otras pantallas. No reemplaces todo el contenido del editor con este fragmento.

```text
match /usuarios/{uid}/categorias/{categoriaId} {
  allow read, write: if request.auth != null && request.auth.uid == uid;
}

match /usuarios/{uid}/presupuestos/{presupuestoId} {
  allow read, write: if request.auth != null && request.auth.uid == uid;
}

match /usuarios/{uid}/movimientos/{movimientoId} {
  allow read: if request.auth != null && request.auth.uid == uid;
}
```

Estos permisos permiten a cada usuario acceder únicamente a sus categorías y presupuestos, y consultar sus movimientos para calcular el gasto. No permiten acceder a los datos de otro usuario ni modifican las reglas del perfil, cuentas o metas.

Antes de publicar, revisa si ya existen bloques para estas rutas y combina los permisos con sus validaciones actuales. Para preparar esa combinación hace falta leer las reglas publicadas.

Después de publicar, cierra y vuelve a abrir la aplicación para restablecer los listeners de Firestore, y prueba crear una categoría y un presupuesto con tu sesión iniciada. Publicar reglas no requiere instalar otra APK.

Estado: fragmento preparado; todavía no publicado ni verificado en producción.
