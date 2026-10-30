package com.finanquest.sv.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.finanquest.sv.data.model.MetaAhorro
import com.finanquest.sv.data.model.AporteMeta
import com.finanquest.sv.data.repository.MetaRepository
import com.finanquest.sv.utils.FinanzasIntegrante2
import com.google.firebase.firestore.ListenerRegistration
import kotlin.math.abs
import kotlin.math.round

class MetaViewModel : ViewModel() {
 private val repo = MetaRepository()
 private var listener: ListenerRegistration? = null
 private var aportesListener: ListenerRegistration? = null

 var metas by mutableStateOf<List<MetaAhorro>>(emptyList())
  private set
 var aportes by mutableStateOf<List<AporteMeta>>(emptyList())
  private set
 var cargando by mutableStateOf(false)
  private set
 var mensaje by mutableStateOf<String?>(null)
  private set

 fun observar() {
  if (listener != null) return
  cargando = true
  listener = repo.escuchar { lista, error ->
   metas = lista
   cargando = false
   if (error != null) mensaje = error
  }
 }

 fun crear(nombre: String, objetivo: Double, inicial: Double, fecha: Long, icono: String, onExito: () -> Unit) {
  if (cargando) return
  val error = FinanzasIntegrante2.validarMeta(nombre, objetivo, inicial, fecha)
  if (error != null) { mensaje = error; return }
  cargando = true
  mensaje = null
  repo.crear(nombre, objetivo, inicial, fecha, icono) { resultado ->
   cargando = false
   mensaje = resultado ?: "Meta guardada correctamente"
   if (resultado == null) onExito()
  }
 }

 // Creación con ahorro inicial desde una cuenta. No altera crear() ni aportar().
 fun crearConCuenta(
  nombre: String, objetivo: Double, inicial: Double, fecha: Long,
  icono: String, cuentaId: String, solicitudId: String, onExito: () -> Unit
 ) {
  if (cargando) return
  val error = FinanzasIntegrante2.validarMeta(nombre, objetivo, inicial, fecha)
  if (error != null) { mensaje = error; return }
  if (inicial <= 0 || cuentaId.isBlank()) {
   mensaje = "Selecciona una cuenta para el ahorro inicial"; return
  }
  if (abs(inicial * 100 - round(inicial * 100)) > 0.000001) {
   mensaje = "El ahorro inicial admite máximo dos decimales"; return
  }
  cargando = true
  mensaje = null
  repo.crearConCuenta(nombre, objetivo, inicial, fecha, icono, cuentaId, solicitudId) { resultado ->
   cargando = false
   mensaje = resultado ?: "Meta creada y ahorro descontado correctamente"
   if (resultado == null) onExito()
  }
 }

 fun aportar(meta: MetaAhorro, cuentaId: String, texto: String, solicitudId: String, onExito: () -> Unit) {
  if (cargando) return
  val error = FinanzasIntegrante2.validarAporte(texto)
  val monto = texto.trim().replace(",", ".").toDoubleOrNull()
  if (error != null || monto == null || !monto.isFinite() || monto <= 0) {
   mensaje = error ?: "Ingresa un aporte válido"; return
  }
  if (abs(monto * 100 - round(monto * 100)) > 0.000001) {
   mensaje = "Usa un máximo de dos decimales"; return
  }
  if (cuentaId.isBlank() || solicitudId.isBlank()) {
   mensaje = "Selecciona una cuenta de origen"; return
  }
  if (monto > meta.objetivo - meta.actual + 0.000001) {
   mensaje = "El aporte supera lo pendiente de la meta"; return
  }
  cargando = true
  mensaje = null
  repo.aportar(meta.id, cuentaId, monto, solicitudId) { resultado ->
   cargando = false
   mensaje = resultado ?: "Aporte guardado correctamente"
   if (resultado == null) onExito()
  }
 }

 fun observarAportes(metaId: String) {
  aportesListener?.remove()
  aportes = emptyList()
  aportesListener = repo.escucharAportes(metaId) { lista, error ->
   aportes = lista
   if (error != null) mensaje = error
  }
 }

 fun limpiarMensaje() { mensaje = null }

 override fun onCleared() {
  listener?.remove()
  aportesListener?.remove()
  super.onCleared()
 }
}
