package com.finanquest.sv

import com.finanquest.sv.data.model.Movimiento
import com.finanquest.sv.utils.FinanzasIntegrante2
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class FinanzasIntegrante2Test {
    private fun día(base: Long = System.currentTimeMillis(), desplazamiento: Int = 0): Long = Calendar.getInstance().apply {
        timeInMillis = base
        add(Calendar.DAY_OF_YEAR, desplazamiento)
    }.timeInMillis

    private fun inicioDía(valor: Long): Long = Calendar.getInstance().apply {
        timeInMillis = valor
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    @Test fun metaAceptaDatosValidosYFechaDeHoy() {
        assertNull(FinanzasIntegrante2.validarMeta("Laptop", 1000.0, 100.0, inicioDía(System.currentTimeMillis())))
        assertNull(FinanzasIntegrante2.validarMeta("  Viaje  ", 1000.0, 0.0, 0L))
    }

    @Test fun metaRechazaNombreObjetivoYAhorroInicialInvalidos() {
        assertEquals("Escribe el nombre de la meta", FinanzasIntegrante2.validarMeta(" ", 100.0, 0.0, 0L))
        assertEquals("El objetivo debe ser mayor que cero", FinanzasIntegrante2.validarMeta("Meta", 0.0, 0.0, 0L))
        assertEquals("El ahorro inicial debe estar entre cero y el objetivo", FinanzasIntegrante2.validarMeta("Meta", 100.0, 101.0, 0L))
        assertNotNull(FinanzasIntegrante2.validarMeta("Meta", Double.NaN, 0.0, 0L))
    }

    @Test fun metaRechazaFechaAnteriorAHoy() {
        assertEquals("La fecha límite no puede estar en el pasado", FinanzasIntegrante2.validarMeta("Meta", 100.0, 0.0, inicioDía(día(desplazamiento = -1))))
    }

    @Test fun aporteValidaFormatoRangoYDecimalesConComa() {
        assertNull(FinanzasIntegrante2.validarAporte(" 12,50 "))
        listOf("", "abc", "0", "-2", "NaN", "1000000001").forEach { assertNotNull("Debe rechazar: $it", FinanzasIntegrante2.validarAporte(it)) }
    }

    @Test fun categoriaValidaNombreLimiteYUmbral() {
        assertNull(FinanzasIntegrante2.validarCategoria("Comida", "250,50", .8))
        assertNotNull(FinanzasIntegrante2.validarCategoria(" ", "10", .8))
        assertNotNull(FinanzasIntegrante2.validarCategoria("Comida", "0", .8))
        assertNotNull(FinanzasIntegrante2.validarCategoria("Comida", "10", .49))
        assertTrue(FinanzasIntegrante2.validarUmbral(.5))
        assertTrue(FinanzasIntegrante2.validarUmbral(1.0))
        assertFalse(FinanzasIntegrante2.validarUmbral(1.01))
    }

    @Test fun presupuestoValidaCategoriaMontoYPeriodo() {
        assertNull(FinanzasIntegrante2.validarPresupuesto("Transporte", "80", "Mensual"))
        assertNull(FinanzasIntegrante2.validarPresupuesto("Comida", "12,75", "Semanal"))
        assertNotNull(FinanzasIntegrante2.validarPresupuesto("", "80", "Mensual"))
        assertNotNull(FinanzasIntegrante2.validarPresupuesto("Comida", "NaN", "Mensual"))
        assertNotNull(FinanzasIntegrante2.validarPresupuesto("Comida", "80", "Diario"))
    }

    @Test fun gastoPorCategoriaSumaSoloGastosDelPeriodo() {
        val desde = día(desplazamiento = -1)
        val datos = listOf(
            Movimiento(tipo = "Gasto", categoria = "Comida", monto = 10.25, fecha = desde),
            Movimiento(tipo = "Gasto", categoria = "comida", monto = 2.75, fecha = día()),
            Movimiento(tipo = "Ingreso", categoria = "Comida", monto = 50.0, fecha = día()),
            Movimiento(tipo = "Gasto", categoria = "Comida", monto = 99.0, fecha = día(desplazamiento = -2)),
            Movimiento(tipo = "Gasto", categoria = "Transporte", monto = 4.0, fecha = día())
        )
        assertEquals(13.0, FinanzasIntegrante2.gasto(datos, "COMIDA", desde), .001)
    }

    @Test fun totalesYBalanceDelReporteSeCalculanDesdeMovimientos() {
        val datos = listOf(
            Movimiento(tipo = "Ingreso", monto = 500.0, fecha = día()),
            Movimiento(tipo = "Gasto", monto = 125.5, fecha = día()),
            Movimiento(tipo = "Gasto", monto = 10.0, fecha = día(desplazamiento = -4))
        )
        val (ingresos, gastos, ahorro) = FinanzasIntegrante2.totales(datos, día(desplazamiento = -1))
        assertEquals(500.0, ingresos, .001)
        assertEquals(125.5, gastos, .001)
        assertEquals(374.5, ahorro, .001)
    }

    @Test fun rachaCuentaDiasConsecutivosDesdeHoyYSeCortaEnElPrimerHueco() {
        val hoy = día()
        val consecutivos = listOf(-2, -1, 0).map { Movimiento(fecha = día(hoy, it)) }
        assertEquals(3, FinanzasIntegrante2.racha(consecutivos, hoy))
        val conHueco = listOf(-3, -1, 0).map { Movimiento(fecha = día(hoy, it)) }
        assertEquals(2, FinanzasIntegrante2.racha(conHueco, hoy))
        assertEquals(0, FinanzasIntegrante2.racha(emptyList(), hoy))
    }

    @Test fun presupuestoSemanalIniciaLunesYMensualEnDiaUno() {
        val ahora = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 23, 16, 30, 0) }.timeInMillis
        val semanal = Calendar.getInstance().apply { timeInMillis = FinanzasIntegrante2.inicioPeriodoPresupuesto("Semanal", ahora) }
        assertEquals(Calendar.MONDAY, semanal.get(Calendar.DAY_OF_WEEK))
        assertEquals(0, semanal.get(Calendar.HOUR_OF_DAY))
        val mensual = Calendar.getInstance().apply { timeInMillis = FinanzasIntegrante2.inicioPeriodoPresupuesto("Mensual", ahora) }
        assertEquals(1, mensual.get(Calendar.DAY_OF_MONTH))
    }

    @Test fun reportesUsanMesActualTresMesesYAnoCalendario() {
        val ahora = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 23, 16, 30, 0) }.timeInMillis
        val mes = Calendar.getInstance().apply { timeInMillis = FinanzasIntegrante2.inicioPeriodoReporte("Este mes", ahora) }
        assertEquals(Calendar.OCTOBER, mes.get(Calendar.MONTH)); assertEquals(1, mes.get(Calendar.DAY_OF_MONTH))
        val trimestre = Calendar.getInstance().apply { timeInMillis = FinanzasIntegrante2.inicioPeriodoReporte("Últimos 3 meses", ahora) }
        assertEquals(Calendar.AUGUST, trimestre.get(Calendar.MONTH)); assertEquals(1, trimestre.get(Calendar.DAY_OF_MONTH))
        val año = Calendar.getInstance().apply { timeInMillis = FinanzasIntegrante2.inicioPeriodoReporte("Este año", ahora) }
        assertEquals(Calendar.JANUARY, año.get(Calendar.MONTH)); assertEquals(1, año.get(Calendar.DAY_OF_MONTH))
    }
}

