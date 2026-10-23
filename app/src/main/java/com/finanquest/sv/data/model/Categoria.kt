package com.finanquest.sv.data.model

data class Categoria(val id:String="", val nombre:String="", val limite:Double=0.0, val umbral:Double=0.8, val alertasActivas:Boolean=true, val creadaEn:Long=System.currentTimeMillis())
