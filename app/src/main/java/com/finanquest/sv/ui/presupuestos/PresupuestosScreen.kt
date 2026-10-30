package com.finanquest.sv.ui.presupuestos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.viewmodel.PresupuestoViewModel
import java.text.NumberFormat
import java.util.Locale

private val morado = Color(0xFF6846A5)
private val moradoOscuro = Color(0xFF452579)
private val lavanda = Color(0xFFF4EEFC)
private val tinta = Color(0xFF29223B)
private val gris = Color(0xFF776F84)
private val peligro = Color(0xFFE45A69)
private val verde = Color(0xFF159B76)
private val verdeClaro = Color(0xFFE3F7EE)
private val fondo = Color(0xFFF7F9F8)
private val sugeridas = listOf("Alimentación", "Salud", "Transporte", "Educación", "Hogar", "Entretenimiento", "Ahorro", "Servicios", "Belleza", "Salidas")
private fun moneda(n: Double) = NumberFormat.getCurrencyInstance(Locale.US).format(n)

@Composable
private fun IconoCategoria(nombre: String, modifier: Modifier = Modifier) {
    val texto = nombre.lowercase()
    val icono = when {
        listOf("comida", "alimento", "alimentación", "restaurante").any { it in texto } -> Icons.Default.Restaurant
        listOf("transporte", "bus", "gasolina").any { it in texto } -> Icons.Default.DirectionsCar
        listOf("belleza", "ropa", "compras").any { it in texto } -> Icons.Default.ShoppingBag
        listOf("salida", "entretenimiento", "cine").any { it in texto } -> Icons.Default.Celebration
        listOf("estudio", "educacion", "educación").any { it in texto } -> Icons.Default.School
        listOf("salud", "medicina", "farmacia").any { it in texto } -> Icons.Default.Favorite
        listOf("hogar", "casa", "vivienda") .any { it in texto } -> Icons.Default.Home
        listOf("ahorro", "inversión") .any { it in texto } -> Icons.Default.Savings
        listOf("servicio", "luz", "agua", "internet") .any { it in texto } -> Icons.Default.Receipt
        else -> Icons.Default.Category
    }
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = if (listOf("salud", "alimento", "ahorro", "hogar").any { it in texto }) verdeClaro else lavanda) {
        Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
            Icon(icono, contentDescription = null, tint = if (listOf("salud", "alimento", "ahorro", "hogar").any { it in texto }) verde else morado, modifier = Modifier.size(27.dp))
        }
    }
}

@Composable
private fun Cabecera(titulo: String, subtitulo: String, onVolver: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, "Volver al inicio", tint = tinta) }
        Column(Modifier.weight(1f)) {
            Text(titulo, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = tinta)
            Text(subtitulo, fontSize = 12.sp, color = gris)
        }
    }
}

@Composable
private fun TarjetaMorada(titulo: String, principal: String, descripcion: String, porcentaje: Float) {
    Card(shape = RoundedCornerShape(25.dp), colors = CardDefaults.cardColors(containerColor = moradoOscuro)) {
        Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(moradoOscuro, morado))).padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(titulo, color = Color(0xFFEADCF9), fontSize = 13.sp)
                    Text(principal, color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
            LinearProgressIndicator(progress = { porcentaje.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(9.dp), color = Color.White, trackColor = Color(0xFF9778C3))
            Text(descripcion, color = Color.White, fontSize = 13.sp)
        }
    }
}

