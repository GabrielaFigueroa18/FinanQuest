
package com.finanquest.sv.ui.finanzas

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.R
import com.finanquest.sv.data.model.Cuenta
import com.finanquest.sv.data.model.Movimiento
import com.finanquest.sv.viewmodel.CuentaViewModel
import com.finanquest.sv.viewmodel.MovimientoViewModel
import com.finanquest.sv.viewmodel.PresupuestoViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

// =======================================================
// COLORES Y FUNCIONES GENERALES
// =======================================================

private val fondo = Color(0xFFF7F8FA)
private val verde = Color(0xFF16A34A)
private val morado = Color(0xFF7C3AED)
private val tinta = Color(0xFF20232A)
private val gris = Color(0xFF7B808A)
private val verdeClaro = Color(0xFFE8F8EF)
private val moradoClaro = Color(0xFFF2ECFC)

private fun dinero(valor: Double): String =
    NumberFormat.getCurrencyInstance(
        Locale("es", "SV")
    ).format(valor)

private fun fecha(valor: Long): String =
    SimpleDateFormat(
        "dd/MM/yyyy",
        Locale("es", "SV")
    ).format(Date(valor))

@Composable
private fun Mascota(tamano: Int = 70) {
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(
            R.drawable.img
        ),
        contentDescription = "Mascota FinanQuest",
        modifier = Modifier.size(tamano.dp),
        contentScale = androidx.compose.ui.layout.ContentScale.Fit
    )
}

// =======================================================
// PANTALLA 1: HISTORIAL DE MOVIMIENTOS
// =======================================================

