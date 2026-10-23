package com.finanquest.sv.data.repository

import com.finanquest.sv.data.firebase.FirebaseProvider
import com.finanquest.sv.data.model.MetaAhorro
import com.finanquest.sv.data.model.AporteMeta
import com.google.firebase.firestore.ListenerRegistration

class MetaRepository {
    private fun ref() = FirebaseProvider.auth.currentUser?.uid?.let { FirebaseProvider.firestore.collection("usuarios").document(it) }
    fun escuchar(onResult:(List<MetaAhorro>,String?)->Unit):ListenerRegistration? {
        val r=ref()?.collection("metas") ?: run { onResult(emptyList(),"Inicia sesión para consultar tus metas"); return null }
        return r.addSnapshotListener { s,e -> if(e!=null) onResult(emptyList(),e.localizedMessage ?: "No se pudieron cargar las metas") else onResult(s?.documents.orEmpty().map { d -> MetaAhorro(d.id,d.getString("nombre")?:"",d.getDouble("objetivo")?:0.0,d.getDouble("actual")?:0.0,d.getLong("fechaLimite")?:0L,d.getString("icono")?:"🎯",d.getLong("creadaEn")?:0L,d.getBoolean("completada")?:false) },null) }
    }
    fun crear(nombre:String, objetivo:Double, inicial:Double, fecha:Long, icono:String, cb:(String?)->Unit) {
        val u=ref() ?: run { cb("Inicia sesión para guardar metas"); return }; val d=u.collection("metas").document()
        val datos=mapOf("nombre" to nombre.trim(),"objetivo" to objetivo,"actual" to inicial,"fechaLimite" to fecha,"icono" to icono,"creadaEn" to System.currentTimeMillis(),"completada" to (inicial>=objetivo))
        val batch=FirebaseProvider.firestore.batch();batch.set(d,datos)
        if(inicial>0)batch.set(d.collection("aportes").document(),mapOf("monto" to inicial,"fecha" to System.currentTimeMillis(),"nota" to "Ahorro inicial"))
        batch.commit().addOnSuccessListener { cb(null) }.addOnFailureListener { cb(it.localizedMessage ?: "No se pudo guardar la meta") }
    }
    fun aportar(meta:MetaAhorro,monto:Double,cb:(String?)->Unit) {
        val u=ref() ?: run { cb("Inicia sesión nuevamente"); return }; val m=u.collection("metas").document(meta.id); val a=m.collection("aportes").document()
        FirebaseProvider.firestore.runTransaction { tx -> val snap=tx.get(m); if(!snap.exists()) throw IllegalStateException("La meta ya no existe"); val nuevo=(snap.getDouble("actual")?:0.0)+monto; val objetivo=snap.getDouble("objetivo")?:0.0; tx.update(m,mapOf("actual" to nuevo,"completada" to (nuevo>=objetivo))); tx.set(a,mapOf("monto" to monto,"fecha" to System.currentTimeMillis())); null }
            .addOnSuccessListener { cb(null) }.addOnFailureListener { cb(it.localizedMessage ?: "No se pudo guardar el aporte") }
    }
    fun escucharAportes(id:String,onResult:(List<AporteMeta>,String?)->Unit):ListenerRegistration? {
        val m=ref()?.collection("metas")?.document(id) ?: run { onResult(emptyList(),"Inicia sesión nuevamente"); return null }
        return m.collection("aportes").addSnapshotListener { s,e -> if(e!=null) onResult(emptyList(),e.localizedMessage ?: "No se pudo cargar la actividad") else onResult(s?.documents.orEmpty().map { AporteMeta(it.id,it.getDouble("monto")?:0.0,it.getLong("fecha")?:0L,it.getString("nota")?:"") }.sortedByDescending{it.fecha},null) }
    }
}
