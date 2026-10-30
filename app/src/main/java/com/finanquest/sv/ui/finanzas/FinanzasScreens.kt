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
fun NuevoMovimientoScreen(
    onVolver: () -> Unit,
    onExito: () -> Unit,
    onInicio: () -> Unit,
    onCuentas: () -> Unit,
    vm: MovimientoViewModel = viewModel()
) {
    val presupuestoViewModel: PresupuestoViewModel = viewModel()

    LaunchedEffect(Unit) {
        vm.observar()
        vm.limpiarMensaje()
        presupuestoViewModel.observar()
    }

    var tipo by remember { mutableStateOf("Gasto") }
    var monto by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var cuentaId by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    var dia by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var menuCat by remember { mutableStateOf(false) }
    var menuCuenta by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val esIngreso = tipo == "Ingreso"
    val colorTipo = if (esIngreso) verde else morado

    val categorias = if (esIngreso) {
        listOf(
            "Salario", "Beca", "Apoyo familiar",
            "Venta", "Reembolso", "Otros"
        )
    } else {
        (
                listOf(
                    "Comida", "Transporte", "Compras",
                    "Educación", "Vivienda", "Salud",
                    "Entretenimiento", "Otros"
                ) + presupuestoViewModel.categorias.map { it.nombre }
                ).distinct()
    }

    Scaffold(
        containerColor = fondo,
        topBar = {
            TopBar("Nueva transacción", onVolver)
        }
    ) { pad ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {

            Text(
                "Registra tu movimiento",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = tinta
            )

            Text(
                "Organiza tus ingresos y gastos fácilmente.",
                fontSize = 13.sp,
                color = gris
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {

                    Text(
                        "Tipo de transacción",
                        fontWeight = FontWeight.SemiBold,
                        color = tinta
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFF0EBFA),
                                RoundedCornerShape(15.dp)
                            )
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Gasto", "Ingreso").forEach { opcion ->
                            val seleccionado = tipo == opcion

                            Surface(
                                onClick = {
                                    tipo = opcion
                                    categoria = ""
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = if (seleccionado) {
                                    if (opcion == "Ingreso") verde else morado
                                } else {
                                    Color.Transparent
                                }
                            ) {
                                Text(
                                    text = opcion,
                                    modifier = Modifier.padding(13.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = if (seleccionado) Color.White else gris,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        "Monto",
                        fontWeight = FontWeight.SemiBold,
                        color = tinta
                    )

                    OutlinedTextField(
                        value = monto,
                        onValueChange = {
                            monto = it.filter { c ->
                                c.isDigit() || c == '.' || c == ','
                            }.take(14)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = {
                            Text("$", color = colorTipo, fontSize = 23.sp)
                        },
                        placeholder = {
                            Text("0.00", fontSize = 23.sp)
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            color = tinta
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(15.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = morado
                        )
                    )

                    Text(
                        "Categoría",
                        fontWeight = FontWeight.SemiBold,
                        color = tinta
                    )

                    Box {
                        OutlinedButton(
                            onClick = { menuCat = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                Icons.Filled.Category,
                                contentDescription = null,
                                tint = morado
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                categoria.ifBlank { "Seleccionar categoría" },
                                modifier = Modifier.weight(1f),
                                color = if (categoria.isBlank()) gris else tinta
                            )
                            Icon(Icons.Filled.ArrowDropDown, null)
                        }

                        DropdownMenu(
                            expanded = menuCat,
                            onDismissRequest = { menuCat = false }
                        ) {
                            categorias.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        categoria = opcion
                                        menuCat = false
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        "Cuenta",
                        fontWeight = FontWeight.SemiBold,
                        color = tinta
                    )

                    Box {
                        OutlinedButton(
                            onClick = { menuCuenta = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                tint = morado
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                vm.cuentas.firstOrNull {
                                    it.id == cuentaId
                                }?.nombre ?: "Seleccionar cuenta",
                                modifier = Modifier.weight(1f),
                                color = tinta
                            )
                            Icon(Icons.Filled.ArrowDropDown, null)
                        }

                        DropdownMenu(
                            expanded = menuCuenta,
                            onDismissRequest = { menuCuenta = false }
                        ) {
                            vm.cuentas.filter { it.activa }.forEach { cuenta ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(cuenta.nombre)
                                            Text(
                                                dinero(cuenta.saldo),
                                                fontSize = 12.sp,
                                                color = gris
                                            )
                                        }
                                    },
                                    onClick = {
                                        cuentaId = cuenta.id
                                        menuCuenta = false
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        "Fecha",
                        fontWeight = FontWeight.SemiBold,
                        color = tinta
                    )

                    OutlinedButton(
                        onClick = {
                            dateDialog(context) { dia = it }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = morado
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            fecha(dia),
                            modifier = Modifier.weight(1f),
                            color = tinta
                        )
                        Icon(Icons.Filled.ArrowDropDown, null)
                    }

                    OutlinedTextField(
                        value = nota,
                        onValueChange = { nota = it.take(160) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nota (opcional)") },
                        placeholder = {
                            Text("¿En qué consistió este movimiento?")
                        },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(15.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = morado
                        )
                    )
                }
            }

            vm.mensaje?.let { Aviso(it) }

            Button(
                onClick = {
                    vm.crear(
                        tipo,
                        monto,
                        categoria,
                        cuentaId,
                        dia,
                        nota,
                        onExito
                    )
                },
                enabled = !vm.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = verde
                )
            ) {
                if (vm.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Filled.CheckCircle, null)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Guardar transacción",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}


@Composable
fun CuentasScreen(
    onInicio: () -> Unit,
    onMovimientos: () -> Unit,
    onCrear: () -> Unit,
    onDetalle: (Cuenta) -> Unit,
    vm: CuentaViewModel = viewModel()
) {
    LaunchedEffect(vm) { vm.observar() }

    val verdeC = Color(0xFF119F50)
    val moradoC = Color(0xFF7045B7)
    val verdeClaroC = Color(0xFFE8F8EF)
    val moradoClaroC = Color(0xFFF2ECFC)
    val textoC = Color(0xFF302A40)
    val grisC = Color(0xFF777381)
    val cuentasActivas = vm.cuentas.filter { it.activa }
    val saldoTotal = cuentasActivas.sumOf { it.saldo }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = { TopBar("FinanQuest SV", onInicio) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Text("Mis cuentas", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = textoC)
            Text("Administra tu dinero con FinanQuest", fontSize = 13.sp, color = grisC)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = verdeC)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(Color(0xFF0D9948), Color(0xFF18BB65))
                        )).padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("SALDO TOTAL DISPONIBLE", fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(7.dp))
                            Text(dinero(saldoTotal), fontSize = 30.sp,
                                fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(Modifier.height(5.dp))
                            Text("${cuentasActivas.size} cuentas activas",
                                fontSize = 12.sp, color = Color.White)
                        }
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(
                                com.finanquest.sv.R.drawable.img
                            ),
                            contentDescription = "Mascota FinanQuest",
                            modifier = Modifier.size(96.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.VerifiedUser, null, tint = Color.White,
                            modifier = Modifier.size(19.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Tus finanzas, organizadas en un solo lugar",
                            fontSize = 12.sp, color = Color.White)
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Tus cuentas", Modifier.weight(1f), fontSize = 19.sp,
                    fontWeight = FontWeight.Bold, color = textoC)
                Text("${vm.cuentas.size} registradas", color = verdeC, fontSize = 12.sp)
            }
            vm.mensaje?.let { Aviso(it) }

            when {
                vm.cargando && vm.cuentas.isEmpty() -> {
                    Box(Modifier.fillMaxWidth().height(130.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = verdeC)
                    }
                }
                vm.cuentas.isEmpty() -> {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.AccountBalanceWallet, null, tint = moradoC,
                                modifier = Modifier.size(45.dp))
                            Spacer(Modifier.height(10.dp))
                            Text("Aún no tienes cuentas", fontWeight = FontWeight.Bold, color = textoC)
                            Text("Crea tu primera cuenta para organizar tu dinero.",
                                fontSize = 12.sp, color = grisC)
                        }
                    }
                }
                else -> vm.cuentas.forEachIndexed { index, cuenta ->
                    val colorCuenta = if (index % 2 == 0) verdeC else moradoC
                    val fondoIcono = if (index % 2 == 0) verdeClaroC else moradoClaroC
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onDetalle(cuenta) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(15.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = fondoIcono, shape = RoundedCornerShape(15.dp)) {
                                Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (cuenta.tipo.contains("Ahorro", ignoreCase = true))
                                            Icons.Filled.Savings else Icons.Filled.AccountBalanceWallet,
                                        null, tint = colorCuenta, modifier = Modifier.size(27.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(cuenta.nombre, fontWeight = FontWeight.Bold,
                                    color = textoC, fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("${cuenta.tipo} · ${if (cuenta.activa) "Activa" else "Inactiva"}",
                                    fontSize = 11.sp, color = grisC)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(dinero(cuenta.saldo), fontWeight = FontWeight.Bold,
                                    color = textoC, fontSize = 14.sp)
                                Spacer(Modifier.height(5.dp))
                                Text("Ver detalles", color = verdeC, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = moradoClaroC)) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lightbulb, null, tint = moradoC,
                        modifier = Modifier.size(26.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("¡Cada cuenta te acerca a tus metas financieras!",
                        color = textoC, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }

            Button(
                onClick = onCrear,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = verdeC),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.AddCircle, null)
                Spacer(Modifier.width(9.dp))
                Text("Nueva cuenta", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(85.dp))
        }
    }
}

@Composable
fun FormularioCuentaScreen(
    cuenta: Cuenta?,
    onVolver: () -> Unit,
    onGuardado: () -> Unit,
    vm: CuentaViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        vm.observar()
        vm.limpiarMensaje()
    }

    var nombre by remember(cuenta?.id) { mutableStateOf(cuenta?.nombre.orEmpty()) }
    var tipo by remember(cuenta?.id) { mutableStateOf(cuenta?.tipo ?: "Ahorro") }
    var saldo by remember(cuenta?.id) { mutableStateOf(if (cuenta == null) "" else cuenta.saldo.toString()) }
    var descripcion by remember(cuenta?.id) { mutableStateOf(cuenta?.descripcion.orEmpty()) }
    var confirmar by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }

    val verdePrincipal = Color(0xFF14A247)
    val moradoFuerte = Color(0xFF6D28D9)
    val lilaSuave = Color(0xFFF0E9FF)
    val colorTexto = Color(0xFF252334)
    val campos = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = verdePrincipal,
        focusedLabelColor = verdePrincipal,
        cursorColor = verdePrincipal,
        unfocusedBorderColor = Color(0xFFDCD9E5)
    )

    Scaffold(
        containerColor = Color(0xFFF8F9FB),
        topBar = {
            TopBar(if (cuenta == null) "Nueva cuenta" else "Editar cuenta") {
                confirmar = true
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = lilaSuave)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (cuenta == null) "¡Organiza tu dinero!" else "¡Tu cuenta, a tu manera!",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF4C1D95)
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(
                            if (cuenta == null) "Crea una cuenta y empieza a cumplir tus metas."
                            else "Actualiza los detalles de tu cuenta fácilmente.",
                            fontSize = 13.sp,
                            color = Color(0xFF706184)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("🐱💜", fontSize = 30.sp)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Detalles de la cuenta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorTexto
                    )
                    Text(
                        "Completa la información para administrar tus finanzas.",
                        fontSize = 12.sp,
                        color = gris
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it.take(50) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre de cuenta") },
                        placeholder = { Text("Ej. Mis ahorros") },
                        leadingIcon = {
                            Icon(Icons.Filled.AccountBalanceWallet, null, tint = moradoFuerte)
                        },
                        shape = RoundedCornerShape(15.dp),
                        singleLine = true,
                        colors = campos
                    )

                    Text("Tipo de cuenta", fontWeight = FontWeight.SemiBold, color = colorTexto)
                    Box {
                        OutlinedButton(
                            onClick = { menu = true },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                            shape = RoundedCornerShape(15.dp),
                            border = BorderStroke(1.dp, Color(0xFFDCD9E5))
                        ) {
                            Icon(Icons.Filled.AccountBalance, null, tint = moradoFuerte)
                            Spacer(Modifier.width(12.dp))
                            Text(tipo, modifier = Modifier.weight(1f), color = colorTexto)
                            Icon(Icons.Filled.ArrowDropDown, null, tint = moradoFuerte)
                        }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            listOf("Ahorro", "Efectivo", "Cuenta corriente", "Tarjeta de crédito", "Otro")
                                .forEach { opcion ->
                                    DropdownMenuItem(
                                        text = { Text(opcion) },
                                        onClick = { tipo = opcion; menu = false }
                                    )
                                }
                        }
                    }

                    if (cuenta == null) {
                        OutlinedTextField(
                            value = saldo,
                            onValueChange = { nuevo ->
                                saldo = nuevo.filter { it.isDigit() || it == '.' || it == ',' }.take(14)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Saldo inicial") },
                            placeholder = { Text("0.00") },
                            leadingIcon = { Text("$", color = verdePrincipal, fontWeight = FontWeight.Bold) },
                            supportingText = { Text("Puedes comenzar con 0 si aún no tienes saldo.") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(15.dp),
                            colors = campos
                        )
                    } else {
                        Surface(
                            color = Color(0xFFEAF8EE),
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("SALDO ACTUAL", fontSize = 11.sp, color = Color(0xFF397A4E))
                                Text(
                                    dinero(cuenta.saldo),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 25.sp,
                                    color = verdePrincipal
                                )
                                Text("El saldo se modifica desde los movimientos.", fontSize = 11.sp, color = gris)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it.take(140) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Descripción (opcional)") },
                        placeholder = { Text("¿Para qué utilizarás esta cuenta?") },
                        leadingIcon = { Icon(Icons.Filled.Edit, null, tint = moradoFuerte) },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(15.dp),
                        colors = campos
                    )
                }
            }

            vm.mensaje?.let { Aviso(it) }

            Button(
                onClick = {
                    if (cuenta == null) {
                        vm.crear(
                            nombre,
                            tipo,
                            saldo.trim().replace(",", ".").toDoubleOrNull() ?: Double.NaN,
                            descripcion,
                            onGuardado
                        )
                    } else {
                        vm.actualizar(
                            cuenta.copy(nombre = nombre, tipo = tipo, descripcion = descripcion),
                            onGuardado
                        )
                    }
                },
                enabled = !vm.cargando,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = verdePrincipal)
            ) {
                if (vm.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Filled.CheckCircle, null)
                    Spacer(Modifier.width(9.dp))
                    Text(
                        if (cuenta == null) "Crear cuenta" else "Guardar cambios",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedButton(
                onClick = { confirmar = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFD9D0E9))
            ) {
                Text("Cancelar", color = moradoFuerte, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(10.dp))
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("¿Deseas cancelar?") },
            text = { Text("Los cambios que no hayas guardado se perderán.") },
            confirmButton = { TextButton(onClick = onVolver) { Text("Sí, salir") } },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Continuar") } }
        )
    }
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