@Composable
fun MovimientosScreen(
    onInicio: () -> Unit,
    onCuentas: () -> Unit,
    onNuevo: () -> Unit,
    vm: MovimientoViewModel = viewModel()
) {
    LaunchedEffect(vm) {
        vm.observar()
    }

    var busqueda by remember {
        mutableStateOf("")
    }

    var filtro by remember {
        mutableStateOf("Todos")
    }

    val nombres = vm.cuentas.associate {
        it.id to it.nombre
    }

    val lista = vm.movimientos.filter { movimiento ->
        val coincideTipo =
            filtro == "Todos" ||
                    movimiento.tipo.equals(
                        filtro,
                        ignoreCase = true
                    )

        val coincideBusqueda =
            movimiento.categoria.contains(
                busqueda,
                ignoreCase = true
            ) ||
                    movimiento.nota.contains(
                        busqueda,
                        ignoreCase = true
                    ) ||
                    (
                            nombres[movimiento.cuentaId]
                                ?.contains(busqueda, true) == true
                            )

        coincideTipo && coincideBusqueda
    }.sortedByDescending {
        it.fecha
    }

    val ingresos = lista
        .filter {
            it.tipo.equals("Ingreso", true)
        }
        .sumOf { it.monto }

    val gastos = lista
        .filter {
            it.tipo.equals("Gasto", true)
        }
        .sumOf { it.monto }

    Scaffold(
        containerColor = fondo,
        topBar = {
            TopBar("FinanQuest SV", onInicio)
        }
    ) { padding ->

        // UNA SOLA LAZYCOLUMN:
        // TODO EL CONTENIDO SE DESPLAZA.

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 14.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ENCABEZADO

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Mis movimientos",
                            fontSize = 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tinta
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            "Controla cada ingreso y gasto",
                            fontSize = 12.sp,
                            color = gris
                        )
                    }

                    Mascota(70)
                }
            }

            // TARJETA DE BALANCE

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = verde
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF0B9647),
                                        Color(0xFF19BB67)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Text(
                            "BALANCE DE LA SELECCIÓN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(
                                alpha = 0.85f
                            )
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            dinero(ingresos - gastos),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(Modifier.height(14.dp))

                        HorizontalDivider(
                            color = Color.White.copy(
                                alpha = 0.3f
                            )
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "↑ INGRESOS",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    dinero(ingresos),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "↓ GASTOS",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    dinero(gastos),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // MENSAJES DE FIREBASE

            vm.mensaje?.let { mensaje ->
                item {
                    Aviso(mensaje)
                }
            }

            // BUSCADOR

            item {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = {
                        busqueda = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Buscar categoría, nota o cuenta",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = morado
                        )
                    },
                    trailingIcon = {
                        if (busqueda.isNotEmpty()) {
                            IconButton(
                                onClick = { busqueda = "" }
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Limpiar"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = morado,
                        unfocusedBorderColor =
                            Color(0xFFDCD8E8)
                    )
                )
            }

            // FILTROS

            item {
                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Todos",
                        "Ingreso",
                        "Gasto"
                    ).forEach { opcion ->

                        FilterChip(
                            selected = filtro == opcion,
                            onClick = {
                                filtro = opcion
                            },
                            label = {
                                Text(
                                    when (opcion) {
                                        "Ingreso" -> "Ingresos"
                                        "Gasto" -> "Gastos"
                                        else -> "Todos"
                                    }
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor =
                                        moradoClaro,
                                    selectedLabelColor = morado
                                )
                        )
                    }
                }
            }

            // ENCABEZADO DE TRANSACCIONES

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Transacciones",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = tinta
                    )

                    Text(
                        "${lista.size} registros",
                        fontSize = 12.sp,
                        color = morado
                    )
                }
            }

            // LISTA COMPLETA DE TRANSACCIONES

            when {
                vm.cargando && vm.movimientos.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = verde
                            )
                        }
                    }
                }

                lista.isEmpty() -> {
                    item {
                        Vacio(
                            "No hay movimientos",
                            "Prueba otra búsqueda o registra una transacción."
                        )
                    }
                }

                else -> {
                    items(
                        items = lista,
                        key = { it.id }
                    ) { movimiento ->
                        MovimientoFila(
                            m = movimiento,
                            cuenta = nombres[
                                movimiento.cuentaId
                            ] ?: "Cuenta"
                        )
                    }
                }
            }

            // BOTÓN AL FINAL, SIN TAPAR MOVIMIENTOS

            item {
                Button(
                    onClick = onNuevo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(17.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = verde
                    )
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        "Nuevo movimiento",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// =======================================================
// PANTALLA 2: NUEVA TRANSACCIÓN
// =======================================================

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

    var tipo by remember {
        mutableStateOf("Gasto")
    }

    var monto by remember {
        mutableStateOf("")
    }

    var categoria by remember {
        mutableStateOf("")
    }

    var cuentaId by remember {
        mutableStateOf("")
    }

    var nota by remember {
        mutableStateOf("")
    }

    var dia by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    var menuCat by remember {
        mutableStateOf(false)
    }

    var menuCuenta by remember {
        mutableStateOf(false)
    }

    val context =
        androidx.compose.ui.platform.LocalContext.current

    val esIngreso = tipo == "Ingreso"
    val colorTipo = if (esIngreso) verde else morado

    val categorias = if (esIngreso) {
        listOf(
            "Salario",
            "Beca",
            "Apoyo familiar",
            "Venta",
            "Reembolso",
            "Otros"
        )
    } else {
        (
                listOf(
                    "Comida",
                    "Transporte",
                    "Compras",
                    "Educación",
                    "Vivienda",
                    "Salud",
                    "Entretenimiento",
                    "Otros"
                ) + presupuestoViewModel.categorias.map {
                    it.nombre
                }
                ).distinct()
    }

    Scaffold(
        containerColor = fondo,
        topBar = {
            TopBar("Nueva transacción", onVolver)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 18.dp,
                    vertical = 14.dp
                ),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
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
                }

                Mascota(65)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(15.dp)
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
                                moradoClaro,
                                RoundedCornerShape(15.dp)
                            )
                            .padding(5.dp),
                        horizontalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Gasto",
                            "Ingreso"
                        ).forEach { opcion ->

                            val seleccionado = tipo == opcion

                            Surface(
                                onClick = {
                                    tipo = opcion
                                    categoria = ""
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = if (seleccionado) {
                                    if (opcion == "Ingreso") {
                                        verde
                                    } else {
                                        morado
                                    }
                                } else {
                                    Color.Transparent
                                }
                            ) {
                                Text(
                                    text = opcion,
                                    modifier = Modifier.padding(13.dp),
                                    textAlign = TextAlign.Center,
                                    color = if (seleccionado) {
                                        Color.White
                                    } else {
                                        gris
                                    },
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
                        onValueChange = { nuevo ->
                            monto = nuevo.filter { caracter ->
                                caracter.isDigit() ||
                                        caracter == '.' ||
                                        caracter == ','
                            }.take(14)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = {
                            Text(
                                "$",
                                color = colorTipo,
                                fontSize = 23.sp
                            )
                        },
                        placeholder = {
                            Text(
                                "0.00",
                                fontSize = 23.sp
                            )
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
                            onClick = {
                                menuCat = true
                            },
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
                                categoria.ifBlank {
                                    "Seleccionar categoría"
                                },
                                modifier = Modifier.weight(1f),
                                color = if (categoria.isBlank()) {
                                    gris
                                } else {
                                    tinta
                                }
                            )

                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = null
                            )
                        }

                        DropdownMenu(
                            expanded = menuCat,
                            onDismissRequest = {
                                menuCat = false
                            }
                        ) {
                            categorias.forEach { opcion ->
                                DropdownMenuItem(
                                    text = {
                                        Text(opcion)
                                    },
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
                            onClick = {
                                menuCuenta = true
                            },
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

                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = null
                            )
                        }

                        DropdownMenu(
                            expanded = menuCuenta,
                            onDismissRequest = {
                                menuCuenta = false
                            }
                        ) {
                            vm.cuentas.filter {
                                it.activa
                            }.forEach { cuenta ->

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
                            dateDialog(context) {
                                dia = it
                            }
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

                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = null
                        )
                    }

                    OutlinedTextField(
                        value = nota,
                        onValueChange = {
                            nota = it.take(160)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Nota (opcional)")
                        },
                        placeholder = {
                            Text(
                                "¿En qué consistió este movimiento?"
                            )
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

            vm.mensaje?.let {
                Aviso(it)
            }

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
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        "Guardar transacción",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(Modifier.height(15.dp))
        }
    }
}

// =======================================================
// PANTALLA 3: MIS CUENTAS
// =======================================================

@Composable
fun CuentasScreen(
    onInicio: () -> Unit,
    onMovimientos: () -> Unit,
    onCrear: () -> Unit,
    onDetalle: (Cuenta) -> Unit,
    vm: CuentaViewModel = viewModel()
) {
    LaunchedEffect(vm) {
        vm.observar()
    }

    val cuentasActivas = vm.cuentas.filter {
        it.activa
    }

    val saldoTotal = cuentasActivas.sumOf {
        it.saldo
    }

    Scaffold(
        containerColor = fondo,
        topBar = {
            TopBar("FinanQuest SV", onInicio)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(6.dp))

            Text(
                "Mis cuentas",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = tinta
            )

            Text(
                "Administra tu dinero con FinanQuest",
                fontSize = 13.sp,
                color = gris
            )

            // SALDO TOTAL

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = verde
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0D9948),
                                    Color(0xFF18BB65)
                                )
                            )
                        )
                        .padding(18.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "SALDO TOTAL DISPONIBLE",
                                fontSize = 11.sp,
                                color = Color.White.copy(
                                    alpha = 0.9f
                                )
                            )

                            Spacer(Modifier.height(7.dp))

                            Text(
                                dinero(saldoTotal),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(Modifier.height(5.dp))

                            Text(
                                "${cuentasActivas.size} cuentas activas",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        Mascota(96)
                    }

                    HorizontalDivider(
                        color = Color.White.copy(
                            alpha = 0.3f
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "Tus finanzas, organizadas en un solo lugar",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // ENCABEZADO

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Tus cuentas",
                    modifier = Modifier.weight(1f),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = tinta
                )

                Text(
                    "${vm.cuentas.size} registradas",
                    color = verde,
                    fontSize = 12.sp
                )
            }

            vm.mensaje?.let {
                Aviso(it)
            }

            // LISTA DE CUENTAS

            when {
                vm.cargando && vm.cuentas.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = verde
                        )
                    }
                }

                vm.cuentas.isEmpty() -> {
                    Vacio(
                        "Aún no tienes cuentas",
                        "Crea tu primera cuenta para organizar tu dinero."
                    )
                }

                else -> {
                    vm.cuentas.forEachIndexed { index, cuenta ->

                        val colorCuenta =
                            if (index % 2 == 0) verde else morado

                        val fondoIcono =
                            if (index % 2 == 0) {
                                verdeClaro
                            } else {
                                moradoClaro
                            }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDetalle(cuenta)
                                },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(15.dp),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = fondoIcono,
                                    shape = RoundedCornerShape(15.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(54.dp),
                                        contentAlignment =
                                            Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector =
                                                if (cuenta.tipo.contains(
                                                        "Ahorro",
                                                        true
                                                    )
                                                ) {
                                                    Icons.Filled.Savings
                                                } else {
                                                    Icons.Filled.AccountBalanceWallet
                                                },
                                            contentDescription = null,
                                            tint = colorCuenta,
                                            modifier = Modifier.size(27.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        cuenta.nombre,
                                        fontWeight = FontWeight.Bold,
                                        color = tinta,
                                        fontSize = 14.sp
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    Text(
                                        "${cuenta.tipo} · ${
                                            if (cuenta.activa) "Activa"
                                            else "Inactiva"
                                        }",
                                        fontSize = 11.sp,
                                        color = gris
                                    )
                                }

                                Column(
                                    horizontalAlignment =
                                        Alignment.End
                                ) {
                                    Text(
                                        dinero(cuenta.saldo),
                                        fontWeight = FontWeight.Bold,
                                        color = tinta,
                                        fontSize = 14.sp
                                    )

                                    Spacer(Modifier.height(5.dp))

                                    Text(
                                        "Ver detalles",
                                        color = verde,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // CONSEJO

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = moradoClaro
                )
            ) {
                Row(
                    modifier = Modifier.padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = morado,
                        modifier = Modifier.size(26.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        "¡Cada cuenta te acerca a tus metas financieras!",
                        color = tinta,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Button(
                onClick = onCrear,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = verde
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Filled.AddCircle,
                    contentDescription = null
                )

                Spacer(Modifier.width(9.dp))

                Text(
                    "Nueva cuenta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            OutlinedButton(
                onClick = onMovimientos,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = morado
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    "Ver movimientos",
                    color = morado
                )
            }

            Spacer(Modifier.height(85.dp))
        }
    }
}

