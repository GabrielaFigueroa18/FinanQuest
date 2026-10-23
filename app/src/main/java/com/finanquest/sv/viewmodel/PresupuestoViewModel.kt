package com.finanquest.sv.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.finanquest.sv.data.model.Categoria
import com.finanquest.sv.data.model.Presupuesto
import com.finanquest.sv.data.model.Movimiento
import com.finanquest.sv.data.repository.PresupuestoRepository
import com.google.firebase.firestore.ListenerRegistration
import com.finanquest.sv.utils.FinanzasIntegrante2

class PresupuestoViewModel:ViewModel(){
 private val repo=PresupuestoRepository();private var categoriasListener:ListenerRegistration?=null;private var presupuestosListener:ListenerRegistration?=null;private var movimientosListener:ListenerRegistration?=null
 var categorias by mutableStateOf<List<Categoria>>(emptyList());private set
 var presupuestos by mutableStateOf<List<Presupuesto>>(emptyList());private set
 var movimientos by mutableStateOf<List<Movimiento>>(emptyList());private set
 var cargando by mutableStateOf(false);private set
 var mensaje by mutableStateOf<String?>(null);private set
 fun observar(){if(categoriasListener!=null&&presupuestosListener!=null&&movimientosListener!=null)return;cargando=true;categoriasListener=repo.escucharCategorias{v,e->categorias=v;if(e!=null)mensaje=e;cargando=false};presupuestosListener=repo.escucharPresupuestos{v,e->presupuestos=v;if(e!=null)mensaje=e};movimientosListener=repo.escucharMovimientos{v,e->movimientos=v;if(e!=null)mensaje=e}}
 fun guardarCategoria(nombre:String,limiteTexto:String,umbral:Double,onExito:()->Unit){val err=FinanzasIntegrante2.validarCategoria(nombre,limiteTexto,umbral);val limite=limiteTexto.trim().replace(",",".").toDoubleOrNull();if(err!=null||limite==null){mensaje=err?:"Límite no válido";return};repo.guardarCategoria(nombre,limite,umbral){e->mensaje=e?:"Categoría guardada";if(e==null)onExito()}}
 fun actualizarAlertasCategoria(categoria:Categoria,activas:Boolean,umbral:Double){if(!FinanzasIntegrante2.validarUmbral(umbral)){mensaje="El umbral debe estar entre 50% y 100%";return};repo.actualizarAlertasCategoria(categoria,activas,umbral){e->if(e!=null)mensaje=e}}
 fun guardarPresupuesto(categoria:String,limiteTexto:String,periodo:String,onExito:()->Unit){val err=FinanzasIntegrante2.validarPresupuesto(categoria,limiteTexto,periodo);val lim=limiteTexto.trim().replace(",",".").toDoubleOrNull();if(err!=null||lim==null){mensaje=err?:"Límite no válido";return};repo.guardarPresupuesto(categoria,lim,periodo){e->mensaje=e?:"Presupuesto guardado";if(e==null)onExito()}}
 fun gasto(categoria:String,inicio:Long):Double=FinanzasIntegrante2.gasto(movimientos,categoria,inicio)
 fun limpiarMensaje(){mensaje=null}
 override fun onCleared(){categoriasListener?.remove();presupuestosListener?.remove();movimientosListener?.remove();super.onCleared()}
}
