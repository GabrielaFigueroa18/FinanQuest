package com.finanquest.sv.data.model

data class Cuenta(
    val id: String = "",
    val nombre: String = "",
    val tipo: String = "Ahorro",
    val saldo: Double = 0.0,
    val descripcion: String = "",
    val activa: Boolean = true,
    val creadaEn: Long = System.currentTimeMillis()
)
