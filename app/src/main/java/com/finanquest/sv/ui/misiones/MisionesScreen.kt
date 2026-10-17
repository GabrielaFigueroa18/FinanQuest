
package com.finanquest.sv.ui.misiones

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val Fondo = Color(0xFFF7F8FA)
private val Morado = Color(0xFF9B70FA)
private val Verde = Color(0xFF16A34A)
private val Texto = Color(0xFF22252B)
private val Gris = Color(0xFF777D87)
private val Borde = Color(0xFFE5E7EB)

private data class Mision(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val xp: Int,
    val progreso: Int,
    val icono: ImageVector,
    val detalle: String
)

// Catálogo inicial local; el progreso de cada usuario se consulta en Firestore.
private val misionesDemo = listOf(
    Mision(
        "primer_ahorro_semanal", "Primer Ahorro Semanal",
        "Guarda al menos $5.00 en tu alcancía digital esta semana.",
        250, 0, Icons.Filled.Savings,
        "Ahorra $5.00 durante la semana. Cuando conectemos el módulo de ahorro, el progreso se calculará automáticamente."
    ),
    Mision(
        "maestro_presupuesto", "Maestro de Presupuesto",
        "Completa el módulo educativo sobre presupuestos universitarios.",
        150, 0, Icons.Filled.School,
        "Completa la lección de presupuestos y su evaluación para recibir la recompensa."
    ),
    Mision(
        "inversionista_novato", "Inversionista Novato",
        "Lee el artículo sobre fondos de inversión locales.",
        100, 0, Icons.Filled.TrendingUp,
        "Lee el contenido educativo sobre inversión y responde la actividad de comprensión."
    )
)

