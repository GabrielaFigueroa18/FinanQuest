package com.finanquest.sv.data.repository

import com.finanquest.sv.data.firebase.FirebaseProvider
import com.finanquest.sv.data.model.Categoria
import com.finanquest.sv.data.model.Movimiento
import com.finanquest.sv.data.model.Presupuesto
import com.finanquest.sv.utils.FinanzasIntegrante2
import com.google.firebase.firestore.ListenerRegistration

class PresupuestoRepository {

    private fun user() =
        FirebaseProvider.auth.currentUser?.uid?.let { uid ->
            FirebaseProvider.firestore.collection("usuarios").document(uid)
        }

    private fun idCategoria(nombre: String): String {
        return FinanzasIntegrante2.idCategoria(nombre)
    }

    fun eliminarCategoria(categoria: Categoria, cb: (String?) -> Unit) {
        val usuario =
            user()
                ?: run {
                    cb("Inicia sesión nuevamente")
                    return
                }
        val ref = usuario.collection("categorias").document(categoria.id)
        FirebaseProvider.firestore
            .runTransaction { transaction ->
                val presupuesto =
                    transaction.get(usuario.collection("presupuestos").document(categoria.id))
                check(!presupuesto.exists()) { "Elimina primero el presupuesto de esta categoría" }
                transaction.delete(ref)
            }
            .addOnSuccessListener { cb(null) }
            .addOnFailureListener { cb(it.localizedMessage ?: "No se pudo eliminar la categoría") }
    }

    fun editarCategoria(
        categoria: Categoria,
        limite: Double,
        umbral: Double,
        cb: (String?) -> Unit,
    ) {
        val ref =
            user()?.collection("categorias")?.document(categoria.id)
                ?: run {
                    cb("Inicia sesión nuevamente")
                    return
                }
        val error =
            FinanzasIntegrante2.validarCategoria(categoria.nombre, limite.toString(), umbral)
        if (error != null) {
            cb(error)
            return
        }
        ref.update(mapOf("limite" to limite, "umbral" to umbral))
            .addOnSuccessListener { cb(null) }
            .addOnFailureListener { cb(it.localizedMessage ?: "No se pudo actualizar") }
    }

    fun eliminarPresupuesto(id: String, cb: (String?) -> Unit) {
        val ref =
            user()?.collection("presupuestos")?.document(id)
                ?: run {
                    cb("Inicia sesión nuevamente")
                    return
                }
        ref.delete()
            .addOnSuccessListener { cb(null) }
            .addOnFailureListener { cb(it.localizedMessage ?: "No se pudo eliminar") }
    }

    fun actualizarUmbrales(categorias: List<Categoria>, umbral: Double, cb: (String?) -> Unit) {
        val usuario =
            user()
                ?: run {
                    cb("Inicia sesión nuevamente")
                    return
                }
        if (!FinanzasIntegrante2.validarUmbral(umbral) || categorias.size > 500) {
            cb("No se puede aplicar este umbral")
            return
        }
        val batch = FirebaseProvider.firestore.batch()
        categorias.forEach {
            batch.update(usuario.collection("categorias").document(it.id), "umbral", umbral)
        }
        batch
            .commit()
            .addOnSuccessListener { cb(null) }
            .addOnFailureListener {
                cb(it.localizedMessage ?: "No se pudieron actualizar las alertas")
            }
    }

    // ==========================================
    // ESCUCHAR CATEGORÍAS
    // ==========================================

    fun escucharCategorias(cb: (List<Categoria>, String?) -> Unit): ListenerRegistration? {

        val referencia =
            user()?.collection("categorias")
                ?: run {
                    cb(emptyList(), "Inicia sesión para consultar categorías")
                    return null
                }

        return referencia.addSnapshotListener { snapshot, error ->
            if (error != null) {
                cb(emptyList(), error.localizedMessage ?: "No se pudieron cargar categorías")
                return@addSnapshotListener
            }

            val categorias =
                snapshot?.documents.orEmpty().map { doc ->
                    Categoria(
                        doc.id,
                        doc.getString("nombre") ?: "",
                        doc.getDouble("limite") ?: 0.0,
                        doc.getDouble("umbral") ?: 0.8,
                        doc.getBoolean("alertasActivas") ?: true,
                        doc.getLong("creadaEn") ?: 0L,
                    )
                }

            cb(categorias, null)
        }
    }

    // ==========================================
    // GUARDAR CATEGORÍA
    // ==========================================

    fun guardarCategoria(nombre: String, limite: Double, umbral: Double, cb: (String?) -> Unit) {

        val referencia =
            user()?.collection("categorias")
                ?: run {
                    cb("Inicia sesión nuevamente")
                    return
                }

        val errorValidacion =
            FinanzasIntegrante2.validarCategoria(nombre, limite.toString(), umbral)
        if (errorValidacion != null) {
            cb(errorValidacion)
            return
        }
        val nombreLimpio = nombre.trim()
        val id = idCategoria(nombreLimpio)

        if (id.isBlank()) {
            cb("El nombre de la categoría no es válido")
            return
        }

        if (!limite.isFinite() || limite <= 0.0) {
            cb("El límite debe ser mayor que cero")
            return
        }

        if (umbral !in 0.5..1.0) {
            cb("El umbral debe estar entre 50% y 100%")
            return
        }

        val documento = referencia.document(id)

        FirebaseProvider.firestore
            .runTransaction { transaction ->
                check(!transaction.get(documento).exists()) {
                    "Ya existe una categoría con ese nombre"
                }
                transaction.set(
                    documento,
                    mapOf(
                        "nombre" to nombreLimpio,
                        "limite" to limite,
                        "umbral" to umbral,
                        "alertasActivas" to true,
                        "creadaEn" to System.currentTimeMillis(),
                    ),
                )
            }
            .addOnSuccessListener { cb(null) }
            .addOnFailureListener { cb(it.localizedMessage ?: "No se pudo guardar la categoría") }
    }

