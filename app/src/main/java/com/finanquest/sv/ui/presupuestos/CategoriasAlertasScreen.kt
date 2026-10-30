package com.finanquest.sv.ui.presupuestos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.viewmodel.PresupuestoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasAlertasScreen(
    onVolver: () -> Unit = {},
    onGuardarCategoria: (String, String, Double) -> Unit = { _, _, _ -> },
    vm: PresupuestoViewModel = viewModel(),
) {
    PresupuestoEstilo {
        LaunchedEffect(vm) { vm.observar() }
        var eliminarId by rememberSaveable { mutableStateOf<String?>(null) }
        var editarId by rememberSaveable { mutableStateOf<String?>(null) }
        var editarLimite by rememberSaveable { mutableStateOf("") }
        var editarUmbral by rememberSaveable { mutableFloatStateOf(.8f) }
        var creando by rememberSaveable { mutableStateOf(false) }
        var nombre by rememberSaveable { mutableStateOf("") }
        var limite by rememberSaveable { mutableStateOf("") }
        var umbral by rememberSaveable { mutableFloatStateOf(0.8f) }
        var umbralGlobal by rememberSaveable { mutableFloatStateOf(0.8f) }

        var guardadoSolicitado by rememberSaveable { mutableStateOf(false) }

        // Se espera la confirmación del ViewModel antes de cerrar el formulario.
        LaunchedEffect(guardadoSolicitado, vm.mensaje, vm.categorias.size, vm.cargando) {
            val mensaje = vm.mensaje.orEmpty()
            if (guardadoSolicitado && !vm.cargando) {
                guardadoSolicitado = false
                if (mensaje == "Categoría guardada correctamente") {
                    creando = false
                    guardadoSolicitado = false
                    nombre = ""
                    limite = ""
                    umbral = 0.8f
                }
            }
        }

        Scaffold(
            containerColor = fondoPres,
            topBar = { PresupuestoBarra("Categorías y alertas", onVolver) },
        ) { padding ->
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PresupuestoIntroduccion(
                    "Cada gasto en su lugar",
                    "Configura categorías y umbrales de aviso.",
                )
                vm.mensaje?.let { mensaje ->
                    Text(
                        mensaje,
                        color =
                            if (mensaje.contains("guardad", ignoreCase = true)) verdePres
                            else rojoPres,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Mis categorías", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                    TextButton(
                        enabled = !vm.cargando,
                        onClick = {
                            creando = !creando
                            guardadoSolicitado = false
                            vm.limpiarMensaje()
                        },
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text(" Nueva")
                    }
                }
                if (vm.cargando && vm.categorias.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                vm.categorias.forEach { categoria ->
                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                        shape = RoundedCornerShape(22.dp),
                    ) {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .clickable {
                                        editarId = categoria.id
                                        editarLimite = categoria.limite.toString()
                                        editarUmbral = categoria.umbral.toFloat()
                                        vm.limpiarMensaje()
                                    }
                                    .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            PresupuestoIcono(categoria.nombre)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(categoria.nombre, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Editar límite y alerta",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                                Text(
                                    "Límite ${dineroPres(categoria.limite)} · alerta al ${(categoria.umbral * 100).toInt()}%",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                )
                            }
                            Switch(
                                checked = categoria.alertasActivas,
                                enabled = !vm.cargando,
                                onCheckedChange = { activas ->
                                    vm.actualizarAlertasCategoria(
                                        categoria,
                                        activas,
                                        categoria.umbral,
                                    )
                                },
                            )
                        }
                    }
                }
                if (vm.categorias.isEmpty() && !vm.cargando) {
                    Text(
                        "Todavía no hay categorías. Crea una para comenzar.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (vm.categorias.isNotEmpty()) {
                    HorizontalDivider()
                    Text("Umbral de alerta", fontWeight = FontWeight.Bold)
                    Text(
                        "Configura el porcentaje para todas tus categorías.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Avisar al ${(umbralGlobal * 100).toInt()}%",
                        fontWeight = FontWeight.Medium,
                    )
                    Slider(
                        value = umbralGlobal,
                        onValueChange = { umbralGlobal = it },
                        valueRange = 0.5f..1f,
                    )
                    OutlinedButton(
                        onClick = { vm.actualizarUmbrales(umbralGlobal.toDouble()) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !vm.cargando,
                    ) {
                        Text("Aplicar umbral a todas")
                    }
                }

                if (creando) {
                    HorizontalDivider()
                    Text("Nueva categoría", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it.take(40) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre de la categoría") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = limite,
                        onValueChange = { texto ->
                            limite = texto.filter { it.isDigit() || it == '.' || it == ',' }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Límite mensual de gasto") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Text("$") },
                        singleLine = true,
                    )
                    Text("Umbral de alerta: ${(umbral * 100).toInt()}%")
                    Slider(
                        value = umbral,
                        onValueChange = { umbral = it },
                        valueRange = 0.5f..1f,
                        enabled = !vm.cargando,
                    )
                    Text(
                        "La categoría se guardará con el umbral elegido. La activación de alertas se puede modificar desde la lista.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                    Button(
                        onClick = {
                            guardadoSolicitado = true
                            onGuardarCategoria(nombre.trim(), limite, umbral.toDouble())
                        },
                        enabled =
                            !vm.cargando &&
                                !guardadoSolicitado &&
                                nombre.isNotBlank() &&
                                (limite.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (vm.cargando) "Guardando…" else "Guardar categoría")
                    }
                    OutlinedButton(
                        onClick = {
                            creando = false
                            guardadoSolicitado = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Cancelar")
                    }
                } else {
                    Button(
                        enabled = !vm.cargando,
                        onClick = {
                            creando = true
                            vm.limpiarMensaje()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text(" Nueva categoría")
                    }
                }
            }
        }
        val editando = vm.categorias.find { it.id == editarId }
        if (editando != null)
            AlertDialog(
                onDismissRequest = { if (!vm.cargando) editarId = null },
                title = { Text("Editar ${editando.nombre}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = editarLimite,
                            onValueChange = { editarLimite = it },
                            label = { Text("Límite mensual de referencia") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                        )
                        Text("Alerta al ${(editarUmbral * 100).toInt()}%")
                        Slider(
                            value = editarUmbral,
                            onValueChange = { editarUmbral = it },
                            valueRange = .5f..1f,
                        )
                        Text(
                            "Este límite es una referencia. Los límites de tus presupuestos se editan en su detalle."
                        )
                        OutlinedButton(
                            enabled = !vm.cargando,
                            onClick = {
                                eliminarId = editando.id
                                editarId = null
                                vm.limpiarMensaje()
                            },
                        ) {
                            Text("Eliminar categoría")
                        }
                        vm.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = !vm.cargando,
                        onClick = {
                            vm.editarCategoria(editando, editarLimite, editarUmbral.toDouble()) {
                                editarId = null
                            }
                        },
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(enabled = !vm.cargando, onClick = { editarId = null }) {
                        Text("Cancelar")
                    }
                },
            )
        val eliminando = vm.categorias.find { it.id == eliminarId }
        if (eliminando != null)
            AlertDialog(
                onDismissRequest = { if (!vm.cargando) eliminarId = null },
                title = { Text("¿Eliminar ${eliminando.nombre}?") },
                text = {
                    Column {
                        Text(
                            "Los movimientos anteriores se conservarán. Debes eliminar primero su presupuesto, si existe."
                        )
                        vm.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = !vm.cargando,
                        onClick = { vm.eliminarCategoria(eliminando) { eliminarId = null } },
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(enabled = !vm.cargando, onClick = { eliminarId = null }) {
                        Text("Cancelar")
                    }
                },
            )
    }
}
