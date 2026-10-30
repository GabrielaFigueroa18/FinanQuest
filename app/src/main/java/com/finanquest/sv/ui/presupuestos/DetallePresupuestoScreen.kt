package com.finanquest.sv.ui.presupuestos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.finanquest.sv.viewmodel.PresupuestoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallePresupuestoScreen(id: String, vm: PresupuestoViewModel, onVolver: () -> Unit) {
    PresupuestoEstilo {
        LaunchedEffect(vm) {
            vm.observar()
            vm.limpiarMensaje()
        }
        val presupuesto = vm.presupuestos.find { it.id == id }
        var confirmar by rememberSaveable { mutableStateOf(false) }
        Scaffold(
            containerColor = fondoPres,
            topBar = { PresupuestoBarra("Detalle del presupuesto", onVolver) },
        ) { padding ->
            Column(
                Modifier.fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (presupuesto == null) {
                    Text(
                        if (vm.cargando) "Cargando presupuesto…"
                        else "Este presupuesto ya no está disponible"
                    )
                    return@Column
                }
                var limite by
                    rememberSaveable(id, presupuesto.limite) {
                        mutableStateOf(presupuesto.limite.toString())
                    }
                var periodo by
                    rememberSaveable(id, presupuesto.periodo) {
                        mutableStateOf(presupuesto.periodo)
                    }
                val gasto = vm.gastoPresupuesto(presupuesto)
                PresupuestoIntroduccion(
                    presupuesto.categoria,
                    "Presupuesto ${presupuesto.periodo.lowercase()}",
                )
                PresupuestoResumen(gasto, presupuesto.limite, "Gastado en el período actual")
                Text("Ajusta tu presupuesto", style = MaterialTheme.typography.titleMedium)
                if (vm.alerta(presupuesto))
                    Text(
                        "Has alcanzado el umbral de alerta",
                        color = MaterialTheme.colorScheme.error,
                    )
                OutlinedTextField(
                    value = limite,
                    onValueChange = { limite = it },
                    label = { Text("Límite de gasto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Semanal", "Mensual").forEach { opcion ->
                        FilterChip(
                            selected = periodo == opcion,
                            onClick = { periodo = opcion },
                            label = { Text(opcion) },
                        )
                    }
                }
                Text(
                    "El gasto se calcula desde el lunes o desde el primer día del mes actual. Cambiar el límite no modifica tus movimientos."
                )
                vm.mensaje?.let { Text(it) }
                Button(
                    onClick = {
                        vm.guardarPresupuesto(presupuesto.categoria, limite, periodo, onVolver)
                    },
                    enabled = !vm.cargando,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Guardar cambios")
                }
                OutlinedButton(
                    onClick = { confirmar = true },
                    enabled = !vm.cargando,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Eliminar presupuesto")
                }
            }
        }
        if (confirmar)
            AlertDialog(
                onDismissRequest = { confirmar = false },
                title = { Text("¿Eliminar presupuesto?") },
                text = { Text("Tus movimientos y la categoría se conservarán.") },
                confirmButton = {
                    TextButton(
                        enabled = !vm.cargando,
                        onClick = {
                            vm.eliminarPresupuesto(id) {
                                confirmar = false
                                onVolver()
                            }
                        },
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } },
            )
    }
}
