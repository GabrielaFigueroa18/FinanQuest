
package com.finanquest.sv.ui.reportes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.R
import com.finanquest.sv.viewmodel.MovimientoViewModel
import com.finanquest.sv.utils.FinanzasIntegrante2
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

// =============================================
// COLORES FINANQUEST
// =============================================

private val verdeRep = Color(0xFF119F50)
private val verdeClaroRep = Color(0xFFE8F8EF)
private val moradoRep = Color(0xFF7045B7)
private val moradoClaroRep = Color(0xFFF2ECFC)
private val rojoRep = Color(0xFFE45161)
private val rojoClaroRep = Color(0xFFFDECEF)
private val fondoRep: Color @Composable get() = MaterialTheme.colorScheme.background
private val textoRep: Color @Composable get() = MaterialTheme.colorScheme.onSurface
private val grisRep: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

private val coloresGrafico = listOf(
    Color(0xFF119F50),
    Color(0xFF7045B7),
    Color(0xFF8BD7E5),
    Color(0xFFF3B65F),
    Color(0xFFE45161)
)

private data class ItemGasto(
    val nombre: String,
    val monto: Double,
    val porcentaje: Double
)

private fun monedaReporte(valor: Double): String =
    NumberFormat.getCurrencyInstance(Locale.US).format(valor)