@Composable
fun PresupuestosScreen(
    onVolver: () -> Unit = {},
    onCrear: () -> Unit = {},
    onCategorias: () -> Unit = {},
    onDetalle: (String) -> Unit = {},
    onReportes: () -> Unit = {},
    vm: PresupuestoViewModel = viewModel()
) {
    LaunchedEffect(vm) { vm.observar() }
    val activos = vm.presupuestos.filter { it.activo }
    val limite = activos.sumOf { it.limite }
    // Sumamos los gastos de cada presupuesto desde su inicio real.
    val gastado = activos.sumOf { vm.gasto(it.categoria, it.inicioPeriodo) }
    val restante = (limite - gastado).coerceAtLeast(0.0)
    val avance = if (limite > 0) (gastado / limite).toFloat() else 0f
    Column(Modifier.fillMaxSize().background(Color(0xFFFAF8FD)).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Cabecera("Mis presupuestos", "Organiza tu dinero con FinanQuest", onVolver)
        TarjetaMorada("TOTAL PRESUPUESTADO", moneda(limite), if (limite > 0) "Gastado: ${moneda(gastado)}  ·  Disponible: ${moneda(restante)}" else "Crea un presupuesto para comenzar", avance)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, null, tint = morado)
            Spacer(Modifier.width(8.dp))
            Text("Tus presupuestos", fontWeight = FontWeight.Bold, fontSize = 19.sp, color = tinta, modifier = Modifier.weight(1f))
            Text("${activos.size} activos", color = gris, fontSize = 12.sp)
        }
        vm.mensaje?.let { Text(it, color = peligro, fontSize = 12.sp) }
        if (activos.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(25.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Savings, null, tint = morado, modifier = Modifier.size(56.dp))
                    Text("¡Comienza a planificar!", fontWeight = FontWeight.Bold, color = tinta)
                    Text("Elige una categoría y cuánto puedes gastar cada semana o mes.", color = gris, fontSize = 13.sp)
                }
            }
        }
        activos.forEach { p ->
            val usado = vm.gasto(p.categoria, p.inicioPeriodo)
            val progreso = if (p.limite > 0) (usado / p.limite).toFloat() else 0f
            val alerta = progreso >= p.alertaUmbral.toFloat()
            Card(modifier = Modifier.fillMaxWidth().clickable { onDetalle(p.categoria) }, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconoCategoria(p.categoria)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.categoria, color = tinta, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("${p.periodo} · Límite ${moneda(p.limite)}", color = gris, fontSize = 12.sp)
                        }
                        Text("${(progreso * 100).toInt()}%", color = if (alerta) peligro else morado, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(progress = { progreso.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(9.dp), color = if (alerta) peligro else morado, trackColor = lavanda)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gastado: ${moneda(usado)}", color = gris, fontSize = 12.sp)
                        Text("Queda: ${moneda((p.limite-usado).coerceAtLeast(0.0))}", color = morado, fontSize = 12.sp)
                    }
                    if (alerta) Text(if (progreso >= 1f) "⚠ Has alcanzado o superado tu límite" else "⚠ Te estás acercando a tu límite", color = peligro, fontSize = 12.sp)
                }
            }
        }
        Button(onClick = onCrear, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = morado)) {
            Icon(Icons.Default.AddCircle, null); Spacer(Modifier.width(8.dp)); Text("Crear presupuesto")
        }
        OutlinedButton(onClick = onCategorias, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.Default.Category, null, tint = morado); Spacer(Modifier.width(8.dp)); Text("Mis categorías", color = morado)
        }
        TextButton(onClick = onReportes, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Ver reportes", color = morado) }
        Spacer(Modifier.height(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPresupuestoScreen(
    onVolver: () -> Unit = {},
    onGuardar: (String, String, String) -> Unit = { _, _, _ -> },
    vm: PresupuestoViewModel = viewModel()
) {
    LaunchedEffect(vm) { vm.observar() }
    var categoria by remember { mutableStateOf("") }
    var limite by remember { mutableStateOf("") }
    var periodo by remember { mutableStateOf("Mensual") }
    var expandido by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(Color(0xFFFAF8FD)).verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Cabecera("Nuevo presupuesto", "Define cuánto puedes gastar", onVolver)
        TarjetaMorada("PLANIFICA TUS GASTOS", "Tu dinero, tus metas", "Un presupuesto no descuenta dinero: te ayuda a controlar tus gastos.", 0f)
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Configura tu presupuesto", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = tinta)
                ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
                    OutlinedTextField(value = categoria, onValueChange = {}, readOnly = true, label = { Text("Categoría") }, modifier = Modifier.fillMaxWidth().menuAnchor(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandido) })
                    ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                        vm.categorias.forEach { item -> DropdownMenuItem(text = { Text(item.nombre) }, onClick = { categoria = item.nombre; expandido = false }) }
                    }
                }
                if (vm.categorias.isEmpty()) Text("Primero crea una categoría desde Mis categorías.", color = peligro, fontSize = 12.sp)
                OutlinedTextField(value = limite, onValueChange = { limite = it.filter { c -> c.isDigit() || c == '.' || c == ',' } }, label = { Text("Límite de gasto") }, leadingIcon = { Text("$") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("¿Cada cuánto quieres controlar tus gastos?", color = tinta)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Semanal", "Mensual").forEach { opcion -> FilterChip(selected = periodo == opcion, onClick = { periodo = opcion }, label = { Text(opcion) }) }
                }
                Text("🔔 Aviso: al llegar al 80% del límite, verás una advertencia en tu presupuesto.", color = gris, fontSize = 12.sp)
                vm.mensaje?.let { Text(it, color = peligro, fontSize = 12.sp) }
                Button(onClick = { onGuardar(categoria, limite, periodo) }, enabled = !vm.cargando && categoria.isNotBlank() && (limite.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = morado)) {
                    Text(if (vm.cargando) "Guardando…" else "Guardar presupuesto")
                }
            }
        }
    }
}

