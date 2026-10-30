package com.finanquest.sv.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.net.HttpURLConnection
import java.net.URL

class RecompensasViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var listener: ListenerRegistration? = null
    private var uidEscuchado: String? = null

    var recompensasDesbloqueadas by mutableStateOf<Set<String>>(emptySet())
        private set
    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    // Para un telefono fisico conectado por USB se requiere:
    // adb reverse tcp:3001 tcp:3001
    private val backendUrl = "http://127.0.0.1:3001"

    fun iniciarEscucha() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            detenerEscucha()
            recompensasDesbloqueadas = emptySet()
            error = "Debes iniciar sesión para ver tus recompensas"
            cargando = false
            return
        }
        if (listener != null && uidEscuchado == uid) return

        detenerEscucha()
        uidEscuchado = uid
        cargando = true
        error = null
        recompensasDesbloqueadas = emptySet()

        listener = firestore.collection("usuarios")
            .document(uid)
            .collection("recompensas_desbloqueadas")
            .addSnapshotListener { snapshot, excepcion ->
                if (uidEscuchado != uid) return@addSnapshotListener
                if (excepcion != null) {
                    error = excepcion.localizedMessage ?: "No se pudieron cargar las recompensas"
                    cargando = false
                    return@addSnapshotListener
                }
                recompensasDesbloqueadas = snapshot?.documents
                    ?.filter { it.getBoolean("desbloqueada") == true }
                    ?.map { it.id }
                    ?.toSet() ?: emptySet()
                cargando = false
            }

        sincronizarRecompensas(uid)
    }

    private fun sincronizarRecompensas(uid: String) {
        val usuario = auth.currentUser ?: return
        usuario.getIdToken(false).addOnSuccessListener { resultado ->
            val token = resultado.token
            if (token.isNullOrBlank()) {
                if (uidEscuchado == uid) error = "No se pudo obtener el token de sesión"
                return@addOnSuccessListener
            }
            Thread {
                var conexion: HttpURLConnection? = null
                try {
                    conexion = (URL("$backendUrl/api/recompensas/sincronizar")
                        .openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = 10000
                        readTimeout = 15000
                        setRequestProperty("Authorization", "Bearer $token")
                        setRequestProperty("Content-Type", "application/json")
                    }
                    val codigo = conexion.responseCode
                    if (codigo !in 200..299) {
                        mainHandler.post {
                            if (uidEscuchado == uid) {
                                error = "No se pudieron verificar los logros (HTTP $codigo)"
                            }
                        }
                    } else {
                        mainHandler.post { if (uidEscuchado == uid) error = null }
                    }
                    // El listener de Firestore actualiza las insignias automáticamente.
                } catch (ex: Exception) {
                    mainHandler.post {
                        if (uidEscuchado == uid) {
                            error = "No se pudo conectar con el backend: ${ex.localizedMessage}"
                        }
                    }
                } finally {
                    conexion?.disconnect()
                }
            }.start()
        }.addOnFailureListener { ex ->
            if (uidEscuchado == uid) error = "Error de autenticación: ${ex.localizedMessage}"
        }
    }

    fun detenerEscucha() {
        listener?.remove()
        listener = null
        uidEscuchado = null
    }

    override fun onCleared() {
        detenerEscucha()
        super.onCleared()
    }
}
