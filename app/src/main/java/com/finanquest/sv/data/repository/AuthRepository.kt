
package com.finanquest.sv.data.repository

import com.finanquest.sv.data.firebase.FirebaseProvider
import com.finanquest.sv.data.model.Usuario
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue

class AuthRepository {

    private val auth = FirebaseProvider.auth
    private val firestore = FirebaseProvider.firestore


    fun obtenerUsuarioActual(): FirebaseUser? {
        return auth.currentUser
    }


    fun registrarUsuario(
        nombre: String,
        correo: String,
        contrasena: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
            onResultado(false, "Completa todos los campos")
            return
        }

        if (contrasena.length < 6) {
            onResultado(
                false,
                "La contraseña debe tener al menos 6 caracteres"
            )
            return
        }

        auth.createUserWithEmailAndPassword(
            correo.trim(),
            contrasena
        ).addOnCompleteListener { tarea ->

            if (!tarea.isSuccessful) {
                onResultado(
                    false,
                    tarea.exception?.localizedMessage
                        ?: "Error al registrar usuario"
                )
                return@addOnCompleteListener
            }

            val uid = tarea.result?.user?.uid

            if (uid == null) {
                onResultado(false, "No se pudo obtener el ID del usuario")
                return@addOnCompleteListener
            }

            val datosUsuario = hashMapOf(
                "nombre" to nombre.trim(),
                "correo" to correo.trim(),
                "universidad" to "",
                "moneda" to "USD",
                "xp" to 0,
                "nivel" to 1,
                "mascota" to "Gatito",
                "fechaRegistro" to FieldValue.serverTimestamp()
            )

            firestore.collection("usuarios")
                .document(uid)
                .set(datosUsuario)
                .addOnSuccessListener {
                    onResultado(true, "Cuenta creada correctamente")
                }
                .addOnFailureListener { error ->
                    onResultado(
                        false,
                        "La cuenta se creó, pero no se pudo guardar el perfil: " +
                                (error.localizedMessage ?: "Error desconocido")
                    )
                }
        }
    }


    fun iniciarSesion(
        correo: String,
        contrasena: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        if (correo.isBlank() || contrasena.isBlank()) {
            onResultado(false, "Completa todos los campos")
            return
        }

        auth.signInWithEmailAndPassword(
            correo.trim(),
            contrasena
        ).addOnCompleteListener { tarea ->

            if (tarea.isSuccessful) {
                onResultado(true, "Bienvenido a FinanQuest SV")
            } else {
                onResultado(
                    false,
                    tarea.exception?.localizedMessage
                        ?: "No se pudo iniciar sesión"
                )
            }
        }
    }


    fun obtenerPerfil(
        onResultado: (Usuario?, String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onResultado(null, "No hay una sesión activa")
            return
        }

        firestore.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { documento ->

                if (!documento.exists()) {
                    onResultado(
                        null,
                        "El perfil del usuario no existe"
                    )
                    return@addOnSuccessListener
                }

                val usuario = Usuario(
                    id = uid,
                    nombre = documento.getString("nombre") ?: "",
                    correo = documento.getString("correo") ?: "",
                    universidad = documento.getString("universidad") ?: "",
                    moneda = documento.getString("moneda") ?: "USD",
                    xp = documento.getLong("xp")?.toInt() ?: 0,
                    nivel = documento.getLong("nivel")?.toInt() ?: 1,
                    mascota = documento.getString("mascota") ?: "Gatito"
                )

                onResultado(usuario, null)
            }
            .addOnFailureListener { error ->
                onResultado(null, error.localizedMessage)
            }
    }


    fun recuperarContrasena(
        correo: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        if (correo.isBlank()) {
            onResultado(false, "Ingresa tu correo electrónico")
            return
        }

        auth.sendPasswordResetEmail(correo.trim())
            .addOnSuccessListener {
                onResultado(
                    true,
                    "Si el correo tiene una cuenta, recibirás instrucciones para recuperar el acceso"
                )
            }
            .addOnFailureListener {
                onResultado(
                    false,
                    "No se pudo procesar la solicitud. Inténtalo nuevamente"
                )
            }
    }


    fun cerrarSesion() {
        auth.signOut()
    }
}