@Composable
fun CategoriasAlertasScreen(
    onVolver: () -> Unit = {},
    onGuardarCategoria: (String, String, Double) -> Unit = { _, _, _ -> },
    vm: PresupuestoViewModel = viewModel()
) {
    LaunchedEffect(vm) { vm.observar() }
    var creando by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var esperando by remember { mutableStateOf(false) }
    LaunchedEffect(vm.mensaje, vm.categorias.size, vm.cargando) {
        if (esperando && !vm.cargando && vm.mensaje.orEmpty().contains("guardad", ignoreCase = true)) {
            creando = false; esperando = false; nombre = ""
        }
    }
    val guardadas = vm.categorias.map { it.nombre.trim().lowercase() }.toSet()
    val faltantes = sugeridas.filter { it.lowercase() !in guardadas }
    Column(Modifier.fillMaxSize().background(fondo).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Cabecera("Mis categorías", "Dale identidad a cada gasto", onVolver)
        Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = moradoOscuro)) {
            Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(moradoOscuro, morado, verde))).padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Tu dinero, organizado ✨", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Personaliza tus categorías y descubre dónde gastas.", color = Color.White, fontSize = 13.sp)
                    }
                    Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Surface(color = Color.White.copy(alpha = 0.18f), shape = RoundedCornerShape(50)) {
                        Text("${vm.categorias.size} guardadas", Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = Color.White, fontSize = 12.sp)
                    }
                    Surface(color = Color.White.copy(alpha = 0.18f), shape = RoundedCornerShape(50)) {
                        Text("${faltantes.size} sugeridas", Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
        vm.mensaje?.let { mensaje ->
            if (mensaje.contains("PERMISSION_DENIED", true) || mensaje.contains("permission", true)) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0EF)), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = peligro)
                        Spacer(Modifier.width(9.dp))
                        Text("Firebase rechazó una consulta. Revisa las reglas de seguridad y que la sesión esté iniciada. Las categorías guardadas podrían estar incompletas.", color = peligro, fontSize = 12.sp)
                    }
                }
            } else Text(mensaje, color = if (mensaje.contains("guardad", true)) verde else peligro, fontSize = 12.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tus categorías", color = tinta, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("${vm.categorias.size} guardadas", color = verde, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        if (vm.categorias.isEmpty()) {
            Text("Todavía no hay categorías guardadas. Puedes añadir una de las sugeridas de abajo.", color = gris, fontSize = 13.sp)
        }
        vm.categorias.forEachIndexed { index, categoria ->
            val tono = if (index % 2 == 0) verde else morado
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconoCategoria(categoria.nombre)
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(categoria.nombre.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, color = tinta, fontSize = 16.sp)
                        Text("Lista para registrar gastos", color = gris, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.CheckCircle, "Guardada", tint = tono)
                }
            }
        }
        if (faltantes.isNotEmpty()) {
            Text("Explora categorías", color = tinta, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Estas son sugerencias. Pulsa Agregar para guardarlas en tu cuenta y usarlas en presupuestos.", color = gris, fontSize = 13.sp)
            faltantes.forEachIndexed { index, sugerencia ->
                val tono = if (index % 2 == 0) verde else morado
                Card(colors = CardDefaults.cardColors(containerColor = if (index % 2 == 0) verdeClaro else lavanda), shape = RoundedCornerShape(19.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconoCategoria(sugerencia)
                        Spacer(Modifier.width(12.dp))
                        Text(sugerencia, fontWeight = FontWeight.SemiBold, color = tinta, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            vm.limpiarMensaje()
                            onGuardarCategoria(sugerencia, "1", 0.8)
                        }, enabled = !vm.cargando) {
                            Icon(Icons.Default.Add, null, tint = tono, modifier = Modifier.size(17.dp))
                            Text("Agregar", color = tono, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = verdeClaro), shape = RoundedCornerShape(20.dp)) {
            Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, null, tint = verde, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Text("Las categorías organizan gastos. Los límites y alertas se configuran únicamente al crear presupuestos.", color = tinta, fontSize = 13.sp)
            }
        }
        if (creando) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Crea tu propia categoría", color = tinta, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it.take(40) }, label = { Text("Nombre") }, placeholder = { Text("Ejemplo: Mascotas") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Button(onClick = {
                        esperando = true
                        onGuardarCategoria(nombre.trim(), "1", 0.8)
                    }, enabled = nombre.isNotBlank() && !vm.cargando && !esperando, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = verde)) {
                        Text("Guardar categoría")
                    }
                    TextButton(onClick = { creando = false; esperando = false }) { Text("Cancelar", color = morado) }
                }
            }
        } else {
            Button(onClick = { creando = true; esperando = false }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = morado), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.AddCircle, null)
                Spacer(Modifier.width(8.dp))
                Text("Crear categoría personalizada")
            }
        }
        Spacer(Modifier.height(22.dp))
    }
}
