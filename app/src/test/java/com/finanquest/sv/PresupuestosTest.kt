package com.finanquest.sv

import com.finanquest.sv.data.model.Movimiento
import com.finanquest.sv.utils.FinanzasIntegrante2 as Finanzas
import java.util.Calendar
import org.junit.Assert.*
import org.junit.Test

class PresupuestosTest {
    private fun fecha(mes: Int, dia: Int): Long =
        Calendar.getInstance()
            .apply {
                clear()
                set(2026, mes - 1, dia, 12, 0)
            }
            .timeInMillis

    @Test
    fun renuevaMesSinArrastrarGastosNiFechasFuturas() {
        val movimientos =
            listOf(
                Movimiento(
                    tipo = "Gasto",
                    categoria = "Comida",
                    monto = 50.0,
                    fecha = fecha(9, 30),
                ),
                Movimiento(
                    tipo = "Gasto",
                    categoria = "Comida",
                    monto = 10.0,
                    fecha = fecha(10, 1),
                ),
                Movimiento(
                    tipo = "Gasto",
                    categoria = "Comida",
                    monto = 100.0,
                    fecha = fecha(11, 1),
                ),
                Movimiento(
                    tipo = "Ingreso",
                    categoria = "Comida",
                    monto = 80.0,
                    fecha = fecha(10, 2),
                ),
            )
        assertEquals(
            10.0,
            Finanzas.gastoPresupuesto(movimientos, "Comida", "Mensual", fecha(10, 30)),
            0.0,
        )
    }

    @Test
    fun renuevaSemanaElLunesAunqueCambieElMes() {
        val movimientos =
            listOf(
                Movimiento(
                    tipo = "Gasto",
                    categoria = "Comida",
                    monto = 8.0,
                    fecha = fecha(10, 25),
                ),
                Movimiento(
                    tipo = "Gasto",
                    categoria = "Comida",
                    monto = 12.0,
                    fecha = fecha(10, 26),
                ),
                Movimiento(tipo = "Gasto", categoria = "Comida", monto = 3.0, fecha = fecha(11, 1)),
            )
        assertEquals(
            15.0,
            Finanzas.gastoPresupuesto(movimientos, "Comida", "Semanal", fecha(11, 1)),
            0.0,
        )
        assertEquals(
            0.0,
            Finanzas.gastoPresupuesto(movimientos, "Comida", "Semanal", fecha(11, 2)),
            0.0,
        )
    }

    @Test
    fun sumaCentavosSinErrorBinarioYFiltraCategorias() {
        val movimientos =
            listOf(0.1, 0.2).map {
                Movimiento(tipo = "Gasto", categoria = " comida ", monto = it, fecha = fecha(10, 2))
            } + Movimiento(tipo = "Gasto", categoria = "Otro", monto = 100.0, fecha = fecha(10, 2))
        assertEquals(
            0.3,
            Finanzas.gastoPresupuesto(movimientos, "COMIDA", "Mensual", fecha(10, 30)),
            0.0,
        )
    }

    @Test
    fun normalizaNombreConAcentosEspaciosYMayusculas() {
        assertEquals("alimentacion", Finanzas.idCategoria("  ALIMENTACIÓN  "))
        assertEquals("salud_y_bienestar", Finanzas.idCategoria("Salud y bienestar"))
        assertNotNull(Finanzas.validarCategoria("!!!", "100", .8))
    }

    @Test
    fun rechazaLimitesNoFinitosYUmbralesInvalidos() {
        listOf("NaN", "Infinity", "-1", "0", "1.2.3").forEach {
            assertNotNull(Finanzas.validarPresupuesto("Comida", it, "Mensual"))
        }
        assertNotNull(Finanzas.validarCategoria("Comida", "100", Double.NaN))
        assertNotNull(Finanzas.validarCategoria("Comida", "100", .49))
    }
}
