package com.finanquest.sv.data.repository

import com.finanquest.sv.data.firebase.FirebaseProvider
import com.finanquest.sv.data.model.Cuenta
import com.google.firebase.firestore.ListenerRegistration

class CuentaRepository {
    private val auth = FirebaseProvider.auth
    private val firestore = FirebaseProvider.firestore

    private fun cuentas() = auth.currentUser?.uid?.let {
        firestore.collection("usuarios").document(it).collection("cuentas")
    }

    fun escucharCuentas(onResultado: (List<Cuenta>, String?) -> Unit): ListenerRegistration? {
        val ref = cuentas() ?: run { onResultado(emptyList(), "Inicia sesión para consultar tus cuentas"); return null }
        return ref.addSnapshotListener { snapshot, error ->
            if (error != null) onResultado(emptyList(), error.localizedMessage ?: "No se pudieron cargar las cuentas")
            else onResultado(snapshot?.documents.orEmpty().map { d ->
                Cuenta(d.id, d.getString("nombre") ?: "", d.getString("tipo") ?: "Ahorro",
                    d.getDouble("saldo") ?: 0.0, d.getString("descripcion") ?: "",
                    d.getBoolean("activa") ?: true, d.getLong("creadaEn") ?: 0L)
            }, null)
        }
    }

    fun crearCuenta(nombre: String, tipo: String, saldo: Double, descripcion: String, onResultado: (String?) -> Unit) {
        val ref = cuentas() ?: run { onResultado("Inicia sesión para crear una cuenta"); return }
        val id = ref.document().id
        ref.document(id).set(mapOf("nombre" to nombre.trim(), "tipo" to tipo, "saldo" to saldo,
            "descripcion" to descripcion.trim(), "activa" to true, "creadaEn" to System.currentTimeMillis()))
            .addOnSuccessListener { onResultado(null) }
            .addOnFailureListener { onResultado(it.localizedMessage ?: "No se pudo guardar la cuenta") }
    }

    fun actualizarCuenta(cuenta: Cuenta, onResultado: (String?) -> Unit) {
        val ref = cuentas()?.document(cuenta.id) ?: run { onResultado("Inicia sesión nuevamente"); return }
        ref.update(mapOf("nombre" to cuenta.nombre.trim(), "tipo" to cuenta.tipo,
            "descripcion" to cuenta.descripcion.trim(), "activa" to cuenta.activa))
            .addOnSuccessListener { onResultado(null) }
            .addOnFailureListener { onResultado(it.localizedMessage ?: "No se pudo actualizar la cuenta") }
    }
}
