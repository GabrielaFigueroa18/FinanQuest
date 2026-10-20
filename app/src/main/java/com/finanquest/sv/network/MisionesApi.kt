package com.finanquest.sv.network
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object MisionesApi {

    private const val BASE_URL = "http://127.0.0.1:3001"

    fun completarInversionistaNovato(
        respuestas: List<Int>,
        onResultado: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario == null) {
            onError("Debes iniciar sesión.")
            return
        }

        usuario.getIdToken(false)
            .addOnSuccessListener { tokenResult ->

                val token = tokenResult.token

                if (token.isNullOrBlank()) {
                    onError("No se pudo obtener el token.")
                    return@addOnSuccessListener
                }

                Thread {
                    var conexion: HttpURLConnection? = null

                    try {
                        val url = URL(
                            "$BASE_URL/api/misiones/inversionista-novato/completar"
                        )

                        conexion = url.openConnection() as HttpURLConnection

                        conexion.requestMethod = "POST"
                        conexion.setRequestProperty(
                            "Authorization",
                            "Bearer $token"
                        )
                        conexion.setRequestProperty(
                            "Content-Type",
                            "application/json"
                        )

                        conexion.connectTimeout = 10000
                        conexion.readTimeout = 10000
                        conexion.doOutput = true

                        val datos = JSONObject().apply {
                            put("respuestas", JSONArray(respuestas))
                        }

                        conexion.outputStream.use { salida ->
                            salida.write(
                                datos.toString().toByteArray(Charsets.UTF_8)
                            )
                        }

                        val codigo = conexion.responseCode

                        val respuestaTexto = (
                                if (codigo in 200..299) {
                                    conexion.inputStream
                                } else {
                                    conexion.errorStream
                                }
                                )?.bufferedReader()?.use { it.readText() }
                            ?: ""

                        val respuesta = JSONObject(respuestaTexto)

                        if (codigo in 200..299) {
                            onResultado(respuesta)
                        } else {
                            onError(
                                respuesta.optString(
                                    "error",
                                    "Error del servidor: $codigo"
                                )
                            )
                        }

                    } catch (e: Exception) {
                        onError(
                            e.localizedMessage
                                ?: "No se pudo conectar con el servidor."
                        )
                    } finally {
                        conexion?.disconnect()
                    }
                }.start()
            }
            .addOnFailureListener {
                onError("No se pudo verificar la sesión.")
            }
    }
}
