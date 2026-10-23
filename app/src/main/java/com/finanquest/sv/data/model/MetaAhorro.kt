package com.finanquest.sv.data.model

data class MetaAhorro(val id:String="", val nombre:String="", val objetivo:Double=0.0, val actual:Double=0.0, val fechaLimite:Long=0L, val icono:String="🎯", val creadaEn:Long=System.currentTimeMillis(), val completada:Boolean=false)
