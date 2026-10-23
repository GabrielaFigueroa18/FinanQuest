package com.finanquest.sv.ui.finanzas

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.data.model.Cuenta
import com.finanquest.sv.data.model.Movimiento
import com.finanquest.sv.viewmodel.CuentaViewModel
import com.finanquest.sv.viewmodel.MovimientoViewModel
import com.finanquest.sv.viewmodel.PresupuestoViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val fondo = Color(0xFFF7F8FA)
private val verde = Color(0xFF16A34A)
private val morado = Color(0xFF7C3AED)
private val tinta = Color(0xFF20232A)
private val gris = Color(0xFF7B808A)
private fun dinero(v: Double) = NumberFormat.getCurrencyInstance(Locale("es", "SV")).format(v)
private fun fecha(v: Long) = SimpleDateFormat("dd/MM/yyyy", Locale("es", "SV")).format(Date(v))

@Composable
fun MovimientosScreen(onInicio: () -> Unit, onCuentas: () -> Unit, onNuevo: () -> Unit, vm: MovimientoViewModel = viewModel()) {
    LaunchedEffect(Unit) { vm.observar() }
    var busqueda by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf("Todos") }
    val nombres = vm.cuentas.associate { it.id to it.nombre }
    val lista = vm.movimientos.filter { m ->
        (filtro == "Todos" || m.tipo == filtro) &&
            (m.categoria.contains(busqueda, true) || m.nota.contains(busqueda, true) ||
                (nombres[m.cuentaId]?.contains(busqueda, true) == true))
    }
    Scaffold(containerColor = fondo, topBar = { TopBar("FinanQuest SV", onInicio) },
        bottomBar = { BottomBar("Movimientos", onInicio, {}, onCuentas) },
        floatingActionButton = { FloatingActionButton(onClick = onNuevo, containerColor = verde, contentColor = Color.White) { Icon(Icons.Filled.Add, "Añadir transacción") } }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(horizontal = 18.dp)) {
            Spacer(Modifier.height(10.dp))
            Text("Historial de Movimientos", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = tinta)
            Text("Consulta y filtra tus transacciones.", color = gris, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            vm.mensaje?.let { Aviso(it) }
            OutlinedTextField(busqueda, { busqueda = it }, Modifier.fillMaxWidth(), placeholder = { Text("Buscar por categoría o cuenta") },
                leadingIcon = { Icon(Icons.Filled.Search, null) }, singleLine = true, shape = RoundedCornerShape(15.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Todos", "Ingreso", "Gasto").forEach { FilterChip(filtro == it, { filtro = it }, label = { Text(if (it == "Ingreso") "Ingresos" else if (it == "Gasto") "Gastos" else it) }) }
            }
            when {
                vm.cargando && lista.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = verde) }
                lista.isEmpty() -> Vacio("Aún no tienes movimientos", "Registra tu primer ingreso o gasto.")
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 80.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(lista, key = { it.id }) { MovimientoFila(it, nombres[it.cuentaId] ?: "Cuenta") }
                }
            }
        }
    }
}