    // ==========================================
    // ACTUALIZAR ALERTAS DE CATEGORÍA
    // ==========================================

    fun actualizarAlertasCategoria(
        categoria: Categoria,
        activas: Boolean,
        umbral: Double,
        cb: (String?) -> Unit,
    ) {

        val documento =
            user()?.collection("categorias")?.document(categoria.id)
                ?: run {
                    cb("Inicia sesión nuevamente")
                    return
                }

        if (umbral !in 0.5..1.0) {
            cb("El umbral debe estar entre 50% y 100%")
            return
        }

        documento
            .update(mapOf("alertasActivas" to activas, "umbral" to umbral))
            .addOnSuccessListener { cb(null) }
            .addOnFailureListener { error ->
                cb(error.localizedMessage ?: "No se pudieron actualizar las alertas")
            }
    }

    // ==========================================
    // ESCUCHAR PRESUPUESTOS
    // ==========================================

    fun escucharPresupuestos(cb: (List<Presupuesto>, String?) -> Unit): ListenerRegistration? {

        val referencia =
            user()?.collection("presupuestos")
                ?: run {
                    cb(emptyList(), "Inicia sesión nuevamente")
                    return null
                }

        return referencia.addSnapshotListener { snapshot, error ->
            if (error != null) {
                cb(emptyList(), error.localizedMessage ?: "No se pudieron cargar presupuestos")
                return@addSnapshotListener
            }

            val presupuestos =
                snapshot?.documents.orEmpty().map { doc ->
                    Presupuesto(
                        doc.id,
                        doc.getString("categoriaId") ?: "",
                        doc.getString("categoria") ?: "",
                        doc.getDouble("limite") ?: 0.0,
                        doc.getString("periodo") ?: "Mensual",
                        doc.getLong("inicioPeriodo") ?: 0L,
                        doc.getDouble("alertaUmbral") ?: 0.8,
                        doc.getBoolean("activo") ?: true,
                    )
                }

            cb(presupuestos, null)
        }
    }

    // ==========================================
    // GUARDAR PRESUPUESTO
    // ==========================================

    fun guardarPresupuesto(
        categoria: String,
        limite: Double,
        periodo: String,
        cb: (String?) -> Unit,
    ) {

        val usuario =
            user()
                ?: run {
                    cb("Inicia sesión nuevamente")
                    return
                }

        val categoriaLimpia = categoria.trim()
        val id = categoriaLimpia

        if (id.isBlank()) {
            cb("Selecciona una categoría válida")
            return
        }

        if (!limite.isFinite() || limite <= 0.0) {
            cb("El límite debe ser mayor que cero")
            return
        }

        if (periodo !in listOf("Semanal", "Mensual")) {
            cb("Selecciona un período válido")
            return
        }

        val categoriaRef = usuario.collection("categorias").document(id)

        FirebaseProvider.firestore
            .runTransaction { transaction ->
                val categoriaDoc = transaction.get(categoriaRef)
                check(categoriaDoc.exists()) { "La categoría seleccionada no existe" }
                transaction.set(
                    usuario.collection("presupuestos").document(id),
                    mapOf(
                        "categoriaId" to id,
                        "categoria" to (categoriaDoc.getString("nombre") ?: categoriaLimpia),
                        "limite" to limite,
                        "periodo" to periodo,
                        "inicioPeriodo" to FinanzasIntegrante2.inicioPeriodoPresupuesto(periodo),
                        "alertaUmbral" to (categoriaDoc.getDouble("umbral") ?: 0.8),
                        "activo" to true,
                    ),
                )
            }
            .addOnSuccessListener { cb(null) }
            .addOnFailureListener { cb(it.localizedMessage ?: "No se pudo guardar el presupuesto") }
    }

    // ==========================================
    // ESCUCHAR MOVIMIENTOS
    // ==========================================

    fun escucharMovimientos(cb: (List<Movimiento>, String?) -> Unit): ListenerRegistration? {

        val referencia =
            user()?.collection("movimientos")
                ?: run {
                    cb(emptyList(), "Inicia sesión nuevamente")
                    return null
                }

        return referencia.addSnapshotListener { snapshot, error ->
            if (error != null) {
                cb(emptyList(), error.localizedMessage ?: "No se pudieron cargar movimientos")
                return@addSnapshotListener
            }

            val movimientos =
                snapshot?.documents.orEmpty().map { doc ->
                    Movimiento(
                        doc.id,
                        doc.getString("cuentaId") ?: "",
                        doc.getString("tipo") ?: "Gasto",
                        doc.getDouble("monto") ?: 0.0,
                        doc.getString("categoria") ?: "",
                        doc.getLong("fecha") ?: 0L,
                        doc.getString("nota") ?: "",
                    )
                }

            cb(movimientos, null)
        }
    }
}
