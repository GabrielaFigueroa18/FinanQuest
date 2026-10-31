package com.finanquest.sv.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class RecompensaRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun escucharDesbloqueadas(
        onCambio: (Set<String>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("Debes iniciar sesión para ver tus recompensas")
            return null
        }

        return firestore.collection("usuarios")
            .document(uid)
            .collection("recompensas_desbloqueadas")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "No se pudieron cargar las recompensas")
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    onError("No se recibió información de recompensas")
                    return@addSnapshotListener
                }
                val ids = snapshot.documents
                    .filter { it.getBoolean("desbloqueada") == true }
                    .map { it.id }
                    .toSet()
                onCambio(ids)
            }
    }
}