// =======================================================
// PANTALLA 4: CREAR Y EDITAR CUENTA
// =======================================================

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

    var nombre by remember(cuenta?.id) {
        mutableStateOf(cuenta?.nombre.orEmpty())
    }

    var tipo by remember(cuenta?.id) {
        mutableStateOf(cuenta?.tipo ?: "Ahorro")
    }

    var saldo by remember(cuenta?.id) {
        mutableStateOf(
            if (cuenta == null) ""
            else cuenta.saldo.toString()
        )
    }

    var descripcion by remember(cuenta?.id) {
        mutableStateOf(cuenta?.descripcion.orEmpty())
    }

    var confirmar by remember {
        mutableStateOf(false)
    }

    var menu by remember {
        mutableStateOf(false)
    }

    val campos = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = verde,
        focusedLabelColor = verde,
        cursorColor = verde,
        unfocusedBorderColor = Color(0xFFDCD9E5)
    )

    Scaffold(
        containerColor = fondo,
        topBar = {
            TopBar(
                if (cuenta == null) {
                    "Nueva cuenta"
                } else {
                    "Editar cuenta"
                }
            ) {
                confirmar = true
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 18.dp,
                    vertical = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // PRESENTACIÓN

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = moradoClaro
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (cuenta == null) {
                                "¡Organiza tu dinero!"
                            } else {
                                "¡Tu cuenta, a tu manera!"
                            },
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = morado
                        )

                        Spacer(Modifier.height(7.dp))

                        Text(
                            if (cuenta == null) {
                                "Crea una cuenta y empieza a cumplir tus metas."
                            } else {
                                "Actualiza los detalles de tu cuenta fácilmente."
                            },
                            fontSize = 13.sp,
                            color = gris
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Mascota(72)
                }
            }

            // FORMULARIO

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Detalles de la cuenta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = tinta
                    )

                    Text(
                        "Completa la información para administrar tus finanzas.",
                        fontSize = 12.sp,
                        color = gris
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it.take(50)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Nombre de cuenta")
                        },
                        placeholder = {
                            Text("Ej. Mis ahorros")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                tint = morado
                            )
                        },
                        shape = RoundedCornerShape(15.dp),
                        singleLine = true,
                        colors = campos
                    )

                    Text(
                        "Tipo de cuenta",
                        fontWeight = FontWeight.SemiBold,
                        color = tinta
                    )

                    Box {
                        OutlinedButton(
                            onClick = {
                                menu = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp),
                            shape = RoundedCornerShape(15.dp),
                            border = BorderStroke(
                                1.dp,
                                Color(0xFFDCD9E5)
                            )
                        ) {
                            Icon(
                                Icons.Filled.AccountBalance,
                                contentDescription = null,
                                tint = morado
                            )

                            Spacer(Modifier.width(12.dp))

                            Text(
                                tipo,
                                modifier = Modifier.weight(1f),
                                color = tinta
                            )

                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = morado
                            )
                        }

                        DropdownMenu(
                            expanded = menu,
                            onDismissRequest = {
                                menu = false
                            }
                        ) {
                            listOf(
                                "Ahorro",
                                "Efectivo",
                                "Cuenta corriente",
                                "Tarjeta de crédito",
                                "Otro"
                            ).forEach { opcion ->

                                DropdownMenuItem(
                                    text = {
                                        Text(opcion)
                                    },
                                    onClick = {
                                        tipo = opcion
                                        menu = false
                                    }
                                )
                            }
                        }
                    }

                    // SALDO INICIAL O SALDO ACTUAL

                    if (cuenta == null) {
                        OutlinedTextField(
                            value = saldo,
                            onValueChange = { nuevo ->
                                saldo = nuevo.filter {
                                    it.isDigit() ||
                                            it == '.' ||
                                            it == ','
                                }.take(14)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Saldo inicial")
                            },
                            placeholder = {
                                Text("0.00")
                            },
                            leadingIcon = {
                                Text(
                                    "$",
                                    color = verde,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            supportingText = {
                                Text(
                                    "Puedes comenzar con 0 si aún no tienes saldo."
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(15.dp),
                            colors = campos
                        )
                    } else {
                        Surface(
                            color = verdeClaro,
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    "SALDO ACTUAL",
                                    fontSize = 11.sp,
                                    color = verde
                                )

                                Text(
                                    dinero(cuenta.saldo),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 25.sp,
                                    color = verde
                                )

                                Text(
                                    "El saldo se modifica desde los movimientos.",
                                    fontSize = 11.sp,
                                    color = gris
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = {
                            descripcion = it.take(140)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Descripción (opcional)")
                        },
                        placeholder = {
                            Text(
                                "¿Para qué utilizarás esta cuenta?"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null,
                                tint = morado
                            )
                        },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(15.dp),
                        colors = campos
                    )
                }
            }

            vm.mensaje?.let {
                Aviso(it)
            }

            // GUARDAR

            Button(
                onClick = {
                    if (cuenta == null) {
                        vm.crear(
                            nombre,
                            tipo,
                            saldo.trim()
                                .replace(",", ".")
                                .toDoubleOrNull()
                                ?: Double.NaN,
                            descripcion,
                            onGuardado
                        )
                    } else {
                        vm.actualizar(
                            cuenta.copy(
                                nombre = nombre,
                                tipo = tipo,
                                descripcion = descripcion
                            ),
                            onGuardado
                        )
                    }
                },
                enabled = !vm.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = verde
                )
            ) {
                if (vm.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(9.dp))

                    Text(
                        if (cuenta == null) {
                            "Crear cuenta"
                        } else {
                            "Guardar cambios"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    confirmar = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    Color(0xFFD9D0E9)
                )
            ) {
                Text(
                    "Cancelar",
                    color = morado,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(10.dp))
        }
    }

    // CONFIRMACIÓN AL SALIR

    if (confirmar) {
        AlertDialog(
            onDismissRequest = {
                confirmar = false
            },
            title = {
                Text("¿Deseas cancelar?")
            },
            text = {
                Text(
                    "Los cambios que no hayas guardado se perderán."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onVolver
                ) {
                    Text("Sí, salir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmar = false
                    }
                ) {
                    Text("Continuar")
                }
            }
        )
    }
}

// =======================================================
// PANTALLA 5: DETALLE DE CUENTA
// =======================================================

@Composable
fun DetalleCuentaScreen(
    cuenta: Cuenta,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    vm: MovimientoViewModel = viewModel()
) {
    LaunchedEffect(vm) {
        vm.observar()
    }

    val lista = vm.movimientos
        .filter {
            it.cuentaId == cuenta.id
        }
        .sortedByDescending {
            it.fecha
        }

    Scaffold(
        containerColor = fondo,
        topBar = {
            TopBar(cuenta.nombre, onVolver)
        }
    ) { padding ->

        // UNA SOLA LISTA PARA QUE TODO SE DESPLACE

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 16.dp,
                bottom = 100.dp
            ),
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = moradoClaro
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "SALDO ACTUAL",
                                    color = morado,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    dinero(cuenta.saldo),
                                    fontSize = 30.sp,
                                    color = morado,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Mascota(70)
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            "${cuenta.tipo} · ${
                                if (cuenta.activa) "Activa"
                                else "Inactiva"
                            }",
                            color = gris
                        )

                        if (cuenta.descripcion.isNotBlank()) {
                            Spacer(Modifier.height(7.dp))

                            Text(
                                cuenta.descripcion,
                                color = gris,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = onEditar,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null,
                                tint = morado
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                "Editar cuenta",
                                color = morado
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Movimientos de esta cuenta",
                        modifier = Modifier.weight(1f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = tinta
                    )

                    Text(
                        "${lista.size}",
                        color = morado,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (lista.isEmpty()) {
                item {
                    Vacio(
                        "Sin movimientos todavía",
                        "Los movimientos de esta cuenta aparecerán aquí."
                    )
                }
            } else {
                items(
                    items = lista,
                    key = { it.id }
                ) { movimiento ->

                    MovimientoFila(
                        m = movimiento,
                        cuenta = cuenta.nombre
                    )
                }
            }
        }
    }
}

