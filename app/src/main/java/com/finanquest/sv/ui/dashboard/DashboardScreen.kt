package com.finanquest.sv.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.R
import com.finanquest.sv.data.model.Movimiento
import com.finanquest.sv.ui.components.*
import com.finanquest.sv.utils.FinanzasIntegrante2
import com.finanquest.sv.viewmodel.AuthViewModel
import com.finanquest.sv.viewmodel.CuentaViewModel
import com.finanquest.sv.viewmodel.MetaViewModel
import com.finanquest.sv.viewmodel.MovimientoViewModel
import com.finanquest.sv.viewmodel.PresupuestoViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

private val Fondo: Color
    @Composable get() = MaterialTheme.colorScheme.background
private val Verde: Color
    @Composable get() = MaterialTheme.colorScheme.primary
private val Morado: Color
    @Composable get() = MaterialTheme.colorScheme.secondary
private val Rojo: Color
    @Composable get() = MaterialTheme.colorScheme.error
private val Azul = Color(0xFF5086FF)
private val Naranja = Color(0xFFF59E0B)
private val Texto: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val Gris: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val Borde: Color
    @Composable get() = MaterialTheme.colorScheme.outline

private fun formatoDinero(valor: Double): String = String.format(Locale.US, "$%,.2f", valor)

private fun rachaMovimientos(movimientos: List<Movimiento>): Int =
    FinanzasIntegrante2.racha(movimientos)

