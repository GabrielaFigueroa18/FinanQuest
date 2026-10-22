package com.finanquest.sv.data.model

data class Movimiento(
    val id: String = "",
    val cuentaId: String = "",
    val tipo: String = "Gasto",
    val monto: Double = 0.0,
    val categoria: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val nota: String = "",
    val comercio: String = ""
)
