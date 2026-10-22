package com.finanquest.sv.data.repository

import com.finanquest.sv.data.firebase.FirebaseProvider
import com.finanquest.sv.data.model.Movimiento
import com.google.firebase.firestore.ListenerRegistration

class MovimientoRepository {
    private val auth = FirebaseProvider.auth
    private val firestore = FirebaseProvider.firestore

    private fun usuario() = auth.currentUser?.uid?.let { firestore.collection("usuarios").document(it) }

    fun escucharMovimientos(onResultado: (List<Movimiento>, String?) -> Unit): ListenerRegistration? {
        val ref = usuario()?.collection("movimientos") ?: run {
            onResultado(emptyList(), "Inicia sesión para consultar tus movimientos"); return null
        }
        return ref.addSnapshotListener { snapshot, error ->
            if (error != null) onResultado(emptyList(), error.localizedMessage ?: "No se pudieron cargar los movimientos")
            else onResultado(snapshot?.documents.orEmpty().map { d ->
                Movimiento(d.id, d.getString("cuentaId") ?: "", d.getString("tipo") ?: "Gasto",
                    d.getDouble("monto") ?: 0.0, d.getString("categoria") ?: "",
                    d.getLong("fecha") ?: System.currentTimeMillis(), d.getString("nota") ?: "",
                    d.getString("comercio") ?: "")
            }.sortedByDescending { it.fecha }, null)
        }
    }

    fun crearMovimiento(movimiento: Movimiento, onResultado: (String?) -> Unit) {
        val usuarioRef = usuario() ?: run { onResultado("Inicia sesión para registrar movimientos"); return }
        val movimientoRef = usuarioRef.collection("movimientos").document()
        val cuentaRef = usuarioRef.collection("cuentas").document(movimiento.cuentaId)
        firestore.runTransaction { tx ->
            val cuenta = tx.get(cuentaRef)
            if (!cuenta.exists()) throw IllegalStateException("La cuenta seleccionada ya no existe")
            if (cuenta.getBoolean("activa") == false) throw IllegalStateException("La cuenta está inactiva")
            val saldo = cuenta.getDouble("saldo") ?: 0.0
            val signo = if (movimiento.tipo == "Ingreso") 1 else -1
            tx.update(cuentaRef, "saldo", saldo + signo * movimiento.monto)
            tx.set(movimientoRef, mapOf(
                "cuentaId" to movimiento.cuentaId, "tipo" to movimiento.tipo,
                "monto" to movimiento.monto, "categoria" to movimiento.categoria,
                "fecha" to movimiento.fecha, "nota" to movimiento.nota,
                "comercio" to movimiento.comercio
            ))
            null
        }.addOnSuccessListener { onResultado(null) }
            .addOnFailureListener { onResultado(it.localizedMessage ?: "No se pudo guardar el movimiento") }
    }
}
