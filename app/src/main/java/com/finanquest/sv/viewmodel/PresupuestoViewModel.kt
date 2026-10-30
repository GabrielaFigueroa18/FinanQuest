package com.finanquest.sv.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.finanquest.sv.data.model.Categoria
import com.finanquest.sv.data.model.Movimiento
import com.finanquest.sv.data.model.Presupuesto
import com.finanquest.sv.data.repository.PresupuestoRepository
import com.finanquest.sv.utils.FinanzasIntegrante2
import com.google.firebase.firestore.ListenerRegistration

class PresupuestoViewModel : ViewModel() {

    private var recibidas = mutableSetOf<String>()
    private var guardando = false
    private var uid: String? = null
    private val repo = PresupuestoRepository()
    private val auth = com.finanquest.sv.data.firebase.FirebaseProvider.auth
    private val authListener =
        com.google.firebase.auth.FirebaseAuth.AuthStateListener { observar() }

    private var categoriasListener: ListenerRegistration? = null
    private var presupuestosListener: ListenerRegistration? = null
    private var movimientosListener: ListenerRegistration? = null

    var categorias by mutableStateOf<List<Categoria>>(emptyList())
        private set

    var presupuestos by mutableStateOf<List<Presupuesto>>(emptyList())
        private set

    var movimientos by mutableStateOf<List<Movimiento>>(emptyList())
        private set

    var cargando by mutableStateOf(false)
        private set

    var mensaje by mutableStateOf<String?>(null)
        private set

    init {
        auth.addAuthStateListener(authListener)
    }

    fun observar() {
        val actual = com.finanquest.sv.data.firebase.FirebaseProvider.auth.currentUser?.uid
        if (uid != actual) {
            categoriasListener?.remove()
            presupuestosListener?.remove()
            movimientosListener?.remove()
            categoriasListener = null
            presupuestosListener = null
            movimientosListener = null
            categorias = emptyList()
            presupuestos = emptyList()
            movimientos = emptyList()
            recibidas.clear()
            mensaje = null
            uid = actual
        }

        if (
            categoriasListener != null &&
                presupuestosListener != null &&
                movimientosListener != null
        ) {
            return
        }

        cargando = true

        if (categoriasListener == null) {
            categoriasListener =
                repo.escucharCategorias { lista, error ->
                    categorias = lista
                    recibidas.add("categorias")
                    cargando = guardando || recibidas.size < 3

                    if (error != null) {
                        mensaje = error
                    }
                }
        }

        if (presupuestosListener == null) {
            presupuestosListener =
                repo.escucharPresupuestos { lista, error ->
                    presupuestos = lista
                    recibidas.add("presupuestos")
                    cargando = guardando || recibidas.size < 3

                    if (error != null) {
                        mensaje = error
                    }
                }
        }

        if (movimientosListener == null) {
            movimientosListener =
                repo.escucharMovimientos { lista, error ->
                    movimientos = lista
                    recibidas.add("movimientos")
                    cargando = guardando || recibidas.size < 3

                    if (error != null) {
                        mensaje = error
                    }
                }
        }
    }

    fun guardarCategoria(nombre: String, limiteTexto: String, umbral: Double, onExito: () -> Unit) {

        val errorValidacion = FinanzasIntegrante2.validarCategoria(nombre, limiteTexto, umbral)

        val limite = limiteTexto.trim().replace(",", ".").toDoubleOrNull()

        if (errorValidacion != null || limite == null) {
            mensaje = errorValidacion ?: "Límite no válido"
            return
        }

        if (guardando) return
        guardando = true
        cargando = true
        mensaje = null

        repo.guardarCategoria(nombre, limite, umbral) { error ->
            guardando = false
            cargando = recibidas.size < 3

            if (error == null) {
                mensaje = "Categoría guardada correctamente"
                onExito()
            } else {
                mensaje = error
            }
        }
    }

    // ==============================
    // ACTUALIZAR ALERTAS
    // ==============================

