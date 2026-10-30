
package com.finanquest.sv.data.repository

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.finanquest.sv.data.firebase.FirebaseProvider
import com.finanquest.sv.data.model.Movimiento
import com.google.firebase.firestore.ListenerRegistration
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MovimientoRepository {

    private val auth = FirebaseProvider.auth
    private val firestore = FirebaseProvider.firestore

    // Para emulador de Android Studio
    private val backendUrl = "http://127.0.0.1:3001"

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private fun usuario() = auth.currentUser?.uid?.let {
        firestore.collection("usuarios").document(it)
    }

    // ==========================================
    // CONSULTAR MOVIMIENTOS
    // ==========================================

    fun escucharMovimientos(
        onResultado: (List<Movimiento>, String?) -> Unit
    ): ListenerRegistration? {

        val ref = usuario()?.collection("movimientos") ?: run {
            onResultado(
                emptyList(),
                "Inicia sesión para consultar tus movimientos"
            )
            return null
        }

        return ref.addSnapshotListener { snapshot, error ->

            if (error != null) {
                onResultado(
                    emptyList(),
                    error.localizedMessage
                        ?: "No se pudieron cargar los movimientos"
                )
            } else {
                val lista = snapshot?.documents.orEmpty()
                    .map { d ->
                        Movimiento(
                            d.id,
                            d.getString("cuentaId") ?: "",
                            d.getString("tipo") ?: "Gasto",
                            d.getDouble("monto") ?: 0.0,
                            d.getString("categoria") ?: "",
                            d.getLong("fecha")
                                ?: System.currentTimeMillis(),
                            d.getString("nota") ?: "",
                            d.getString("comercio") ?: ""
                        )
                    }
                    .sortedByDescending { it.fecha }

                onResultado(lista, null)
            }
        }
    }

    // ==========================================
    // CREAR MOVIMIENTO MEDIANTE BACKEND
    // ==========================================

    fun crearMovimiento(
        movimiento: Movimiento,
        onResultado: (String?) -> Unit
    ) {
        val usuarioActual = auth.currentUser ?: run {
            onResultado("Inicia sesión para registrar movimientos")
            return
        }

        if (
            movimiento.tipo !in listOf("Ingreso", "Gasto") ||
            !movimiento.monto.isFinite() ||
            movimiento.monto <= 0
        ) {
            onResultado("Datos del movimiento inválidos")
            return
        }

        usuarioActual.getIdToken(false)
            .addOnSuccessListener { resultado ->

                val token = resultado.token

                if (token.isNullOrBlank()) {
                    onResultado("No se pudo obtener el token de Firebase")
                    return@addOnSuccessListener
                }

                executor.execute {
                    try {
                        val datos = JSONObject().apply {
                            put("cuentaId", movimiento.cuentaId)
                            put("tipo", movimiento.tipo)
                            put("monto", movimiento.monto)
                            put("categoria", movimiento.categoria)
                            put("fecha", movimiento.fecha)
                            put("nota", movimiento.nota)
                            put("comercio", movimiento.comercio)
                        }

                        val respuesta = solicitarBackend(
                            ruta = "/api/movimientos/crear",
                            token = token,
                            datos = datos
                        )

                        if (!respuesta.optBoolean("ok", false)) {
                            throw Exception(
                                respuesta.optString(
                                    "mensaje",
                                    "No se pudo guardar el movimiento"
                                )
                            )
                        }

                        // La transacción ya está confirmada.
                        mainHandler.post {
                            onResultado(null)
                        }

                        // Actualizar misiones después de guardar.
                        sincronizarConToken(token)

                    } catch (error: Exception) {
                        Log.e(
                            "FinanQuestMovimientos",
                            "Error guardando movimiento",
                            error
                        )

                        mainHandler.post {
                            onResultado(
                                error.message
                                    ?: "No se pudo guardar el movimiento"
                            )
                        }
                    }
                }
            }
            .addOnFailureListener {
                onResultado(
                    "No se pudo autenticar la solicitud"
                )
            }
    }

    // ==========================================
    // SINCRONIZAR MISIONES
    // ==========================================

    fun sincronizarMisionesMovimientos() {
        val usuarioActual = auth.currentUser ?: return

        usuarioActual.getIdToken(false)
            .addOnSuccessListener { resultado ->

                val token = resultado.token
                    ?: return@addOnSuccessListener

                executor.execute {
                    sincronizarConToken(token)
                }
            }
            .addOnFailureListener { error ->
                Log.e(
                    "FinanQuestMisiones",
                    "Error obteniendo token",
                    error
                )
            }
    }

    private fun sincronizarConToken(token: String) {
        try {
            val respuesta = solicitarBackend(
                ruta = "/api/misiones/movimientos/sincronizar",
                token = token,
                datos = JSONObject()
            )

            Log.d(
                "FinanQuestMisiones",
                "Sincronización: $respuesta"
            )
        } catch (error: Exception) {
            Log.e(
                "FinanQuestMisiones",
                "No se pudieron actualizar las misiones",
                error
            )
        }
    }

    // ==========================================
    // CONEXIÓN HTTP CON BACKEND
    // ==========================================

    private fun solicitarBackend(
        ruta: String,
        token: String,
        datos: JSONObject
    ): JSONObject {

        val conexion = URL("$backendUrl$ruta")
            .openConnection() as HttpURLConnection

        try {
            conexion.requestMethod = "POST"
            conexion.connectTimeout = 10000
            conexion.readTimeout = 15000
            conexion.doOutput = true

            conexion.setRequestProperty(
                "Authorization",
                "Bearer $token"
            )

            conexion.setRequestProperty(
                "Content-Type",
                "application/json; charset=utf-8"
            )

            conexion.outputStream.use { salida ->
                salida.write(
                    datos.toString().toByteArray(Charsets.UTF_8)
                )
            }

            val codigo = conexion.responseCode

            val contenido = (
                    if (codigo in 200..299) {
                        conexion.inputStream
                    } else {
                        conexion.errorStream
                    }
                    )?.bufferedReader()?.use { it.readText() }
                ?: "{}"

            val respuesta = JSONObject(contenido)

            if (codigo !in 200..299) {
                throw Exception(
                    respuesta.optString(
                        "mensaje",
                        "Error HTTP $codigo"
                    )
                )
            }

            return respuesta

        } finally {
            conexion.disconnect()
        }
    }
}