@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    onMovimientos: () -> Unit = {},
    onMetas: () -> Unit = {},
    onMascota: () -> Unit = {},
    onNiveles: () -> Unit = {},
    onPerfil: () -> Unit = {},
    onMisiones: () -> Unit = {},
    onPresupuestos: () -> Unit = {},
    onNuevoGasto: () -> Unit = {},
    onNuevaMeta: () -> Unit = {},
    onNuevaCategoria: () -> Unit = {},
    onReportes: () -> Unit = {},
    onCuentas: () -> Unit = {},
    onRecompensas: () -> Unit = {},
    onNotificaciones: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
) {
    QuestVisualTheme {
        LaunchedEffect(Unit) {
            authViewModel.cargarPerfil()
            authViewModel.escucharCambiosPerfil()
        }

        val usuario = authViewModel.usuario
        val cuentaViewModel: CuentaViewModel = viewModel()
        val movimientoViewModel: MovimientoViewModel = viewModel()
        val metasViewModel: MetaViewModel = viewModel()
        val presupuestoViewModel: PresupuestoViewModel = viewModel()
        LaunchedEffect(Unit) {
            cuentaViewModel.observar()
            movimientoViewModel.observar()
            metasViewModel.observar()
            presupuestoViewModel.observar()
        }

        val primerNombre =
            usuario?.nombre?.trim()?.split(" ")?.firstOrNull()?.takeUnless { it.isNullOrBlank() }
                ?: "Estudiante"

        // Escucha los XP y el nivel directamente en Firestore para reflejar
        // las recompensas otorgadas por el backend, sin recargar la pantalla.
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        var progresoActual by remember(uid) { mutableStateOf<Pair<Int, Int>?>(null) }

        DisposableEffect(uid) {
            if (uid == null) {
                onDispose {}
            } else {
                val registro =
                    FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(uid)
                        .addSnapshotListener { documento, error ->
                            if (error == null && documento != null && documento.exists()) {
                                val xpFirestore = documento.getLong("xp")?.toInt() ?: 0
                                val nivelFirestore = documento.getLong("nivel")?.toInt() ?: 1
                                progresoActual = xpFirestore to nivelFirestore
                            }
                        }
                onDispose { registro.remove() }
            }
        }

        val nivel = progresoActual?.second ?: (usuario?.nivel ?: 1)
        val xp = progresoActual?.first ?: (usuario?.xp ?: 0)

        // Mision seleccionada por el estudiante y progreso real de Firestore.
        var misionElegida by remember(uid) { mutableStateOf<String?>(null) }
        var progresosMisiones by remember(uid) { mutableStateOf<Map<String, Int>>(emptyMap()) }
        var recompensasMisiones by remember(uid) { mutableStateOf<Set<String>>(emptySet()) }
        DisposableEffect(uid) {
            if (uid == null) onDispose {}
            else {
                val usuarioRef =
                    FirebaseFirestore.getInstance().collection("usuarios").document(uid)
                val a =
                    usuarioRef.addSnapshotListener { snap, error ->
                        if (error == null) misionElegida = snap?.getString("misionActivaId")
                    }
                val b =
                    usuarioRef.collection("progreso_misiones").addSnapshotListener { snap, error ->
                        if (error == null)
                            progresosMisiones =
                                snap?.documents?.associate {
                                    it.id to (it.getLong("progreso")?.toInt() ?: 0).coerceIn(0, 100)
                                } ?: emptyMap()
                    }
                val c =
                    usuarioRef.collection("recompensas_otorgadas").addSnapshotListener { snap, error
                        ->
                        if (error == null)
                            recompensasMisiones =
                                snap?.documents?.map { it.id }?.toSet() ?: emptySet()
                    }
                onDispose {
                    a.remove()
                    b.remove()
                    c.remove()
                }
            }
        }
        // Catálogo completo: los IDs deben coincidir con MisionesScreen.kt.
        val catalogoMision =
            listOf(
                Triple("primer_ahorro_semanal", "Primer Ahorro Semanal", 250),
                Triple("maestro_presupuesto", "Maestro de Presupuesto", 150),
                Triple("inversionista_novato", "Inversionista Novato", 100),
                Triple("primer_movimiento", "Primer Movimiento", 50),
                Triple("organizador_financiero", "Organizador Financiero", 100),
                Triple("mi_primera_meta", "Mi Primera Meta", 100),
                Triple("planificador_mes", "Planificador del Mes", 150),
                Triple("ahorrador_constante", "Ahorrador Constante", 200),
                Triple("explorador_financiero", "Explorador Financiero", 200),
                Triple("experto_finanzas", "Experto en Finanzas", 300),
            )

        // Respetar la elección guardada, incluso cuando esté completada.
        // Sin selección guardada, invitar a elegir una desde Mis Misiones.
        val misionActiva = catalogoMision.firstOrNull { it.first == misionElegida }

        val descripcionesMisiones =
            mapOf(
                "primer_ahorro_semanal" to "Ahorra al menos $5.00 durante la semana.",
                "maestro_presupuesto" to
                    "Completa el módulo educativo sobre presupuestos universitarios.",
                "inversionista_novato" to "Aprende sobre inversiones y supera la evaluación.",
                "primer_movimiento" to "Registra tu primer ingreso o gasto.",
                "organizador_financiero" to "Registra cinco movimientos financieros.",
                "mi_primera_meta" to "Crea tu primera meta de ahorro.",
                "planificador_mes" to "Organiza tus finanzas con un presupuesto mensual.",
                "ahorrador_constante" to "Mantén el hábito de ahorrar.",
                "explorador_financiero" to "Explora las herramientas de educación financiera.",
                "experto_finanzas" to "Avanza en tu aprendizaje financiero.",
            )

        val ingresos =
            movimientoViewModel.movimientos.filter { it.tipo == "Ingreso" }.sumOf { it.monto }
        val gastos =
            movimientoViewModel.movimientos.filter { it.tipo == "Gasto" }.sumOf { it.monto }
        val saldo = cuentaViewModel.cuentas.filter { it.activa }.sumOf { it.saldo }
        val primerDiaMes =
            Calendar.getInstance()
                .apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                .timeInMillis
        val metasActivas = metasViewModel.metas.count { !it.completada }
        val presupuestoConAlerta =
            presupuestoViewModel.presupuestos.firstOrNull { presupuestoViewModel.alerta(it) }
        val gastoCategoria =
            presupuestoConAlerta?.let { presupuestoViewModel.gastoPresupuesto(it) } ?: 0.0
        val porcentajeAlerta =
            presupuestoConAlerta?.let { ((gastoCategoria / it.limite) * 100).toInt() } ?: 0

        Scaffold(
            containerColor = Fondo,
            topBar = {
                EncabezadoDashboard(onNotificaciones = onNotificaciones, onPerfil = onPerfil)
            },
        ) { paddingInterno ->
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(paddingInterno)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(18.dp))

                Text(
                    text = "Hola, $primerNombre",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Texto,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Tu dinero y tus metas, en un solo lugar.",
                    fontSize = 13.sp,
                    color = Gris,
                )

                Spacer(Modifier.height(16.dp))

                TarjetaSaldo(saldo = saldo, ingresos = ingresos, gastos = gastos)
                Spacer(Modifier.height(16.dp))
                MensajeMascota(nivel = nivel, xp = xp, onClick = onMascota)
                Spacer(Modifier.height(18.dp))
                ResumenGamificacion(
                    racha = rachaMovimientos(movimientoViewModel.movimientos),
                    metas = metasActivas,
                    nivel = nivel,
                )

                Spacer(Modifier.height(20.dp))

                if (presupuestoConAlerta != null) {
                    AlertaPresupuesto(
                        texto =
                            "${presupuestoConAlerta.categoria}: llevas $porcentajeAlerta% de tu presupuesto.",
                        onClick = onPresupuestos,
                    )
                    Spacer(Modifier.height(18.dp))
                }

                if (misionActiva != null) {
                    TarjetaMisionActiva(
                        titulo = misionActiva.second,
                        descripcion =
                            descripcionesMisiones[misionActiva.first]
                                ?: "Consulta los detalles de esta misión.",
                        xp = misionActiva.third,
                        progreso =
                            if (misionActiva.first in recompensasMisiones) 100
                            else progresosMisiones[misionActiva.first] ?: 0,
                        onClick = onMisiones,
                    )
                } else {
                    OutlinedButton(onClick = onMisiones, modifier = Modifier.fillMaxWidth()) {
                        Text("Elegir una misión activa")
                    }
                }
                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Presupuestos",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Texto,
                    )

                    TextButton(onClick = onPresupuestos) {
                        Text(text = "VER TODO", color = Verde, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                ResumenPresupuestos(
                    presupuestoViewModel.presupuestos,
                    presupuestoViewModel::gastoPresupuesto,
                )

                Spacer(Modifier.height(20.dp))

                AccionesRapidas(
                    onNuevoGasto = onNuevoGasto,
                    onNuevaMeta = onNuevaMeta,
                    onNuevaCategoria = onNuevaCategoria,
                    onReportes = onReportes,
                    onPresupuestos = onPresupuestos,
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onCuentas,
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(15.dp),
                        border = BorderStroke(1.dp, Rojo),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalance,
                            contentDescription = null,
                            tint = Rojo,
                            modifier = Modifier.size(18.dp),
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(text = "MIS CUENTAS", color = Rojo, fontSize = 11.sp)
                    }

                    FilledIconButton(
                        onClick = onRecompensas,
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Morado),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "Recompensas",
                            tint = Color.White,
                        )
                    }
                }

                Spacer(Modifier.height(26.dp))

                Text(
                    text = "XP acumulados: $xp · Nivel $nivel",
                    modifier = Modifier.fillMaxWidth(),
                    color = Gris,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun EncabezadoDashboard(onNotificaciones: () -> Unit, onPerfil: () -> Unit) {
    Surface(color = Fondo, shadowElevation = 1.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().height(58.dp).padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier.size(29.dp).clip(RoundedCornerShape(9.dp)).background(Verde),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Pets,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(9.dp))

            Text(
                text = "FinanQuest SV",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Texto,
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onNotificaciones) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notificaciones",
                    tint = Texto,
                )
            }

            IconButton(onClick = onPerfil) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Abrir perfil",
                    tint = Morado,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun MensajeMascota(nivel: Int, xp: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color(0xFFE3D8F5)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.financat_hero),
                contentDescription = "Abrir Mi mascota",
                modifier = Modifier.size(84.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Tu compañero financiero · Nivel $nivel",
                    color = Morado,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "Te faltan ${100 - (xp.coerceAtLeast(0) % 100)} XP para llegar al nivel ${nivel + 1}. ¡Vas bien!",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Texto,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "VER MI MASCOTA  ›",
                    color = Verde,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun TarjetaSaldo(saldo: Double, ingresos: Double, gastos: Double) {
    Column(
        Modifier.fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(Color(0xFF35215E), Color(0xFF7044BA))),
                RoundedCornerShape(28.dp),
            )
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "SALDO DISPONIBLE",
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = Color(0xFFE7DDF8),
                )
                Text(
                    formatoDinero(saldo),
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            QuestArtwork(R.drawable.presupuesto_wallet, Modifier.size(82.dp))
        }
        HorizontalDivider(color = Color.White.copy(alpha = .18f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    "INGRESOS",
                    color = Color(0xFF94EDC2),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    formatoDinero(ingresos),
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    "GASTOS",
                    color = Color(0xFFFFC5B7),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    formatoDinero(gastos),
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ResumenGamificacion(racha: Int, metas: Int, nivel: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        TarjetaEstadistica(
            icono = Icons.Filled.LocalFireDepartment,
            colorIcono = Color(0xFFF97316),
            valor = "$racha",
            etiqueta = "RACHA DÍAS",
            modifier = Modifier.weight(1f),
        )

        TarjetaEstadistica(
            icono = Icons.Filled.TrackChanges,
            colorIcono = Verde,
            valor = "$metas",
            etiqueta = "METAS",
            modifier = Modifier.weight(1f),
        )

        TarjetaEstadistica(
            icono = Icons.Filled.EmojiEvents,
            colorIcono = Morado,
            valor = "Lvl $nivel",
            etiqueta = "NIVEL",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TarjetaEstadistica(
    icono: ImageVector,
    colorIcono: Color,
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = Fondo,
        border = BorderStroke(1.dp, Borde),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            QuestArtwork(
                when (etiqueta) {
                    "RACHA DÍAS" -> R.drawable.quest_streak_fire
                    "NIVEL" -> R.drawable.quest_level_trophy
                    else -> R.drawable.quest_goal
                },
                Modifier.size(40.dp),
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = valor,
                color = if (etiqueta == "RACHA DÍAS") Color(0xFFE96B10) else Texto,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )

            Spacer(Modifier.height(3.dp))

            Text(text = etiqueta, color = Gris, fontSize = 11.sp, maxLines = 2)
        }
    }
}

@Composable
private fun AlertaPresupuesto(texto: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFFF3F2),
        border = BorderStroke(1.dp, Color(0xFFFFC9C4)),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFDC2626),
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = texto,
                color = Color(0xFFB91C1C),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f),
            )

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Rojo,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun TarjetaMisionActiva(
    titulo: String,
    descripcion: String,
    xp: Int,
    progreso: Int,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Borde),
        shadowElevation = 2.dp,
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.EmojiEvents, null, tint = Naranja)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Misión Activa",
                    color = Texto,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text("+$xp XP", color = Morado, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Text(titulo, fontWeight = FontWeight.SemiBold, color = Texto)
            Spacer(Modifier.height(5.dp))
            Text(descripcion, color = Gris, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("PROGRESO DE MISIÓN", color = Gris, fontSize = 11.sp)
                Text("${progreso.coerceIn(0,100)}%", color = Texto, fontSize = 11.sp)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progreso.coerceIn(0, 100) / 100f },
                modifier = Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(9.dp)),
                color = Morado,
                trackColor = Color(0xFFEAE7F2),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "VER TODAS MIS MISIONES  ›",
                color = Verde,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TarjetaMision(onClick: () -> Unit) {
    val progreso = 0.65f

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        color = Fondo,
        border = BorderStroke(1.dp, Borde),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Naranja)

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Misión Activa",
                    color = Texto,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )

                Text(
                    text = "+200 XP",
                    color = Morado,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Ahorra $20.00 en entretenimiento esta semana.",
                color = Gris,
                fontSize = 12.sp,
            )

            Spacer(Modifier.height(13.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("PROGRESO DE MISIÓN", color = Gris, fontSize = 11.sp)

                Text(
                    "65% de $20.00",
                    color = Texto,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(8.dp))

            BarraProgreso(progreso = progreso, color = Morado)

            Spacer(Modifier.height(12.dp))
            Text(
                text = "VER TODAS MIS MISIONES  ›",
                color = Verde,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun BarraProgreso(progreso: Float, color: Color) {
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE5E7EB))
    ) {
        Box(
            modifier =
                Modifier.fillMaxWidth(progreso.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(color)
        )
    }
}

@Composable
private fun ResumenPresupuestos(
    presupuestos: List<com.finanquest.sv.data.model.Presupuesto>,
    gasto: (com.finanquest.sv.data.model.Presupuesto) -> Double,
) {
    if (presupuestos.isEmpty()) {
        Text(
            "Aún no tienes presupuestos. Crea uno para ver aquí tu avance.",
            color = Gris,
            fontSize = 12.sp,
        )
        return
    }
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp), color = Fondo) {
        Column(modifier = Modifier.padding(16.dp)) {
            presupuestos
                .filter { it.activo }
                .take(3)
                .forEachIndexed { index, p ->
                    val usado = gasto(p)
                    val porcentaje =
                        if (p.limite > 0) (usado / p.limite * 100).toInt().coerceIn(0, 100) else 0
                    FilaPresupuesto(
                        p.categoria.uppercase(),
                        porcentaje,
                        p.limite,
                        if (porcentaje >= (p.alertaUmbral * 100).toInt()) Rojo else Verde,
                    )
                    if (index < minOf(2, presupuestos.count { it.activo } - 1))
                        Spacer(Modifier.height(16.dp))
                }
        }
    }
}

