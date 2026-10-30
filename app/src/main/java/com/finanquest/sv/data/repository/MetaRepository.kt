package com.finanquest.sv.data.repository

import android.os.Handler
import android.os.Looper
import com.finanquest.sv.data.firebase.FirebaseProvider
import com.finanquest.sv.data.model.MetaAhorro
import com.finanquest.sv.data.model.AporteMeta
import com.google.firebase.firestore.ListenerRegistration
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MetaRepository {
    private val auth = FirebaseProvider.auth
    private val firestore = FirebaseProvider.firestore
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    // Huawei conectado por USB con: adb reverse tcp:3001 tcp:3001
    private val backendUrl = "http://127.0.0.1:3001"

    private fun ref() = auth.currentUser?.uid?.let {
        firestore.collection("usuarios").document(it)
    }

    fun escuchar(onResult: (List<MetaAhorro>, String?) -> Unit): ListenerRegistration? {
        val r = ref()?.collection("metas") ?: run {
            onResult(emptyList(), "Inicia sesión para consultar tus metas")
            return null
        }
        return r.addSnapshotListener { snapshot, error ->
            if (error != null) onResult(emptyList(), error.localizedMessage ?: "No se pudieron cargar las metas")
            else onResult(snapshot?.documents.orEmpty().map { d ->
                MetaAhorro(d.id, d.getString("nombre") ?: "", d.getDouble("objetivo") ?: 0.0,
                    d.getDouble("actual") ?: 0.0, d.getLong("fechaLimite") ?: 0L,
                    d.getString("icono") ?: "🎯", d.getLong("creadaEn") ?: 0L,
                    d.getBoolean("completada") ?: false)
            }, null)
        }
    }

    fun crear(nombre: String, objetivo: Double, inicial: Double, fecha: Long, icono: String, cb: (String?) -> Unit) {
        val u = ref() ?: run { cb("Inicia sesión para guardar metas"); return }
        val d = u.collection("metas").document()
        val ahora = System.currentTimeMillis()
        val datos = mapOf("nombre" to nombre.trim(), "objetivo" to objetivo,
            "actual" to inicial, "fechaLimite" to fecha, "icono" to icono,
            "creadaEn" to ahora, "completada" to (inicial >= objetivo))
        val batch = firestore.batch()
        batch.set(d, datos)
        if (inicial > 0) batch.set(d.collection("aportes").document(),
            mapOf("monto" to inicial, "fecha" to ahora, "nota" to "Ahorro inicial"))
        batch.commit().addOnSuccessListener { cb(null) }
            .addOnFailureListener { cb(it.localizedMessage ?: "No se pudo guardar la meta") }
    }

    // Crea la meta y descuenta su ahorro inicial en UNA transacción del backend.
    // La función crear() anterior permanece intacta para metas con inicial = 0.
    fun crearConCuenta(
        nombre: String, objetivo: Double, inicial: Double, fecha: Long,
        icono: String, cuentaId: String, solicitudId: String,
        cb: (String?) -> Unit
    ) {
        val usuario = auth.currentUser ?: run { cb("Inicia sesión nuevamente"); return }
        usuario.getIdToken(false).addOnSuccessListener { resultado ->
            val token = resultado.token
            if (token.isNullOrBlank()) { cb("No se pudo obtener el token de Firebase"); return@addOnSuccessListener }
            executor.execute {
                var conexion: HttpURLConnection? = null
                try {
                    val datos = JSONObject().apply {
                        put("nombre", nombre); put("objetivo", objetivo)
                        put("inicial", inicial); put("fechaLimite", fecha)
                        put("icono", icono); put("cuentaId", cuentaId)
                        put("solicitudId", solicitudId)
                    }
                    conexion = URL("$backendUrl/api/metas/crear-con-ahorro")
                        .openConnection() as HttpURLConnection
                    conexion.requestMethod = "POST"
                    conexion.connectTimeout = 10000
                    conexion.readTimeout = 15000
                    conexion.doOutput = true
                    conexion.setRequestProperty("Authorization", "Bearer $token")
                    conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    conexion.outputStream.use { it.write(datos.toString().toByteArray(Charsets.UTF_8)) }
                    val codigo = conexion.responseCode
                    val contenido = (if (codigo in 200..299) conexion.inputStream else conexion.errorStream)
                        ?.bufferedReader()?.use { it.readText() } ?: "{}"
                    val respuesta = JSONObject(contenido)
                    if (codigo !in 200..299 || !respuesta.optBoolean("ok", false)) {
                        throw Exception(respuesta.optString("mensaje", "No se pudo crear la meta"))
                    }
                    mainHandler.post { cb(null) }
                } catch (e: Exception) {
                    mainHandler.post { cb(e.localizedMessage ?: "Error conectando con el backend") }
                } finally { conexion?.disconnect() }
            }
        }.addOnFailureListener { cb("No se pudo autenticar la solicitud") }
    }

    fun aportar(metaId: String, cuentaId: String, monto: Double, solicitudId: String, cb: (String?) -> Unit) {
        val usuario = auth.currentUser ?: run { cb("Inicia sesión nuevamente"); return }
        usuario.getIdToken(false).addOnSuccessListener { resultado ->
            val token = resultado.token
            if (token.isNullOrBlank()) { cb("No se pudo obtener el token de Firebase"); return@addOnSuccessListener }
            executor.execute {
                var conexion: HttpURLConnection? = null
                try {
                    val datos = JSONObject().apply {
                        put("metaId", metaId)
                        put("cuentaId", cuentaId)
                        put("monto", monto)
                        put("solicitudId", solicitudId)
                    }
                    conexion = URL("$backendUrl/api/metas/aportar").openConnection() as HttpURLConnection
                    conexion.requestMethod = "POST"
                    conexion.connectTimeout = 10000
                    conexion.readTimeout = 15000
                    conexion.doOutput = true
                    conexion.setRequestProperty("Authorization", "Bearer $token")
                    conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    conexion.outputStream.use { it.write(datos.toString().toByteArray(Charsets.UTF_8)) }
                    val codigo = conexion.responseCode
                    val contenido = (if (codigo in 200..299) conexion.inputStream else conexion.errorStream)
                        ?.bufferedReader()?.use { it.readText() } ?: "{}"
                    val respuesta = JSONObject(contenido)
                    if (codigo !in 200..299 || !respuesta.optBoolean("ok", false)) {
                        throw Exception(respuesta.optString("mensaje", "No se pudo guardar el aporte"))
                    }
                    mainHandler.post { cb(null) }
                } catch (e: Exception) {
                    mainHandler.post { cb(e.localizedMessage ?: "No se pudo conectar con el backend") }
                } finally {
                    conexion?.disconnect()
                }
            }
        }.addOnFailureListener { cb("No se pudo autenticar la solicitud") }
    }

    fun escucharAportes(id: String, onResult: (List<AporteMeta>, String?) -> Unit): ListenerRegistration? {
        val m = ref()?.collection("metas")?.document(id) ?: run {
            onResult(emptyList(), "Inicia sesión nuevamente")
            return null
        }
        return m.collection("aportes").addSnapshotListener { snapshot, error ->
            if (error != null) onResult(emptyList(), error.localizedMessage ?: "No se pudo cargar la actividad")
            else onResult(snapshot?.documents.orEmpty().map { d ->
                AporteMeta(d.id, d.getDouble("monto") ?: 0.0,
                    d.getLong("fecha") ?: 0L, d.getString("nota") ?: "")
            }.sortedByDescending { it.fecha }, null)
        }
    }
}
