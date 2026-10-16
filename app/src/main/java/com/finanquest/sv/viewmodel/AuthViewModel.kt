
package com.finanquest.sv.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.finanquest.sv.data.model.Usuario
import com.finanquest.sv.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    var cargando by mutableStateOf(false)
        private set

    var mensaje by mutableStateOf("")
        private set

    var usuario by mutableStateOf<Usuario?>(null)
        private set

    var sesionIniciada by mutableStateOf(
        repository.obtenerUsuarioActual() != null
    )
        private set

    fun registrar(
        nombre: String,
        correo: String,
        contrasena: String,
        confirmarContrasena: String,
        onExito: () -> Unit
    ) {
        if (cargando) return

        if (
            nombre.isBlank() ||
            correo.isBlank() ||
            contrasena.isBlank() ||
            confirmarContrasena.isBlank()
        ) {
            mensaje = "Completa todos los campos"
            return
        }

        if (contrasena != confirmarContrasena) {
            mensaje = "Las contraseñas no coinciden"
            return
        }

        cargando = true
        mensaje = ""

        repository.registrarUsuario(
            nombre = nombre,
            correo = correo,
            contrasena = contrasena
        ) { exito, respuesta ->

            cargando = false
            mensaje = respuesta

            if (exito) {
                sesionIniciada = true
                cargarPerfil()
                onExito()
            }
        }
    }

    fun iniciarSesion(
        correo: String,
        contrasena: String,
        onExito: () -> Unit
    ) {
        if (cargando) return

        cargando = true
        mensaje = ""

        repository.iniciarSesion(
            correo = correo,
            contrasena = contrasena
        ) { exito, respuesta ->

            cargando = false
            mensaje = respuesta

            if (exito) {
                sesionIniciada = true
                cargarPerfil()
                onExito()
            }
        }
    }

    fun cargarPerfil() {
        repository.obtenerPerfil { perfil, error ->
            if (perfil != null) {
                usuario = perfil
            } else {
                mensaje = error ?: "No se pudo cargar el perfil"
            }
        }
    }

    // ACTUALIZAR DATOS PERSONALES EN FIRESTORE
    fun actualizarDatosPersonales(
        nombre: String,
        universidad: String,
        onExito: () -> Unit
    ) {
        if (cargando) return

        val nombreLimpio = nombre.trim()
        val universidadLimpia = universidad.trim()

        if (nombreLimpio.isBlank() || universidadLimpia.isBlank()) {
            mensaje = "Completa tu nombre y universidad"
            return
        }

        if (nombreLimpio.length > 100 ||
            universidadLimpia.length > 150
        ) {
            mensaje = "Los datos son demasiado largos"
            return
        }

        val uid = firebaseAuth.currentUser?.uid

        if (uid == null) {
            mensaje = "Debes iniciar sesión nuevamente"
            return
        }

        cargando = true
        mensaje = ""

        val cambios = mapOf(
            "nombre" to nombreLimpio,
            "universidad" to universidadLimpia
        )

        firestore.collection("usuarios")
            .document(uid)
            .update(cambios)
            .addOnSuccessListener {
                cargando = false
                mensaje = "Datos actualizados correctamente"

                // Recargar datos para Perfil y Dashboard
                cargarPerfil()

                onExito()
            }
            .addOnFailureListener { error ->
                cargando = false
                mensaje = "No se pudieron guardar los cambios: ${error.localizedMessage}"
            }
    }

    fun recuperarContrasena(correo: String) {
        if (cargando) return

        cargando = true
        mensaje = ""

        repository.recuperarContrasena(correo) { _, respuesta ->
            cargando = false
            mensaje = respuesta
        }
    }

    fun cerrarSesion() {
        repository.cerrarSesion()

        usuario = null
        sesionIniciada = false
        mensaje = ""
        cargando = false
    }

    fun limpiarMensaje() {
        mensaje = ""
    }
}