@Composable
fun NuevoMovimientoScreen(onVolver: () -> Unit, onExito: () -> Unit, onInicio: () -> Unit, onCuentas: () -> Unit, vm: MovimientoViewModel = viewModel()) {
    val presupuestoViewModel: PresupuestoViewModel = viewModel()
    LaunchedEffect(Unit) { vm.observar(); vm.limpiarMensaje(); presupuestoViewModel.observar() }
    var tipo by remember { mutableStateOf("Gasto") }
    var monto by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var cuentaId by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    var dia by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var menuCat by remember { mutableStateOf(false) }
    var menuCuenta by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    Scaffold(containerColor = fondo, topBar = { TopBar("Nueva transacción", onVolver) },
        bottomBar = { BottomBar("Movimientos", onInicio, onVolver, onCuentas) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(18.dp).verticalScroll(rememberScrollState()).imePadding()) {
            Row(Modifier.fillMaxWidth().background(Color(0xFFEAE5F7), RoundedCornerShape(14.dp)).padding(4.dp)) {
                listOf("Gasto", "Ingreso").forEach { t -> TextButton(onClick = { tipo = t; categoria = "" }, modifier = Modifier.weight(1f)) {
                    Text(t, color = if (tipo == t) morado else gris, fontWeight = FontWeight.Bold)
                } }
            }
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(monto, { monto = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(14) },
                Modifier.fillMaxWidth(), label = { Text("Monto") }, prefix = { Text("$") }, placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(14.dp))
            Spacer(Modifier.height(12.dp))
            Box {
                OutlinedButton(onClick = { menuCat = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(categoria.ifBlank { "Seleccionar categoría" }); Spacer(Modifier.weight(1f)); Icon(Icons.Filled.ArrowDropDown, null)
                }
                DropdownMenu(menuCat, { menuCat = false }) {
                    (if (tipo == "Gasto") (listOf("Comida", "Transporte", "Compras", "Educación", "Vivienda", "Salud", "Entretenimiento", "Otros") + presupuestoViewModel.categorias.map { it.nombre }).distinct() else listOf("Salario", "Beca", "Apoyo familiar", "Venta", "Reembolso", "Otros"))
                        .forEach { DropdownMenuItem(text = { Text(it) }, onClick = { categoria = it; menuCat = false }) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Box {
                OutlinedButton(onClick = { menuCuenta = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(vm.cuentas.firstOrNull { it.id == cuentaId }?.nombre ?: "Seleccionar cuenta"); Spacer(Modifier.weight(1f)); Icon(Icons.Filled.ArrowDropDown, null)
                }
                DropdownMenu(menuCuenta, { menuCuenta = false }) {
                    vm.cuentas.filter { it.activa }.forEach { c ->
                        DropdownMenuItem(text = { Text("${c.nombre} · ${dinero(c.saldo)}") }, onClick = { cuentaId = c.id; menuCuenta = false })
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { dateDialog(context) { dia = it } }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.CalendarMonth, null); Spacer(Modifier.width(8.dp)); Text(fecha(dia))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(nota, { nota = it.take(160) }, Modifier.fillMaxWidth(), label = { Text("Nota (opcional)") },
                minLines = 2, maxLines = 3, shape = RoundedCornerShape(14.dp))
            vm.mensaje?.let { Spacer(Modifier.height(8.dp)); Aviso(it) }
            Spacer(Modifier.height(14.dp))
            Button(onClick = { vm.crear(tipo, monto, categoria, cuentaId, dia, nota, onExito) }, enabled = !vm.cargando,
                modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = verde)) {
                if (vm.cargando) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                else { Icon(Icons.Filled.CheckCircle, null); Spacer(Modifier.width(8.dp)); Text("Guardar transacción") }
            }
        }
    }
}

@Composable
fun CuentasScreen(onInicio: () -> Unit, onMovimientos: () -> Unit, onCrear: () -> Unit, onDetalle: (Cuenta) -> Unit, vm: CuentaViewModel = viewModel()) {
    LaunchedEffect(Unit) { vm.observar() }
    Scaffold(containerColor = fondo, topBar = { TopBar("FinanQuest SV", onInicio) },
        bottomBar = { BottomBar("Cuentas", onInicio, onMovimientos, {}) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = onCrear, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Nueva") }, containerColor = verde, contentColor = Color.White) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(horizontal = 18.dp)) {
            Text("Mis cuentas", Modifier.padding(top = 10.dp), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = tinta)
            Card(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.fillMaxWidth().padding(17.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SALDO TOTAL", fontSize = 11.sp, color = gris, fontWeight = FontWeight.Bold)
                    Text(dinero(vm.cuentas.filter { it.activa }.sumOf { it.saldo }), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = morado)
                }
            }
            vm.mensaje?.let { Aviso(it) }
            when {
                vm.cargando && vm.cuentas.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = verde) }
                vm.cuentas.isEmpty() -> Vacio("Aún no tienes cuentas", "Crea una cuenta para registrar movimientos.")
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 85.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(vm.cuentas, key = { it.id }) { cuenta ->
                        Card(Modifier.fillMaxWidth().clickable { onDetalle(cuenta) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AccountBalance, null, tint = gris, modifier = Modifier.size(30.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(cuenta.nombre, fontWeight = FontWeight.Bold, color = tinta)
                                    Text(cuenta.tipo + if (cuenta.activa) " · Activa" else " · Inactiva", fontSize = 12.sp, color = gris)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(dinero(cuenta.saldo), fontWeight = FontWeight.Bold, color = tinta)
                                    Text("Ver detalles", color = verde, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormularioCuentaScreen(cuenta: Cuenta?, onVolver: () -> Unit, onGuardado: () -> Unit, vm: CuentaViewModel = viewModel()) {
    LaunchedEffect(Unit) { vm.observar(); vm.limpiarMensaje() }
    var nombre by remember(cuenta?.id) { mutableStateOf(cuenta?.nombre.orEmpty()) }
    var tipo by remember(cuenta?.id) { mutableStateOf(cuenta?.tipo ?: "Ahorro") }
    var saldo by remember(cuenta?.id) { mutableStateOf(if (cuenta == null) "" else cuenta.saldo.toString()) }
    var descripcion by remember(cuenta?.id) { mutableStateOf(cuenta?.descripcion.orEmpty()) }
    var confirmar by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    Scaffold(containerColor = fondo, topBar = { TopBar(if (cuenta == null) "Nueva cuenta" else "Editar cuenta") { confirmar = true } }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(18.dp).verticalScroll(rememberScrollState()).imePadding()) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(nombre, { nombre = it.take(50) }, Modifier.fillMaxWidth(), label = { Text("Nombre de cuenta") }, placeholder = { Text("Ej. Cuenta de ahorro") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                    Box {
                        OutlinedButton(onClick = { menu = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(tipo); Spacer(Modifier.weight(1f)); Icon(Icons.Filled.ArrowDropDown, null)
                        }
                        DropdownMenu(menu, { menu = false }) {
                            listOf("Ahorro", "Efectivo", "Cuenta corriente", "Tarjeta de crédito", "Otro").forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = { tipo = it; menu = false })
                            }
                        }
                    }
                    if (cuenta == null) OutlinedTextField(saldo, { saldo = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(14) },
                        Modifier.fillMaxWidth(), label = { Text("Saldo inicial") }, placeholder = { Text("$ 0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(12.dp))
                    else Text("Saldo actual: ${dinero(cuenta.saldo)}", fontWeight = FontWeight.Bold, color = morado)
                    OutlinedTextField(descripcion, { descripcion = it.take(140) }, Modifier.fillMaxWidth(), label = { Text("Descripción (opcional)") }, minLines = 2, maxLines = 3, shape = RoundedCornerShape(12.dp))
                    vm.mensaje?.let { Aviso(it) }
                    Button(onClick = {
                        if (cuenta == null) vm.crear(nombre, tipo, saldo.trim().replace(",", ".").toDoubleOrNull() ?: Double.NaN, descripcion, onGuardado)
                        else vm.actualizar(cuenta.copy(nombre = nombre, tipo = tipo, descripcion = descripcion), onGuardado)
                    }, enabled = !vm.cargando, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = verde)) {
                        if (vm.cargando) CircularProgressIndicator(Modifier.size(19.dp), color = Color.White)
                        else Text(if (cuenta == null) "Crear cuenta" else "Guardar cambios")
                    }
                    OutlinedButton(onClick = { confirmar = true }, modifier = Modifier.fillMaxWidth()) { Text("Cancelar", color = morado) }
                }
            }
        }
    }
    if (confirmar) AlertDialog(onDismissRequest = { confirmar = false }, title = { Text("¿Deseas cancelar?") },
        text = { Text("Los cambios que no hayas guardado se perderán.") },
        confirmButton = { TextButton(onClick = onVolver) { Text("Sí") } },
        dismissButton = { TextButton(onClick = { confirmar = false }) { Text("No") } })
}

@Composable
fun DetalleCuentaScreen(cuenta: Cuenta, onVolver: () -> Unit, onEditar: () -> Unit, vm: MovimientoViewModel = viewModel()) {
    LaunchedEffect(Unit) { vm.observar() }
    val lista = vm.movimientos.filter { it.cuentaId == cuenta.id }
    Scaffold(containerColor = fondo, topBar = { TopBar(cuenta.nombre, onVolver) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(18.dp)) {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("SALDO ACTUAL", color = gris, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(dinero(cuenta.saldo), fontSize = 30.sp, color = morado, fontWeight = FontWeight.Bold)
                    Text("${cuenta.tipo} · ${if (cuenta.activa) "Activa" else "Inactiva"}", color = gris)
                    if (cuenta.descripcion.isNotBlank()) Text(cuenta.descripcion, color = gris)
                    OutlinedButton(onClick = onEditar, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Icon(Icons.Filled.Edit, null); Text("Editar cuenta") }
                }
            }
            Text("Movimientos de esta cuenta", Modifier.padding(vertical = 14.dp), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = tinta)
            if (lista.isEmpty()) Vacio("Sin movimientos todavía", "Los movimientos de esta cuenta aparecerán aquí.")
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(lista, key = { it.id }) { MovimientoFila(it, cuenta.nombre) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(titulo: String, volver: () -> Unit) {
    TopAppBar(title = { Text(titulo, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
        navigationIcon = { IconButton(onClick = volver) { Icon(Icons.Filled.ArrowBack, "Volver") } },
        actions = { Icon(Icons.Filled.Pets, null, tint = verde); Spacer(Modifier.width(14.dp)) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = fondo))
}
@Composable
private fun BottomBar(sel: String, inicio: () -> Unit, movimientos: () -> Unit, cuentas: () -> Unit) {
    NavigationBar(containerColor = Color.White) {
        listOf(Triple("Inicio", Icons.Filled.Home, inicio), Triple("Movimientos", Icons.Filled.AccountBalanceWallet, movimientos), Triple("Cuentas", Icons.Filled.AccountBalance, cuentas)).forEach {
            NavigationBarItem(selected = it.first == sel, onClick = it.third, icon = { Icon(it.second, it.first) }, label = { Text(it.first, fontSize = 10.sp) })
        }
    }
}
@Composable
private fun MovimientoFila(m: Movimiento, cuenta: String) {
    val ingreso = m.tipo == "Ingreso"
    Card(shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (ingreso) Icons.Filled.ArrowDownward else Icons.Filled.ShoppingBag, null, tint = if (ingreso) verde else gris)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(m.categoria, fontWeight = FontWeight.SemiBold, color = tinta)
                Text(m.nota.ifBlank { cuenta }, color = gris, fontSize = 11.sp)
                Text("${fecha(m.fecha)} · $cuenta", color = gris, fontSize = 10.sp)
            }
            Text("${if (ingreso) "+" else "−"}${dinero(m.monto)}", color = if (ingreso) verde else tinta, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}
@Composable
private fun Vacio(titulo: String, detalle: String) {
    Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.AccountBalanceWallet, null, tint = morado, modifier = Modifier.size(38.dp))
        Text(titulo, fontWeight = FontWeight.Bold, color = tinta)
        Text(detalle, color = gris, fontSize = 12.sp)
    }
}
@Composable
private fun Aviso(texto: String) {
    val exito = texto.endsWith("correctamente")
    val fondoAviso = if (exito) Color(0xFFE7F7EC) else Color(0xFFFFF3E0)
    val textoAviso = if (exito) Color(0xFF176B35) else Color(0xFF9A5B00)
    Surface(color = fondoAviso, shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (exito) Icon(Icons.Filled.CheckCircle, null, tint = verde)
            Spacer(Modifier.width(if (exito) 8.dp else 0.dp))
            Text(texto, color = textoAviso, fontSize = 13.sp)
        }
    }
}
private fun dateDialog(context: android.content.Context, seleccion: (Long) -> Unit) {
    val today = java.util.Calendar.getInstance()
    android.app.DatePickerDialog(context, { _, year, month, day ->
        val selected = java.util.Calendar.getInstance().apply {
            set(year, month, day, 12, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        seleccion(selected.timeInMillis)
    }, today.get(java.util.Calendar.YEAR), today.get(java.util.Calendar.MONTH), today.get(java.util.Calendar.DAY_OF_MONTH)).show()
}
