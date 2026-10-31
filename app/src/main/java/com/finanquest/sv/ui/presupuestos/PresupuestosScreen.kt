
package com.finanquest.sv.ui.presupuestos

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.R
import com.finanquest.sv.viewmodel.PresupuestoViewModel
import java.text.NumberFormat
import java.util.Locale

// =============================================
// COLORES DE FINANQUEST
// =============================================

private val verde = Color(0xFF119F50)
private val verdeClaro = Color(0xFFE8F8EF)
private val morado = Color(0xFF7045B7)
private val moradoClaro = Color(0xFFF2ECFC)
private val fondo: Color @Composable get() = MaterialTheme.colorScheme.background
private val texto: Color @Composable get() = MaterialTheme.colorScheme.onSurface
private val gris: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val rojo = Color(0xFFE45161)

private val categoriasSugeridas = listOf(
    "Alimentación",
    "Salud",
    "Transporte",
    "Educación",
    "Hogar",
    "Entretenimiento",
    "Ahorro",
    "Servicios",
    "Belleza",
    "Salidas"
)

private fun dinero(valor: Double): String =
    NumberFormat.getCurrencyInstance(Locale.US).format(valor)

// =============================================
// COMPONENTES COMPARTIDOS
// =============================================

@Composable
private fun Encabezado(
    titulo: String,
    subtitulo: String,
    onVolver: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVolver) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = texto
            )
        }

        Column {
            Text(
                text = titulo,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = texto
            )

            Text(
                text = subtitulo,
                fontSize = 12.sp,
                color = gris
            )
        }
    }
}

@Composable
private fun MascotaConsejo(
    titulo: String,
    mensaje: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = moradoClaro
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.img),
                contentDescription = "Mascota FinanQuest",
                modifier = Modifier.size(94.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo,
                    color = morado,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = mensaje,
                    color = texto,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun IconoCategoria(nombre: String) {
    val n = nombre.lowercase(Locale.ROOT)

    val icono = when {
        "alimenta" in n || "comida" in n ->
            Icons.Default.Restaurant

        "salud" in n || "medicina" in n ->
            Icons.Default.Favorite

        "transporte" in n ->
            Icons.Default.DirectionsCar

        "educa" in n || "estudio" in n ->
            Icons.Default.School

        "hogar" in n || "casa" in n ->
            Icons.Default.Home

        "belleza" in n || "compra" in n ->
            Icons.Default.ShoppingBag

        "salida" in n || "entretenimiento" in n ->
            Icons.Default.Celebration

        "ahorro" in n ->
            Icons.Default.Savings

        else -> Icons.Default.Category
    }

    val esVerde = listOf(
        "salud",
        "alimenta",
        "hogar",
        "ahorro"
    ).any { it in n }

    Surface(
        color = if (esVerde) verdeClaro else moradoClaro,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = if (esVerde) verde else morado,
                modifier = Modifier.size(27.dp)
            )
        }
    }
}

