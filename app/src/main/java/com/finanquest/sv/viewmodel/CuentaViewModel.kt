package com.finanquest.sv.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.finanquest.sv.data.firebase.FirebaseProvider
import com.finanquest.sv.data.model.Cuenta
import com.finanquest.sv.data.repository.CuentaRepository
import com.google.firebase.firestore.ListenerRegistration

class CuentaViewModel : ViewModel() {
    private val repository = CuentaRepository()
    private var listener: ListenerRegistration? = null
    private var listenerUid: String? = null
    var cuentas by mutableStateOf<List<Cuenta>>(emptyList()); private set
    var cargando by mutableStateOf(false); private set
    var mensaje by mutableStateOf<String?>(null); private set

    fun observar() {
        val uid = FirebaseProvider.auth.currentUser?.uid
        if (uid == listenerUid && listener != null) return
        listener?.remove(); listener = null; listenerUid = uid
        cuentas = emptyList(); cargando = true
        listener = repository.escucharCuentas { lista, error ->
            cuentas = lista; cargando = false; mensaje = error
        }
    }

    fun crear(nombre: String, tipo: String, saldo: Double, descripcion: String, onExito: () -> Unit) {
        val error = validar(nombre, tipo, saldo)
        if (error != null) { mensaje = error; return }
        cargando = true
        repository.crearCuenta(nombre, tipo, saldo, descripcion) { errorFirebase ->
            cargando = false; mensaje = errorFirebase ?: "Cuenta creada correctamente"
            if (errorFirebase == null) onExito()
        }
    }

    fun actualizar(cuenta: Cuenta, onExito: () -> Unit) {
        val error = validar(cuenta.nombre, cuenta.tipo, 0.0)
        if (error != null) { mensaje = error; return }
        cargando = true
        repository.actualizarCuenta(cuenta) { errorFirebase ->
            cargando = false; mensaje = errorFirebase ?: "Cuenta actualizada correctamente"
            if (errorFirebase == null) onExito()
        }
    }

    fun limpiarMensaje() { mensaje = null }
    private fun validar(nombre: String, tipo: String, saldo: Double): String? = when {
        nombre.isBlank() -> "Escribe el nombre de la cuenta"
        nombre.trim().length > 50 -> "El nombre no puede superar 50 caracteres"
        tipo.isBlank() -> "Selecciona el tipo de cuenta"
        !saldo.isFinite() || saldo < 0 || saldo > 1000000000 -> "El saldo inicial debe ser un monto válido"
        else -> null
    }
    override fun onCleared() { listener?.remove(); super.onCleared() }
}

