package com.finanquest.sv.utils

import com.finanquest.sv.data.model.Movimiento
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.Normalizer
import java.util.Calendar
import java.util.Locale

object FinanzasIntegrante2 {
    fun idCategoria(nombre: String): String =
        Normalizer.normalize(nombre.trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')

    fun gastoPresupuesto(
        movimientos: List<Movimiento>,
        categoria: String,
        periodo: String,
        ahora: Long = System.currentTimeMillis(),
    ): Double {
        val inicio = inicioPeriodoPresupuesto(periodo, ahora)
        return movimientos
            .filter {
                it.tipo.equals("Gasto", true) &&
                    it.categoria.trim().equals(categoria.trim(), true) &&
                    it.fecha in inicio..ahora &&
                    it.monto.isFinite() &&
                    it.monto > 0
            }
            .fold(BigDecimal.ZERO) { total, movimiento ->
                total + BigDecimal.valueOf(movimiento.monto).setScale(2, RoundingMode.HALF_UP)
            }
            .toDouble()
    }

    private fun monto(texto: String): Double? = texto.trim().replace(",", ".").toDoubleOrNull()

    fun validarMeta(
        nombre: String,
        objetivo: Double,
        inicial: Double,
        fechaLimite: Long,
        hoy: Long = inicioDia(System.currentTimeMillis()),
    ): String? =
        when {
            nombre.isBlank() -> "Escribe el nombre de la meta"
            nombre.trim().length > 60 -> "El nombre no puede superar 60 caracteres"
            !objetivo.isFinite() || objetivo <= 0 -> "El objetivo debe ser mayor que cero"
            !inicial.isFinite() || inicial < 0 || inicial > objetivo ->
                "El ahorro inicial debe estar entre cero y el objetivo"
            fechaLimite > 0 && fechaLimite < hoy -> "La fecha límite no puede estar en el pasado"
            else -> null
        }

    fun validarAporte(texto: String): String? {
        val valor = monto(texto)
        return if (valor == null || !valor.isFinite() || valor <= 0 || valor > 1_000_000_000)
            "Ingresa un aporte válido mayor que cero"
        else null
    }

    fun validarCategoria(nombre: String, limiteTexto: String, umbral: Double): String? {
        val limite = monto(limiteTexto)
        return when {
            nombre.isBlank() -> "Escribe el nombre de la categoría"
            idCategoria(nombre).isBlank() -> "Escribe un nombre de categoría válido"
            nombre.trim().length > 40 -> "El nombre no puede superar 40 caracteres"
            limite == null || !limite.isFinite() || limite <= 0 ->
                "El límite debe ser mayor que cero"
            umbral !in .5..1.0 -> "Selecciona un umbral entre 50% y 100%"
            else -> null
        }
    }

    fun validarPresupuesto(categoria: String, limiteTexto: String, periodo: String): String? {
        val limite = monto(limiteTexto)
        return if (
            categoria.isBlank() ||
                limite == null ||
                !limite.isFinite() ||
                limite <= 0 ||
                periodo !in setOf("Semanal", "Mensual")
        )
            "Completa una categoría, un límite válido y un periodo"
        else null
    }

    fun validarUmbral(umbral: Double): Boolean = umbral in .5..1.0

    fun gasto(movimientos: List<Movimiento>, categoria: String, desde: Long): Double =
        movimientos
            .asSequence()
            .filter {
                it.tipo.equals("Gasto", true) &&
                    it.categoria.equals(categoria, true) &&
                    it.fecha >= desde
            }
            .sumOf { it.monto }

    fun totales(movimientos: List<Movimiento>, desde: Long): Triple<Double, Double, Double> {
        val periodo = movimientos.filter { it.fecha >= desde }
        val ingresos = periodo.filter { it.tipo.equals("Ingreso", true) }.sumOf { it.monto }
        val gastos = periodo.filter { it.tipo.equals("Gasto", true) }.sumOf { it.monto }
        return Triple(ingresos, gastos, ingresos - gastos)
    }

    fun racha(movimientos: List<Movimiento>, ahora: Long = System.currentTimeMillis()): Int {
        val diasActivos = movimientos.map { inicioDia(it.fecha) }.toSet()
        val dia =
            Calendar.getInstance().apply {
                timeInMillis = ahora
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        var total = 0
        while (diasActivos.contains(dia.timeInMillis)) {
            total++
            dia.add(Calendar.DAY_OF_YEAR, -1)
        }
        return total
    }

    fun inicioPeriodoPresupuesto(periodo: String, ahora: Long = System.currentTimeMillis()): Long =
        Calendar.getInstance()
            .apply {
                timeInMillis = ahora
                if (periodo == "Semanal") {
                    val diasDesdeLunes = (get(Calendar.DAY_OF_WEEK) + 5) % 7
                    add(Calendar.DAY_OF_YEAR, -diasDesdeLunes)
                } else set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            .timeInMillis

    fun inicioPeriodoReporte(periodo: String, ahora: Long = System.currentTimeMillis()): Long =
        Calendar.getInstance()
            .apply {
                timeInMillis = ahora
                when (periodo) {
                    "Últimos 3 meses" -> {
                        set(Calendar.DAY_OF_MONTH, 1)
                        add(Calendar.MONTH, -2)
                    }
                    "Este año" -> {
                        set(Calendar.MONTH, Calendar.JANUARY)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    else -> set(Calendar.DAY_OF_MONTH, 1)
                }
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            .timeInMillis

    private fun inicioDia(valor: Long): Long =
        Calendar.getInstance()
            .apply {
                timeInMillis = valor
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            .timeInMillis
}
