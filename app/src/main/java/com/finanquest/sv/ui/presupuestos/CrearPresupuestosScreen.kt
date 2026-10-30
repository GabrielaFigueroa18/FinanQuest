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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.viewmodel.PresupuestoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPresupuestoScreen(
    onVolver: () -> Unit = {},
    onCategorias: () -> Unit = {},
    onGuardar: (String, String, String) -> Unit = { _, _, _ -> },
    vm: PresupuestoViewModel = viewModel(),
) {
    PresupuestoEstilo {
        LaunchedEffect(vm) {
            vm.observar()
            vm.limpiarMensaje()
        }
        var categoria by rememberSaveable { mutableStateOf("") }
        var limite by rememberSaveable { mutableStateOf("") }
        var periodo by rememberSaveable { mutableStateOf("Mensual") }
        var expandido by rememberSaveable { mutableStateOf(false) }

        Scaffold(
            containerColor = fondoPres,
            topBar = { PresupuestoBarra("Nuevo presupuesto", onVolver) },
        ) { padding ->
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                PresupuestoIntroduccion(
                    "Dale un plan a tu dinero",
                    "Establece cuánto deseas gastar en cada categoría.",
                )
                Text(
                    "01  ·  Categoría y límite",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                ExposedDropdownMenuBox(
                    expanded = expandido,
                    onExpandedChange = { expandido = it },
                ) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {},
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { Text("Categoría") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandido) },
                        singleLine = true,
                    )
                    ExposedDropdownMenu(
                        expanded = expandido,
                        onDismissRequest = { expandido = false },
                    ) {
                        vm.categorias.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.nombre) },
                                onClick = {
                                    categoria = item.nombre
                                    expandido = false
                                },
                            )
                        }
                    }
                }
                if (vm.categorias.isEmpty()) {
                    TextButton(onClick = onCategorias) { Text("Crear una categoría") }
                }
                OutlinedTextField(
                    value = limite,
                    onValueChange = { texto ->
                        limite = texto.filter { it.isDigit() || it == '.' || it == ',' }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Límite de gasto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("$") },
                    singleLine = true,
                )
                Text(
                    "02  ·  Frecuencia",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
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
                    "Un presupuesto por categoría. Guardar reemplaza su límite y período anteriores.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                vm.mensaje?.let { Text(it, color = rojoPres) }
                Button(
                    onClick = { onGuardar(categoria, limite, periodo) },
                    enabled =
                        !vm.cargando &&
                            categoria.isNotBlank() &&
                            (limite.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                ) {
                    Text(if (vm.cargando) "Guardando…" else "Guardar presupuesto")
                }
            }
        }
    }
}
