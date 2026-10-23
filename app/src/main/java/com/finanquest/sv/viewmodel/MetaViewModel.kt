package com.finanquest.sv.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.finanquest.sv.data.model.MetaAhorro
import com.finanquest.sv.data.model.AporteMeta
import com.finanquest.sv.data.repository.MetaRepository
import com.google.firebase.firestore.ListenerRegistration
import com.finanquest.sv.utils.FinanzasIntegrante2

class MetaViewModel:ViewModel(){
 private val repo=MetaRepository(); private var listener:ListenerRegistration?=null; private var aportesListener:ListenerRegistration?=null
 var metas by mutableStateOf<List<MetaAhorro>>(emptyList()); private set
 var aportes by mutableStateOf<List<AporteMeta>>(emptyList()); private set
 var cargando by mutableStateOf(false); private set
 var mensaje by mutableStateOf<String?>(null); private set
 fun observar(){if(listener!=null)return;cargando=true;listener=repo.escuchar{lista,error->metas=lista;cargando=false;mensaje=error}}
 fun crear(nombre:String,objetivo:Double,inicial:Double,fecha:Long,icono:String,onExito:()->Unit){val err=FinanzasIntegrante2.validarMeta(nombre,objetivo,inicial,fecha);if(err!=null){mensaje=err;return};cargando=true;mensaje=null;repo.crear(nombre,objetivo,inicial,fecha,icono){e->cargando=false;mensaje=e?:"Meta guardada";if(e==null)onExito()}}
 fun aportar(meta:MetaAhorro,texto:String,onExito:()->Unit){val error=FinanzasIntegrante2.validarAporte(texto);val monto=texto.trim().replace(",",".").toDoubleOrNull();if(error!=null||monto==null){mensaje=error?:"Ingresa un aporte válido";return};cargando=true;repo.aportar(meta,monto){e->cargando=false;mensaje=e?:"Aporte guardado";if(e==null)onExito()}}
 fun observarAportes(metaId:String){aportesListener?.remove();aportes=emptyList();aportesListener=repo.escucharAportes(metaId){lista,error->aportes=lista;if(error!=null)mensaje=error}}
 fun limpiarMensaje(){mensaje=null}
 override fun onCleared(){listener?.remove();aportesListener?.remove();super.onCleared()}
}