// =======================================================
// COMPONENTE: BARRA SUPERIOR
// =======================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    titulo: String,
    volver: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                titulo,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = tinta
            )
        },
        navigationIcon = {
            IconButton(
                onClick = volver
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = tinta
                )
            }
        },
        actions = {
            Icon(
                Icons.Filled.Pets,
                contentDescription = null,
                tint = verde
            )

            Spacer(Modifier.width(14.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = fondo
        )
    )
}

// =======================================================
// COMPONENTE: TARJETA DE MOVIMIENTO
// =======================================================

@Composable
private fun MovimientoFila(
    m: Movimiento,
    cuenta: String
) {
    val ingreso = m.tipo.equals(
        "Ingreso",
        ignoreCase = true
    )

    val acento = if (ingreso) {
        verde
    } else {
        morado
    }

    val fondoIcono = if (ingreso) {
        verdeClaro
    } else {
        moradoClaro
    }

    val icono = when {
        ingreso -> Icons.Filled.TrendingUp

        m.categoria.contains("comida", true) ||
                m.categoria.contains("aliment", true) ->
            Icons.Filled.Restaurant

        m.categoria.contains("transport", true) ->
            Icons.Filled.DirectionsCar

        m.categoria.contains("salud", true) ->
            Icons.Filled.Favorite

        m.categoria.contains("educ", true) ->
            Icons.Filled.School

        m.categoria.contains("ahorro", true) ->
            Icons.Filled.Savings

        else -> Icons.Filled.ShoppingBag
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(19.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = fondoIcono
            ) {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icono,
                        contentDescription = null,
                        tint = acento,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(11.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    m.categoria,
                    fontWeight = FontWeight.Bold,
                    color = tinta,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(3.dp))

                Text(
                    m.nota.ifBlank { cuenta },
                    color = gris,
                    fontSize = 11.sp,
                    maxLines = 1
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    "${fecha(m.fecha)} · $cuenta",
                    color = gris,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            Spacer(Modifier.width(6.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "${if (ingreso) "+" else "−"}${dinero(m.monto)}",
                    color = acento,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    if (ingreso) "Ingreso" else "Gasto",
                    color = acento,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// =======================================================
// COMPONENTE: ESTADO VACÍO
// =======================================================

@Composable
private fun Vacio(
    titulo: String,
    detalle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.AccountBalanceWallet,
                contentDescription = null,
                tint = morado,
                modifier = Modifier.size(38.dp)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                titulo,
                fontWeight = FontWeight.Bold,
                color = tinta,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(5.dp))

            Text(
                detalle,
                color = gris,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// =======================================================
// COMPONENTE: MENSAJES
// =======================================================

@Composable
private fun Aviso(texto: String) {
    val exito = texto.endsWith(
        "correctamente",
        ignoreCase = true
    )

    val fondoAviso = if (exito) {
        Color(0xFFE7F7EC)
    } else {
        Color(0xFFFFF3E0)
    }

    val textoAviso = if (exito) {
        Color(0xFF176B35)
    } else {
        Color(0xFF9A5B00)
    }

    Surface(
        color = fondoAviso,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (exito) {
                    Icons.Filled.CheckCircle
                } else {
                    Icons.Filled.Info
                },
                contentDescription = null,
                tint = if (exito) verde else textoAviso
            )

            Spacer(Modifier.width(8.dp))

            Text(
                texto,
                color = textoAviso,
                fontSize = 13.sp
            )
        }
    }
}

// =======================================================
// COMPONENTE: SELECCIONAR FECHA
// =======================================================

private fun dateDialog(
    context: android.content.Context,
    seleccion: (Long) -> Unit
) {
    val today = Calendar.getInstance()

    android.app.DatePickerDialog(
        context,
        { _, year, month, day ->

            val selected = Calendar.getInstance().apply {
                set(
                    year,
                    month,
                    day,
                    12,
                    0,
                    0
                )

                set(
                    Calendar.MILLISECOND,
                    0
                )
            }

            seleccion(selected.timeInMillis)
        },
        today.get(Calendar.YEAR),
        today.get(Calendar.MONTH),
        today.get(Calendar.DAY_OF_MONTH)
    ).show()
}