// =============================================
// PANTALLA REPORTES
// =============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    onVolver: () -> Unit = {},
    onMovimientos: () -> Unit = {},
    onInicio: () -> Unit = {},
    vm: MovimientoViewModel = viewModel()
) {
    LaunchedEffect(vm) {
        vm.observar()
    }

    var periodo by remember {
        mutableStateOf("Este mes")
    }

    var menu by remember {
        mutableStateOf(false)
    }

    var filtros by remember {
        mutableStateOf(false)
    }

    val desde = FinanzasIntegrante2.inicioPeriodoReporte(periodo)

    val movimientos = vm.movimientos.filter {
        it.fecha >= desde
    }

    val (ingresos, gastos, balance) =
        FinanzasIntegrante2.totales(movimientos, desde)

    // Agrupar los gastos por categoría.
    val agrupados = movimientos
        .filter {
            it.tipo.equals("Gasto", ignoreCase = true)
        }
        .groupBy {
            it.categoria.ifBlank { "Otros" }
        }
        .map { (nombre, lista) ->
            nombre to lista.sumOf { it.monto }
        }
        .filter { it.second > 0.0 }
        .sortedByDescending { it.second }

    // Mostrar las cuatro categorías mayores y
    // agrupar las demás en "Otros".
    val principales = agrupados.take(4)
    val restantes = agrupados.drop(4).sumOf { it.second }

    val distribucion = buildList {
        addAll(principales)

        if (restantes > 0.0) {
            add("Otros" to restantes)
        }
    }

    val totalDistribucion = distribucion.sumOf { it.second }

    val items = distribucion.map { (nombre, monto) ->
        ItemGasto(
            nombre = nombre,
            monto = monto,
            porcentaje = if (totalDistribucion > 0.0) {
                monto * 100.0 / totalDistribucion
            } else {
                0.0
            }
        )
    }

    Scaffold(
        containerColor = fondoRep,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Mis reportes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp,
                        color = textoRep
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = textoRep
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { menu = true }
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Opciones",
                                tint = textoRep
                            )
                        }

                        DropdownMenu(
                            expanded = menu,
                            onDismissRequest = {
                                menu = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("Ver movimientos")
                                },
                                onClick = {
                                    menu = false
                                    onMovimientos()
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text("Ir al inicio")
                                },
                                onClick = {
                                    menu = false
                                    onInicio()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = fondoRep
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                "Conoce cómo se mueve tu dinero",
                color = grisRep,
                fontSize = 13.sp
            )

            // =====================================
            // FILTRO DE PERÍODO
            // =====================================

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { filtros = true }
                        .padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = moradoClaroRep,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = moradoRep,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "PERÍODO ACTUAL",
                            fontSize = 10.sp,
                            color = grisRep,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            periodo,
                            fontWeight = FontWeight.SemiBold,
                            color = textoRep
                        )
                    }

                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        tint = moradoRep
                    )

                    Spacer(Modifier.width(5.dp))

                    Text(
                        "Filtrar",
                        color = moradoRep,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // =====================================
            // TARJETA PRINCIPAL CON MASCOTA
            // =====================================

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = verdeRep
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0D9948),
                                    Color(0xFF18B965)
                                )
                            )
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                if (balance >= 0.0) {
                                    "¡Buen trabajo! ✨"
                                } else {
                                    "¡Podemos mejorar! 💜"
                                },
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                if (balance >= 0.0) {
                                    "Tus ingresos superan tus gastos en el período seleccionado."
                                } else {
                                    "Tus gastos superan tus ingresos. Revisa en qué puedes ahorrar."
                                },
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }

                        Image(
                            painter = painterResource(
                                R.drawable.img
                            ),
                            contentDescription = "Mascota FinanQuest",
                            modifier = Modifier.size(100.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.3f)
                    )

                    Text(
                        "BALANCE DEL PERÍODO",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        monedaReporte(balance),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (balance >= 0.0) {
                                Icons.Default.TrendingUp
                            } else {
                                Icons.Default.TrendingDown
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )

                        Spacer(Modifier.width(6.dp))

                        Text(
                            if (balance >= 0.0) {
                                "Balance positivo"
                            } else {
                                "Balance negativo"
                            },
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // =====================================
            // TARJETAS DE RESUMEN
            // =====================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TarjetaResumenReporte(
                    titulo = "INGRESOS",
                    valor = ingresos,
                    periodo = periodo,
                    fondo = verdeClaroRep,
                    tinta = verdeRep,
                    icono = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )

                TarjetaResumenReporte(
                    titulo = "GASTOS",
                    valor = gastos,
                    periodo = periodo,
                    fondo = rojoClaroRep,
                    tinta = rojoRep,
                    icono = Icons.Default.TrendingDown,
                    modifier = Modifier.weight(1f)
                )

                TarjetaResumenReporte(
                    titulo = "BALANCE",
                    valor = balance,
                    periodo = periodo,
                    fondo = moradoClaroRep,
                    tinta = moradoRep,
                    icono = Icons.Default.Savings,
                    modifier = Modifier.weight(1f)
                )
            }

            // =====================================
            // DISTRIBUCIÓN DE GASTOS
            // =====================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Distribución de gastos",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textoRep
                )

                TextButton(onClick = onMovimientos) {
                    Text(
                        "VER TODO",
                        color = moradoRep,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            if (vm.cargando && vm.movimientos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = verdeRep
                    )
                }
            } else if (items.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Wallet,
                            contentDescription = null,
                            tint = moradoRep,
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Sin gastos registrados",
                            color = textoRep,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Todavía no hay gastos en este período.",
                            color = grisRep,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(205.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GraficoDonutReporte(items)

                            Column(
                                horizontalAlignment =
                                    Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "TOTAL GASTOS",
                                    fontSize = 10.sp,
                                    color = grisRep
                                )

                                Text(
                                    monedaReporte(totalDistribucion),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textoRep
                                )
                            }
                        }

                        items.forEachIndexed { index, item ->

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onMovimientos()
                                    }
                                    .padding(vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            coloresGrafico[
                                                index % coloresGrafico.size
                                            ],
                                            RoundedCornerShape(50)
                                        )
                                )

                                Spacer(Modifier.width(10.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        item.nombre,
                                        color = textoRep,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        "${item.porcentaje.toInt()}%",
                                        color = grisRep,
                                        fontSize = 11.sp
                                    )
                                }

                                Text(
                                    monedaReporte(item.monto),
                                    fontWeight = FontWeight.Bold,
                                    color = textoRep
                                )
                            }

                            if (index < items.lastIndex) {
                                HorizontalDivider(
                                    color = Color(0xFFEDEDF0)
                                )
                            }
                        }

                        Button(
                            onClick = onMovimientos,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = verdeRep
                            ),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Text(
                                "Ver movimientos",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // =====================================
            // CONSEJO FINANCIERO
            // =====================================

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = moradoClaroRep
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = moradoRep,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            if (balance >= 0.0) {
                                "¡Sigue así!"
                            } else {
                                "¡Organiza tus gastos!"
                            },
                            color = moradoRep,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Text(
                            "Revisa tus gastos por categoría y planifica cuánto destinar a tus metas.",
                            color = textoRep,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    // =========================================
    // DIÁLOGO DE FILTROS
    // =========================================

    if (filtros) {
        AlertDialog(
            onDismissRequest = {
                filtros = false
            },
            title = {
                Text("Selecciona un período")
            },
            text = {
                Column {
                    listOf(
                        "Este mes",
                        "Últimos 3 meses",
                        "Este año"
                    ).forEach { opcion ->
                        TextButton(
                            onClick = {
                                periodo = opcion
                                filtros = false
                            }
                        ) {
                            Text(
                                opcion,
                                color = if (periodo == opcion) {
                                    verdeRep
                                } else {
                                    textoRep
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        filtros = false
                    }
                ) {
                    Text(
                        "Cerrar",
                        color = moradoRep
                    )
                }
            }
        )
    }
}

// =============================================
// COMPONENTE: TARJETA DE RESUMEN
// =============================================

@Composable
private fun TarjetaResumenReporte(
    titulo: String,
    valor: Double,
    periodo: String,
    fondo: Color,
    tinta: Color,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = fondo
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = tinta,
                modifier = Modifier.size(25.dp)
            )

            Text(
                titulo,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = grisRep
            )

            Text(
                monedaReporte(valor),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = tinta,
                maxLines = 1
            )

            Text(
                periodo.uppercase(Locale.ROOT),
                fontSize = 9.sp,
                color = grisRep,
                maxLines = 1
            )
        }
    }
}

// =============================================
// COMPONENTE: GRÁFICO CIRCULAR
// =============================================

@Composable
private fun GraficoDonutReporte(
    items: List<ItemGasto>
) {
    Canvas(
        modifier = Modifier.size(190.dp)
    ) {
        val grosor = size.minDimension * 0.18f
        val margen = grosor / 2f

        var inicio = -90f

        val total = items.sumOf { it.monto }

        if (total <= 0.0) return@Canvas

        items.forEachIndexed { index, item ->

            val barrido = (
                    360.0 * item.monto / total
                    ).toFloat()

            if (barrido > 0f) {
                drawArc(
                    color = coloresGrafico[
                        index % coloresGrafico.size
                    ],
                    startAngle = inicio,
                    sweepAngle = (barrido - 2f)
                        .coerceAtLeast(0f),
                    useCenter = false,
                    topLeft = Offset(margen, margen),
                    size = Size(
                        size.width - grosor,
                        size.height - grosor
                    ),
                    style = Stroke(
                        width = grosor
                    )
                )
            }

            inicio += barrido
        }
    }
}
