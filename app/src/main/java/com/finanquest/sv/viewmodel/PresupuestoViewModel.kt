
package com.finanquest.sv.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

import com.finanquest.sv.data.model.Categoria
import com.finanquest.sv.data.model.Presupuesto
import com.finanquest.sv.data.model.Movimiento
import com.finanquest.sv.data.repository.PresupuestoRepository
import com.finanquest.sv.utils.FinanzasIntegrante2

import com.google.firebase.firestore.ListenerRegistration

class PresupuestoViewModel : ViewModel() {

 private val repo = PresupuestoRepository()

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



 fun observar() {

  if (
   categoriasListener != null &&
   presupuestosListener != null &&
   movimientosListener != null
  ) {
   return
  }

  cargando = true

  if (categoriasListener == null) {
   categoriasListener = repo.escucharCategorias { lista, error ->

    categorias = lista

    if (error != null) {
     mensaje = error
    }

    cargando = false
   }
  }

  if (presupuestosListener == null) {
   presupuestosListener = repo.escucharPresupuestos { lista, error ->

    presupuestos = lista

    if (error != null) {
     mensaje = error
    }
   }
  }

  if (movimientosListener == null) {
   movimientosListener = repo.escucharMovimientos { lista, error ->

    movimientos = lista

    if (error != null) {
     mensaje = error
    }
   }
  }
 }


 fun guardarCategoria(
  nombre: String,
  limiteTexto: String,
  umbral: Double,
  onExito: () -> Unit
 ) {

  val errorValidacion = FinanzasIntegrante2.validarCategoria(
   nombre,
   limiteTexto,
   umbral
  )

  val limite = limiteTexto
   .trim()
   .replace(",", ".")
   .toDoubleOrNull()

  if (errorValidacion != null || limite == null) {
   mensaje = errorValidacion ?: "Límite no válido"
   return
  }

  cargando = true
  mensaje = null

  repo.guardarCategoria(
   nombre,
   limite,
   umbral
  ) { error ->

   cargando = false

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

 fun actualizarAlertasCategoria(
  categoria: Categoria,
  activas: Boolean,
  umbral: Double
 ) {

  if (!FinanzasIntegrante2.validarUmbral(umbral)) {
   mensaje = "El umbral debe estar entre 50% y 100%"
   return
  }

  mensaje = null

  repo.actualizarAlertasCategoria(
   categoria,
   activas,
   umbral
  ) { error ->

   mensaje = if (error == null) {
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
  onExito: () -> Unit
 ) {

  val errorValidacion = FinanzasIntegrante2.validarPresupuesto(
   categoria,
   limiteTexto,
   periodo
  )

  val limite = limiteTexto
   .trim()
   .replace(",", ".")
   .toDoubleOrNull()

  if (errorValidacion != null || limite == null) {
   mensaje = errorValidacion ?: "Límite no válido"
   return
  }

  cargando = true
  mensaje = null

  repo.guardarPresupuesto(
   categoria,
   limite,
   periodo
  ) { error ->

   cargando = false

   if (error == null) {
    mensaje = "Presupuesto guardado correctamente"
    onExito()
   } else {
    mensaje = error
   }
  }
 }



 fun gasto(
  categoria: String,
  inicio: Long
 ): Double {

  return FinanzasIntegrante2.gasto(
   movimientos,
   categoria,
   inicio
  )
 }



 fun limpiarMensaje() {
  mensaje = null
 }

 override fun onCleared() {

  categoriasListener?.remove()
  presupuestosListener?.remove()
  movimientosListener?.remove()

  categoriasListener = null
  presupuestosListener = null
  movimientosListener = null

  super.onCleared()
 }
}