@Composable
fun MisionesScreen(
    onVolver: () -> Unit,
    onInicio: () -> Unit = onVolver,
    onPerfil: () -> Unit = {},
    onAbrirLeccionInversiones: () -> Unit = {}
) {
    var busqueda by remember { mutableStateOf("") }
    var pestana by remember { mutableIntStateOf(0) }
    var misionDetalle by remember { mutableStateOf<Mision?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(uid) {
        Log.d("MISIONES_DEBUG", "UID actual: $uid")
    }
    var progresos by remember(uid) { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var cargandoMisiones by remember(uid) { mutableStateOf(uid != null) }
    var errorMisiones by remember(uid) { mutableStateOf<String?>(null) }

    DisposableEffect(uid) {
        if (uid == null) {
            cargandoMisiones = false
            errorMisiones = "Inicia sesión para consultar tus misiones."
            onDispose { }
        } else {
            val registro = FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .collection("progreso_misiones")
                .addSnapshotListener { instantanea, error ->
                    Log.d(
                        "MISIONES_DEBUG",
                        "Error: ${error?.message}, documentos: ${instantanea?.documents?.size}"
                    )
                    instantanea?.documents?.forEach { documento ->
                        Log.d(
                            "MISIONES_DEBUG",
                            "Documento: ${documento.id}, progreso: ${documento.get("progreso")}"
                        )
                    }
                    cargandoMisiones = false
                    if (error != null) {
                        errorMisiones = "No se pudo cargar el progreso: ${error.localizedMessage}"
                    } else {
                        errorMisiones = null
                        progresos = instantanea?.documents?.associate { documento ->
                            documento.id to ((documento.getLong("progreso") ?: 0L)
                                .toInt().coerceIn(0, 100))
                        } ?: emptyMap()
                    }
                }
            onDispose { registro.remove() }
        }
    }

    val misiones = misionesDemo.map { mision ->
        mision.copy(progreso = progresos[mision.id] ?: 0)
    }

    val visibles = misiones.filter {
        it.titulo.contains(busqueda.trim(), ignoreCase = true) ||
                it.descripcion.contains(busqueda.trim(), ignoreCase = true)
    }.filter { if (pestana == 0) it.progreso < 100 else it.progreso == 100 }

    Scaffold(
        containerColor = Fondo,
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 3.dp) {
                ItemInferior("Inicio", Icons.Filled.Home, false, onInicio)
                ItemInferior("Movimientos", Icons.Filled.AccountBalanceWallet, false) {
                    aviso = "Movimientos se conectará próximamente."
                }
                ItemInferior("Metas", Icons.Filled.TrackChanges, false) {
                    aviso = "Metas se conectará próximamente."
                }
                ItemInferior("Mascota", Icons.Filled.Pets, false) {
                    aviso = "Mascota se conectará próximamente."
                }
                ItemInferior("Nivel", Icons.Filled.EmojiEvents, false) {
                    aviso = "Nivel se conectará próximamente."
                }
                ItemInferior("Perfil", Icons.Filled.PersonOutline, false, onPerfil)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(start = 12.dp, end = 16.dp, top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mis Misiones", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Texto)
                    Text("Supera desafíos para subir de nivel", fontSize = 12.sp, color = Gris)
                }
                Surface(shape = RoundedCornerShape(30.dp), color = Color(0xFFFFF2E4)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalFireDepartment, null, tint = Color(0xFFFF9100), modifier = Modifier.size(19.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("5", color = Color(0xFF9A5B00), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(7.dp))
                IconButton(onClick = { aviso = "El seguimiento de rachas se conectará próximamente." }) {
                    Icon(Icons.Filled.TrendingUp, "Racha", tint = Verde)
                }
            }

            Spacer(Modifier.height(15.dp))
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar misiones...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Morado,
                    unfocusedContainerColor = Color(0xFFF0F1F3),
                    focusedContainerColor = Color.White
                )
            )
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Borde)
            Spacer(Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .background(Color(0xFFF1F1F4), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                listOf("Activas  ${misiones.count { it.progreso < 100 }}", "Completadas").forEachIndexed { index, texto ->
                    Surface(
                        modifier = Modifier.weight(1f).clickable { pestana = index },
                        shape = RoundedCornerShape(14.dp),
                        color = if (pestana == index) Morado else Color.Transparent,
                        shadowElevation = if (pestana == index) 2.dp else 0.dp
                    ) {
                        Box(Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                            Text(
                                texto,
                                color = if (pestana == index) Color.White else Morado,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))

            if (cargandoMisiones) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                Spacer(Modifier.height(12.dp))
            }
            errorMisiones?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
            if (visibles.isEmpty()) {
                Text(
                    if (pestana == 1) "Todavía no tienes misiones completadas."
                    else "No se encontraron misiones.",
                    modifier = Modifier.fillMaxWidth().padding(28.dp),
                    color = Gris
                )
            } else {
                visibles.forEach { mision ->
                    TarjetaMision(mision, onDetalle = {
                        if (mision.id == "inversionista_novato") {
                            onAbrirLeccionInversiones()
                        } else {
                            misionDetalle = mision
                        }
                    })
                    Spacer(Modifier.height(14.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFE4D8F7),
                border = BorderStroke(1.dp, Color(0xFFD7C5F2))
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text("¡Sigue así!", fontWeight = FontWeight.Bold, color = Verde)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "Completar misiones te otorgará FinaCoins que podrás usar para personalizar tu FinanCat.",
                        color = Gris, fontSize = 13.sp, lineHeight = 19.sp
                    )
                }
            }
            Spacer(Modifier.height(25.dp))
        }
    }

    misionDetalle?.let { mision ->
        AlertDialog(
            onDismissRequest = { misionDetalle = null },
            title = { Text(mision.titulo) },
            text = {
                Column {
                    Text(mision.detalle)
                    Spacer(Modifier.height(12.dp))
                    Text("Progreso registrado: ${mision.progreso}%")
                    Text("Recompensa prevista: +${mision.xp} XP", color = Verde)
                }
            },
            confirmButton = {
                TextButton(onClick = { misionDetalle = null }) { Text("Entendido") }
            }
        )
    }
    aviso?.let { texto ->
        AlertDialog(
            onDismissRequest = { aviso = null },
            title = { Text("Próximamente") },
            text = { Text(texto) },
            confirmButton = { TextButton(onClick = { aviso = null }) { Text("Aceptar") } }
        )
    }
}

@Composable
private fun TarjetaMision(mision: Mision, onDetalle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE0F1E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(mision.icono, null, tint = Morado, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(mision.titulo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Texto)
                    Spacer(Modifier.height(5.dp))
                    Text(mision.descripcion, fontSize = 12.sp, color = Gris, lineHeight = 17.sp)
                }
                Spacer(Modifier.width(5.dp))
                Text("+${mision.xp} XP", color = Verde, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("PROGRESO", color = Gris, fontSize = 10.sp)
                Text("${mision.progreso}%", color = Gris, fontSize = 11.sp)
            }
            Spacer(Modifier.height(7.dp))
            LinearProgressIndicator(
                progress = { mision.progreso / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(10.dp)),
                color = Verde, trackColor = Color(0xFFE7E8EB)
            )
            Spacer(Modifier.height(17.dp))
            HorizontalDivider(color = Borde)
            Spacer(Modifier.height(13.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎁", fontSize = 21.sp)
                Spacer(Modifier.width(10.dp))
                Text("Recompensa de XP", color = Texto, fontSize = 12.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = onDetalle, contentPadding = PaddingValues(0.dp)) {
                    Text("VER DETALLES", color = Verde, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Filled.ChevronRight, null, tint = Verde, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun RowScope.ItemInferior(
    titulo: String,
    icono: ImageVector,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = seleccionado,
        onClick = onClick,
        icon = { Icon(icono, titulo, modifier = Modifier.size(20.dp)) },
        label = { Text(titulo, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Morado,
            selectedTextColor = Morado,
            indicatorColor = Color(0xFFEDE9FE),
            unselectedIconColor = Gris,
            unselectedTextColor = Gris
        )
    )
}