@Composable
private fun FilaPresupuesto(nombre: String, porcentaje: Int, limite: Double, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = nombre, color = Gris, fontSize = 11.sp)

        Text(
            text = "$porcentaje% de ${formatoDinero(limite)}",
            color = Texto,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
    }

    Spacer(Modifier.height(9.dp))

    BarraProgreso(progreso = porcentaje / 100f, color = color)
}

@Composable
private fun AccionesRapidas(
    onNuevoGasto: () -> Unit,
    onNuevaMeta: () -> Unit,
    onNuevaCategoria: () -> Unit,
    onReportes: () -> Unit,
    onPresupuestos: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BotonAccion(
                texto = "NUEVO GASTO",
                icono = Icons.Filled.Add,
                colorIcono = Azul,
                onClick = onNuevoGasto,
                modifier = Modifier.weight(1f),
            )

            BotonAccion(
                texto = "NUEVA META",
                icono = Icons.Filled.TrackChanges,
                colorIcono = Verde,
                onClick = onNuevaMeta,
                modifier = Modifier.weight(1f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BotonAccion(
                texto = "NUEVA CATEGORÍA",
                icono = Icons.Filled.Add,
                colorIcono = Naranja,
                onClick = onNuevaCategoria,
                modifier = Modifier.weight(1f),
            )

            BotonAccion(
                texto = "REPORTES",
                icono = Icons.Filled.Assessment,
                colorIcono = Morado,
                onClick = onReportes,
                modifier = Modifier.weight(1f),
                borde = Morado,
            )
        }

        BotonAccion(
            texto = "PRESUPUESTOS",
            icono = Icons.Filled.AccountBalanceWallet,
            colorIcono = Verde,
            onClick = onPresupuestos,
            modifier = Modifier.fillMaxWidth(),
            borde = Verde,
        )
    }
}

@Composable
private fun BotonAccion(
    texto: String,
    icono: ImageVector,
    colorIcono: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borde: Color = Borde,
) {
    Surface(
        modifier = modifier.heightIn(min = 64.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borde),
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuestArtwork(
                if (texto.contains("META")) R.drawable.quest_goal
                else if (texto.contains("REPORT")) R.drawable.presupuesto_education
                else R.drawable.presupuesto_wallet,
                Modifier.size(36.dp),
            )

            Spacer(Modifier.width(5.dp))

            Text(
                text = texto,
                color = if (borde == Morado) Morado else Texto,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