    fun actualizarAlertasCategoria(categoria: Categoria, activas: Boolean, umbral: Double) {

        if (!FinanzasIntegrante2.validarUmbral(umbral)) {
            mensaje = "El umbral debe estar entre 50% y 100%"
            return
        }

        if (guardando) return
        guardando = true
        cargando = true
        mensaje = null

        repo.actualizarAlertasCategoria(categoria, activas, umbral) { error ->
            guardando = false
            cargando = false
            mensaje =
                if (error == null) {
                    "Configuración de alertas actualizada"
                } else {
                    error
                }
        }
    }

    fun guardarPresupuesto(
        categoria: String,
        limiteTexto: String,
        periodo: String,
        onExito: () -> Unit,
    ) {

        val errorValidacion =
            FinanzasIntegrante2.validarPresupuesto(categoria, limiteTexto, periodo)

        val limite = limiteTexto.trim().replace(",", ".").toDoubleOrNull()

        if (errorValidacion != null || limite == null) {
            mensaje = errorValidacion ?: "Límite no válido"
            return
        }

        if (guardando) return
        guardando = true
        cargando = true
        mensaje = null

        val seleccionada =
            categorias.find { it.nombre == categoria }
                ?: run {
                    guardando = false
                    cargando = false
                    mensaje = "Selecciona una categoría existente"
                    return
                }
        repo.guardarPresupuesto(seleccionada.id, limite, periodo) { error ->
            guardando = false
            cargando = recibidas.size < 3

            if (error == null) {
                mensaje = "Presupuesto guardado correctamente"
                onExito()
            } else {
                mensaje = error
            }
        }
    }

    fun gasto(categoria: String, inicio: Long): Double {

        return FinanzasIntegrante2.gasto(movimientos, categoria, inicio)
    }

    fun eliminarCategoria(categoria: Categoria, onExito: () -> Unit) {
        if (guardando) return
        guardando = true
        cargando = true
        mensaje = null
        repo.eliminarCategoria(categoria) { error ->
            guardando = false
            cargando = false
            mensaje = error
            if (error == null) onExito()
        }
    }

    fun editarCategoria(categoria: Categoria, limite: String, umbral: Double, onExito: () -> Unit) {
        val error = FinanzasIntegrante2.validarCategoria(categoria.nombre, limite, umbral)
        if (error != null) {
            mensaje = error
            return
        }
        if (guardando) return
        guardando = true
        cargando = true
        mensaje = null
        repo.editarCategoria(categoria, limite.replace(',', '.').toDouble(), umbral) { fallo ->
            guardando = false
            cargando = false
            mensaje = fallo
            if (fallo == null) onExito()
        }
    }

    fun gastoPresupuesto(p: Presupuesto): Double =
        FinanzasIntegrante2.gastoPresupuesto(movimientos, p.categoria, p.periodo)

    fun alerta(p: Presupuesto): Boolean {
        val c = categorias.find { it.id == p.categoriaId }
        return p.activo &&
            c?.alertasActivas != false &&
            p.limite > 0 &&
            gastoPresupuesto(p) / p.limite >= (c?.umbral ?: p.alertaUmbral)
    }

    fun eliminarPresupuesto(id: String, onExito: () -> Unit) {
        if (guardando) return
        guardando = true
        cargando = true
        mensaje = null
        repo.eliminarPresupuesto(id) { error ->
            guardando = false
            cargando = false
            mensaje = error
            if (error == null) onExito()
        }
    }

    fun actualizarUmbrales(umbral: Double) {
        if (guardando) return
        guardando = true
        cargando = true
        mensaje = null
        repo.actualizarUmbrales(categorias, umbral) { error ->
            guardando = false
            cargando = false
            mensaje = error ?: "Configuración de alertas actualizada"
        }
    }

    fun limpiarMensaje() {
        mensaje = null
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authListener)

        categoriasListener?.remove()
        presupuestosListener?.remove()
        movimientosListener?.remove()

        categoriasListener = null
        presupuestosListener = null
        movimientosListener = null

        super.onCleared()
    }
}