@Composable
private fun ResumenPequeno(
    titulo: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = color
            )

            Text(
                text = titulo,
                color = gris,
                fontSize = 11.sp
            )

            Text(
                text = valor,
                color = texto,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// =============================================
// 1. PANTALLA PRINCIPAL DE PRESUPUESTOS
// =============================================

@Composable
fun PresupuestosScreen(
    onVolver: () -> Unit = {},
    onCrear: () -> Unit = {},
    onCategorias: () -> Unit = {},
    onDetalle: (String) -> Unit = {},
    onReportes: () -> Unit = {},
    vm: PresupuestoViewModel = viewModel()
) {
    LaunchedEffect(vm) {
        vm.observar()
    }

    val activos = vm.presupuestos.filter { it.activo }

    val total = activos.sumOf { it.limite }

    val gastado = activos.sumOf {
        vm.gasto(it.categoria, it.inicioPeriodo)
    }

    val disponible = (total - gastado).coerceAtLeast(0.0)

    val progreso = if (total > 0) {
        (gastado / total).toFloat()
    } else {
        0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {

        Encabezado(
            titulo = "Mis presupuestos",
            subtitulo = "Controla tus gastos con FinanQuest",
            onVolver = onVolver
        )

        // RESUMEN PRINCIPAL VERDE
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "TOTAL PRESUPUESTADO",
                            color = Color.White,
                            fontSize = 12.sp
                        )

                        Text(
                            dinero(total),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.3f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "GASTADO",
                            color = Color.White,
                            fontSize = 11.sp
                        )

                        Text(
                            dinero(gastado),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            "DISPONIBLE",
                            color = Color.White,
                            fontSize = 11.sp
                        )

                        Text(
                            dinero(disponible),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = {
                        progreso.coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )

                Text(
                    text = if (total > 0) {
                        "${(progreso * 100).toInt()}% utilizado"
                    } else {
                        "Crea tu primer presupuesto"
                    },
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        // MASCOTA UNA SOLA VEZ
        MascotaConsejo(
            titulo = if (total > 0 && gastado >= total) {
                "¡Revisemos tus gastos! 💜"
            } else {
                "¡Vas por buen camino! 💚"
            },
            mensaje = if (total > 0 && gastado >= total) {
                "Has alcanzado tu presupuesto. Revisa tus gastos para mantener el control."
            } else {
                "Tu compañero FinanQuest te ayuda a cuidar cada dólar. ¡Sigue así!"
            }
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResumenPequeno(
                titulo = "Presupuestos",
                valor = "${activos.size} activos",
                color = morado,
                modifier = Modifier.weight(1f)
            )

            ResumenPequeno(
                titulo = "Dinero restante",
                valor = dinero(disponible),
                color = verde,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Tus presupuestos",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = texto
        )

        vm.mensaje?.let { mensaje ->
            Text(
                text = mensaje,
                fontSize = 12.sp,
                color = if (
                    mensaje.contains("correctamente", true)
                ) {
                    verde
                } else {
                    rojo
                }
            )
        }

        if (activos.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Savings,
                        contentDescription = null,
                        tint = verde,
                        modifier = Modifier.size(44.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Todavía no tienes presupuestos",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Crea uno para controlar tus gastos.",
                        color = gris
                    )
                }
            }
        }

        activos.forEach { presupuesto ->

            val usado = vm.gasto(
                presupuesto.categoria,
                presupuesto.inicioPeriodo
            )

            val avance = if (presupuesto.limite > 0) {
                (usado / presupuesto.limite).toFloat()
            } else {
                0f
            }

            val alerta = avance >=
                    presupuesto.alertaUmbral.toFloat()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDetalle(presupuesto.categoria)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconoCategoria(
                            presupuesto.categoria
                        )

                        Spacer(Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                presupuesto.categoria,
                                fontWeight = FontWeight.Bold,
                                color = texto
                            )

                            Text(
                                "${presupuesto.periodo} · Límite ${dinero(presupuesto.limite)}",
                                color = gris,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            "${(avance * 100).toInt()}%",
                            color = if (alerta) rojo else verde,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = {
                            avance.coerceIn(0f, 1f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (alerta) rojo else verde,
                        trackColor = moradoClaro
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Gastado ${dinero(usado)}",
                            color = gris,
                            fontSize = 12.sp
                        )

                        Text(
                            "Disponible ${
                                dinero(
                                    (presupuesto.limite - usado)
                                        .coerceAtLeast(0.0)
                                )
                            }",
                            color = verde,
                            fontSize = 12.sp
                        )
                    }

                    if (alerta) {
                        Text(
                            "⚠ Has alcanzado el nivel de alerta",
                            color = rojo,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Button(
            onClick = onCrear,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = verde
            ),
            shape = RoundedCornerShape(15.dp)
        ) {
            Icon(
                Icons.Default.AddCircle,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text(
                "Crear presupuesto",
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedButton(
            onClick = onCategorias,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(15.dp)
        ) {
            Icon(
                Icons.Default.Category,
                contentDescription = null,
                tint = morado
            )

            Spacer(Modifier.width(8.dp))

            Text(
                "Mis categorías",
                color = morado
            )
        }

        TextButton(
            onClick = onReportes,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        ) {
            Text(
                "Ver reportes",
                color = morado
            )
        }

        Spacer(Modifier.height(20.dp))
    }
}

// =============================================
// 2. CREAR PRESUPUESTO
// =============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPresupuestoScreen(
    onVolver: () -> Unit = {},
    onGuardar: (String, String, String) -> Unit =
        { _, _, _ -> },
    vm: PresupuestoViewModel = viewModel()
) {
    LaunchedEffect(vm) {
        vm.observar()
    }

    var categoria by remember {
        mutableStateOf("")
    }

    var limite by remember {
        mutableStateOf("")
    }

    var periodo by remember {
        mutableStateOf("Mensual")
    }

    var expandido by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Encabezado(
            titulo = "Nuevo presupuesto",
            subtitulo = "Planifica tus gastos",
            onVolver = onVolver
        )

        MascotaConsejo(
            titulo = "¡Planifiquemos juntos! 💜",
            mensaje = "Elige una categoría y establece cuánto dinero deseas gastar."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ENCABEZADO DEL FORMULARIO
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = verdeClaro,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = verde,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            "Configura tu presupuesto",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = texto
                        )

                        Text(
                            "Dale un objetivo a tu dinero",
                            color = gris,
                            fontSize = 12.sp
                        )
                    }
                }

                // SELECCIONAR CATEGORÍA
                ExposedDropdownMenuBox(
                    expanded = expandido,
                    onExpandedChange = {
                        expandido = it
                    }
                ) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Categoría")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                tint = morado
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expandido
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = verde,
                            focusedLabelColor = verde
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandido,
                        onDismissRequest = {
                            expandido = false
                        }
                    ) {
                        vm.categorias.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Text(item.nombre)
                                },
                                onClick = {
                                    categoria = item.nombre
                                    expandido = false
                                }
                            )
                        }
                    }
                }

                if (vm.categorias.isEmpty()) {
                    Text(
                        "Primero crea una categoría.",
                        color = gris,
                        fontSize = 12.sp
                    )
                }

                // LÍMITE DE GASTO
                OutlinedTextField(
                    value = limite,
                    onValueChange = {
                        limite = it.filter { c ->
                            c.isDigit() ||
                                    c == '.' ||
                                    c == ','
                        }
                    },
                    label = {
                        Text("Límite de gasto")
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = verde
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = verde,
                        focusedLabelColor = verde,
                        cursorColor = verde
                    )
                )

                // PERÍODO
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = morado
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        "Período",
                        fontWeight = FontWeight.SemiBold,
                        color = texto
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Semanal",
                        "Mensual"
                    ).forEach { opcion ->
                        FilterChip(
                            selected = periodo == opcion,
                            onClick = {
                                periodo = opcion
                            },
                            label = {
                                Text(opcion)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = moradoClaro,
                                selectedLabelColor = morado
                            )
                        )
                    }
                }

                // INFORMACIÓN DE ALERTAS
                Surface(
                    color = verdeClaro,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = verde
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            "FinanQuest mostrará un aviso cuando alcances el umbral configurado para tu presupuesto.",
                            color = texto,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                vm.mensaje?.let {
                    Text(
                        it,
                        color = rojo,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = {
                        onGuardar(
                            categoria,
                            limite,
                            periodo
                        )
                    },
                    enabled = !vm.cargando &&
                            categoria.isNotBlank() &&
                            (
                                    limite.replace(',', '.')
                                        .toDoubleOrNull() ?: 0.0
                                    ) > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = verde
                    )
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        if (vm.cargando) {
                            "Guardando..."
                        } else {
                            "Guardar presupuesto"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

// =============================================
// 3. CATEGORÍAS
// =============================================

@Composable
fun CategoriasAlertasScreen(
    onVolver: () -> Unit = {},
    onGuardarCategoria: (String, String, Double) -> Unit =
        { _, _, _ -> },
    vm: PresupuestoViewModel = viewModel()
) {
    LaunchedEffect(vm) {
        vm.observar()
    }

    var creando by remember {
        mutableStateOf(false)
    }

    var nombre by remember {
        mutableStateOf("")
    }

    var esperando by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(vm.mensaje, vm.cargando) {
        if (
            esperando &&
            !vm.cargando &&
            vm.mensaje.orEmpty().contains(
                "guardad",
                ignoreCase = true
            )
        ) {
            creando = false
            esperando = false
            nombre = ""
        }
    }

    val guardadas = vm.categorias.map {
        it.nombre.trim().lowercase(Locale.ROOT)
    }.toSet()

    val sugeridas = categoriasSugeridas.filter {
        it.lowercase(Locale.ROOT) !in guardadas
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {

        Encabezado(
            titulo = "Mis categorías",
            subtitulo = "Organiza tus gastos a tu manera",
            onVolver = onVolver
        )

        // ENCABEZADO VERDE CON MASCOTA
        // LA MASCOTA SOLO APARECE AQUÍ
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = verde
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF109D4E),
                                Color(0xFF17B766)
                            )
                        )
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "¡Tu dinero organizado! ✨",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Crea categorías y descubre en qué gastas.",
                        color = Color.White,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "${vm.categorias.size} categorías guardadas",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Image(
                    painter = painterResource(R.drawable.img),
                    contentDescription = "Mascota FinanQuest",
                    modifier = Modifier.size(105.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // NO HAY SEGUNDA TARJETA DE MASCOTA

        vm.mensaje?.let { mensaje ->
            Text(
                text = mensaje,
                color = if (
                    mensaje.contains("guardad", true)
                ) {
                    verde
                } else {
                    rojo
                },
                fontSize = 12.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Tus categorías",
                color = texto,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Text(
                "${vm.categorias.size} guardadas",
                color = verde,
                fontSize = 12.sp
            )
        }

        // CATEGORÍAS GUARDADAS
        vm.categorias.forEachIndexed { index, categoria ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconoCategoria(categoria.nombre)

                    Spacer(Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            categoria.nombre.replaceFirstChar {
                                it.uppercase()
                            },
                            fontWeight = FontWeight.Bold,
                            color = texto
                        )

                        Text(
                            "Lista para registrar gastos",
                            fontSize = 12.sp,
                            color = gris
                        )
                    }

                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Guardada",
                        tint = if (index % 2 == 0) {
                            verde
                        } else {
                            morado
                        }
                    )
                }
            }
        }

        // CATEGORÍAS SUGERIDAS
        if (sugeridas.isNotEmpty()) {
            Text(
                "Explora categorías",
                color = texto,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Agrega las que necesites para organizar tus gastos.",
                color = gris,
                fontSize = 12.sp
            )

            sugeridas.forEachIndexed { index, sugerencia ->
                val color = if (index % 2 == 0) {
                    verde
                } else {
                    morado
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index % 2 == 0) {
                            verdeClaro
                        } else {
                            moradoClaro
                        }
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconoCategoria(sugerencia)

                        Spacer(Modifier.width(10.dp))

                        Text(
                            sugerencia,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold,
                            color = texto
                        )

                        TextButton(
                            onClick = {
                                vm.limpiarMensaje()

                                onGuardarCategoria(
                                    sugerencia,
                                    "1",
                                    0.8
                                )
                            },
                            enabled = !vm.cargando
                        ) {
                            Text(
                                "Agregar",
                                color = color
                            )
                        }
                    }
                }
            }
        }

        // FORMULARIO NUEVA CATEGORÍA
        if (creando) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 3.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = verdeClaro,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                tint = verde,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                "Nueva categoría",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = texto
                            )

                            Text(
                                "Organiza tus gastos a tu manera",
                                fontSize = 12.sp,
                                color = gris
                            )
                        }
                    }

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it.take(40)
                        },
                        label = {
                            Text("Nombre de la categoría")
                        },
                        placeholder = {
                            Text("Ejemplo: Mascotas")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = morado
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = verde,
                            focusedLabelColor = verde,
                            cursorColor = verde
                        )
                    )

                    Button(
                        onClick = {
                            esperando = true

                            onGuardarCategoria(
                                nombre.trim(),
                                "1",
                                0.8
                            )
                        },
                        enabled = nombre.isNotBlank() &&
                                !vm.cargando &&
                                !esperando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = verde
                        )
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "Guardar categoría",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            creando = false
                            esperando = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text(
                            "Cancelar",
                            color = morado
                        )
                    }
                }
            }
        } else {
            Button(
                onClick = {
                    creando = true
                    esperando = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = morado
                ),
                shape = RoundedCornerShape(15.dp)
            ) {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = null
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    "Nueva categoría",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}
