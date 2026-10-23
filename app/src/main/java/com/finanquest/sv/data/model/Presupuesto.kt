package com.finanquest.sv.data.model

data class Presupuesto(val id:String="", val categoriaId:String="", val categoria:String="", val limite:Double=0.0, val periodo:String="Mensual", val inicioPeriodo:Long=0L, val alertaUmbral:Double=0.8, val activo:Boolean=true)
