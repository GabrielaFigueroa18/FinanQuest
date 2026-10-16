
package com.finanquest.sv.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.finanquest.sv.data.model.Usuario
import com.finanquest.sv.data.repository.AuthRepository

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    // Estados que observarán las pantallas
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

    // Registrar un estudiante
    fun registrar(
        nombre: String,
        correo: String,
        contrasena: String,
        confirmarContrasena: String,
        onExito: () -> Unit
    ) {
        if (cargando) return

        if (nombre.isBlank() || correo.isBlank() ||
            contrasena.isBlank() || confirmarContrasena.isBlank()
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

    // Iniciar sesión
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

    // Consultar el perfil en Firestore
    fun cargarPerfil() {
        repository.obtenerPerfil { perfil, error ->

            if (perfil != null) {
                usuario = perfil
            } else {
                mensaje = error ?: "No se pudo cargar el perfil"
            }
        }
    }

    // Recuperar contraseña
    fun recuperarContrasena(correo: String) {
        if (cargando) return

        cargando = true
        mensaje = ""

        repository.recuperarContrasena(correo) { _, respuesta ->
            cargando = false
            mensaje = respuesta
        }
    }

    // Cerrar sesión
    fun cerrarSesion() {
        repository.cerrarSesion()

        usuario = null
        sesionIniciada = false
        mensaje = ""
        cargando = false
    }

    // Limpiar mensajes anteriores
    fun limpiarMensaje() {
        mensaje = ""
    }
}
