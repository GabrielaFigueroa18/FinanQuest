
package com.finanquest.sv.data.model

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val correo: String = "",
    val universidad: String = "",
    val moneda: String = "USD",
    val xp: Int = 0,
    val nivel: Int = 1,
    val mascota: String = "Gatito"
)
