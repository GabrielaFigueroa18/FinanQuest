package com.finanquest.sv.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.finanquest.sv.data.firebase.FirebaseProvider
import com.finanquest.sv.data.model.Cuenta
import com.finanquest.sv.data.model.Movimiento
import com.finanquest.sv.data.repository.CuentaRepository
import com.finanquest.sv.data.repository.MovimientoRepository
import com.google.firebase.firestore.ListenerRegistration

class MovimientoViewModel : ViewModel() {
    private val movimientosRepository = MovimientoRepository()
    private val cuentasRepository = CuentaRepository()
    private var movimientosListener: ListenerRegistration? = null
    private var cuentasListener: ListenerRegistration? = null
    private var listenerUid: String? = null
    var movimientos by mutableStateOf<List<Movimiento>>(emptyList()); private set
    var cuentas by mutableStateOf<List<Cuenta>>(emptyList()); private set
    var cargando by mutableStateOf(false); private set
    var mensaje by mutableStateOf<String?>(null); private set

    fun observar() {
        val uid = FirebaseProvider.auth.currentUser?.uid
        if (uid == listenerUid && movimientosListener != null && cuentasListener != null) return
        movimientosListener?.remove(); cuentasListener?.remove()
        movimientosListener = null; cuentasListener = null; listenerUid = uid
        movimientos = emptyList(); cuentas = emptyList(); cargando = true
        movimientosListener = movimientosRepository.escucharMovimientos { lista, error ->
            movimientos = lista; cargando = false
            if (error != null) mensaje = error
        }
        cuentasListener = cuentasRepository.escucharCuentas { lista, error ->
            cuentas = lista
            if (error != null) mensaje = error
        }
    }

    fun crear(tipo: String, montoTexto: String, categoria: String, cuentaId: String, fecha: Long, nota: String, onExito: () -> Unit) {
        val monto = montoTexto.trim().replace(",", ".").toDoubleOrNull()
        val error = when {
            tipo !in setOf("Ingreso", "Gasto") -> "Selecciona si es un ingreso o gasto"
            monto == null || !monto.isFinite() || monto <= 0 -> "Ingresa un monto mayor que cero"
            monto > 1_000_000_000 -> "El monto supera el máximo permitido"
            categoria.isBlank() -> "Selecciona una categoría"
            cuentaId.isBlank() -> "Selecciona una cuenta"
            fecha > System.currentTimeMillis() + 86_400_000 -> "La fecha no puede ser futura"
            else -> null
        }
        if (error != null) { mensaje = error; return }
        cargando = true; mensaje = null
        movimientosRepository.crearMovimiento(
            Movimiento(cuentaId = cuentaId, tipo = tipo, monto = monto!!,
                categoria = categoria, fecha = fecha, nota = nota.trim())
        ) { errorFirebase ->
            cargando = false; mensaje = errorFirebase ?: "Movimiento guardado correctamente"
            if (errorFirebase == null) onExito()
        }
    }

    fun limpiarMensaje() { mensaje = null }
    override fun onCleared() {
        movimientosListener?.remove(); cuentasListener?.remove()
        super.onCleared()
    }
}


