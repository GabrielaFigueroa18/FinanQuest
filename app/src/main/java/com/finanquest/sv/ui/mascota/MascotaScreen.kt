
package com.finanquest.sv.ui.mascota

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanquest.sv.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

private val verde = Color(0xFF14A247)
private val morado = Color(0xFF7C3AED)
private val lila = Color(0xFFF0E9FF)
private val fondoPantalla = Color(0xFFF7F8FA)

private data class PremioMascota(
    val id: String,
    val nombre: String,
    val emoji: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MascotaScreen(onVolver: () -> Unit) {

    val uid = remember {
        FirebaseAuth.getInstance().currentUser?.uid
    }
    val db = remember { FirebaseFirestore.getInstance() }

    var nivel by remember(uid) { mutableIntStateOf(1) }
    var xp by remember(uid) { mutableIntStateOf(0) }
    var nombre by remember(uid) { mutableStateOf("Michi") }

    var accesorio by remember(uid) {
        mutableStateOf("ninguno")
    }
    var fondo by remember(uid) {
        mutableStateOf("normal")
    }
    var desbloqueadas by remember(uid) {
        mutableStateOf<Set<String>>(emptySet())
    }

    var pestana by remember { mutableIntStateOf(0) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }
    var caricias by remember { mutableIntStateOf(0) }

    DisposableEffect(uid) {
        if (uid == null) {
            mensaje = "Inicia sesión para ver tu mascota"
            onDispose { }
        } else {
            val usuario = db.collection("usuarios").document(uid)

            val perfilListener = usuario.addSnapshotListener {
                    documento, error ->
                if (error != null) {
                    mensaje = error.localizedMessage
                } else {
                    nivel = (documento?.getLong("nivel") ?: 1L)
                        .toInt().coerceAtLeast(1)
                    xp = (documento?.getLong("xp") ?: 0L)
                        .toInt().coerceAtLeast(0)
                }
            }

            val premiosListener = usuario
                .collection("recompensas_desbloqueadas")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        mensaje = error.localizedMessage
                    } else {
                        desbloqueadas = snapshot?.documents
                            ?.filter {
                                it.getBoolean("desbloqueada") == true
                            }
                            ?.map { it.id }
                            ?.toSet()
                            ?: emptySet()
                    }
                }

            val mascotaListener = usuario
                .collection("mascota_configuracion")
                .document("actual")
                .addSnapshotListener { documento, error ->
                    if (error != null) {
                        mensaje = error.localizedMessage
                    } else if (documento?.exists() == true) {
                        nombre = documento.getString("nombre")
                            ?: "Michi"
                        accesorio = documento.getString("accesorio")
                            ?: "ninguno"
                        fondo = documento.getString("fondo")
                            ?: "normal"
                    }
                }

            onDispose {
                perfilListener.remove()
                premiosListener.remove()
                mascotaListener.remove()
            }
        }
    }

    val accesorios = listOf(
        PremioMascota("ninguno", "Sin accesorio", "🐾"),
        PremioMascota("sombrero", "Sombrero", "🎩"),
        PremioMascota("lentes", "Lentes", "👓"),
        PremioMascota("corona", "Corona", "👑")
    )

    val fondos = listOf(
        PremioMascota("normal", "Original", "🤍"),
        PremioMascota("fondo_morado", "Violeta", "💜"),
        PremioMascota("fondo_verde", "Naturaleza", "🌿")
    )

    val colorFondo = when (fondo) {
        "fondo_morado" -> lila
        "fondo_verde" -> Color(0xFFE2F6E8)
        else -> Color(0xFFF2F0FF)
    }

    val etapa = when {
        nivel >= 10 -> "Gatito Maestro"
        nivel >= 5 -> "Gatito Explorador"
        else -> "Gatito Aprendiz"
    }

    val progreso = (xp % 100) / 100f
    val xpRestantes = if (xp % 100 == 0) 100
    else 100 - (xp % 100)

    Scaffold(
        containerColor = fondoPantalla,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi mascota",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Tarjeta principal del gatito
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorFondo
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "⚡ Energía activa",
                            color = morado,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "⭐ Nivel $nivel",
                            color = verde,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(
                                id = R.drawable.img
                            ),
                            contentDescription = "Gatito FinanQuest",
                            modifier = Modifier.size(220.dp),
                            contentScale = ContentScale.Fit
                        )

                        if (accesorio != "ninguno" &&
                            accesorio in desbloqueadas) {
                            Text(
                                text = when (accesorio) {
                                    "sombrero" -> "🎩"
                                    "lentes" -> "👓"
                                    "corona" -> "👑"
                                    else -> ""
                                },
                                fontSize = 38.sp,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                            )
                        }
                    }

                    Text(
                        nombre,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        "🟢 $etapa",
                        color = verde,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Tu compañero financiero 💜",
                        color = morado,
                        fontSize = 13.sp
                    )
                }
            }

            // Pestañas
            TabRow(
                selectedTabIndex = pestana,
                containerColor = Color.White,
                contentColor = morado
            ) {
                listOf("Progreso", "Evolución", "Armario")
                    .forEachIndexed { index, titulo ->
                        Tab(
                            selected = pestana == index,
                            onClick = { pestana = index },
                            text = {
                                Text(
                                    titulo,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        )
                    }
            }

            when (pestana) {

                0 -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            Modifier.padding(18.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Vínculo actual",
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            )

                            Text(
                                "¡Sigue completando misiones para " +
                                        "crecer junto a tu gatito!",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )

                            LinearProgressIndicator(
                                progress = { progreso },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp),
                                color = verde,
                                trackColor = lila
                            )

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {
                                Text("Nivel $nivel")
                                Text("Nivel ${nivel + 1}")
                            }

                            Text(
                                "$xp XP acumulados · " +
                                        "$xpRestantes XP para el siguiente nivel",
                                color = morado,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        DatoMascota(
                            "🏆",
                            "Nivel",
                            "$nivel",
                            Modifier.weight(1f)
                        )
                        DatoMascota(
                            "⭐",
                            "Experiencia",
                            "$xp XP",
                            Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            caricias++
                            mensaje = "¡A $nombre le encantan " +
                                    "tus caricias! 💜"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = verde
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "💚 Mimar a mi gatito",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (caricias > 0) {
                        Text(
                            "Caricias de esta visita: $caricias 🐾",
                            color = morado,
                            fontSize = 12.sp
                        )
                    }
                }

                1 -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            Modifier.padding(18.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                "Evolución de tu gatito",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )

                            EtapaMascota(
                                "🐱 Gatito Aprendiz",
                                "Desde el nivel 1",
                                true
                            )

                            EtapaMascota(
                                "🌟 Gatito Explorador",
                                "Desde el nivel 5",
                                nivel >= 5
                            )

                            EtapaMascota(
                                "👑 Gatito Maestro",
                                "Desde el nivel 10",
                                nivel >= 10
                            )

                            Text(
                                "Las etapas cambian según tu nivel. " +
                                        "La ilustración del gatito se mantiene " +
                                        "hasta agregar sus versiones evolucionadas.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                2 -> {
                    Text(
                        "Accesorios",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    LazyRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {
                        items(accesorios) { item ->
                            val disponible =
                                item.id == "ninguno" ||
                                        item.id in desbloqueadas

                            PremioSeleccionable(
                                item,
                                accesorio == item.id,
                                disponible
                            ) {
                                accesorio = item.id
                            }
                        }
                    }

                    Text(
                        "Fondos",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    LazyRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {
                        items(fondos) { item ->
                            val disponible =
                                item.id == "normal" ||
                                        item.id in desbloqueadas

                            PremioSeleccionable(
                                item,
                                fondo == item.id,
                                disponible
                            ) {
                                fondo = item.id
                            }
                        }
                    }

                    Text(
                        "Desbloquea nuevos accesorios y fondos " +
                                "desde Mis recompensas.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )

                    Button(
                        onClick = {
                            if (uid == null) {
                                mensaje = "Inicia sesión primero."
                            } else if (
                                (accesorio != "ninguno" &&
                                        accesorio !in desbloqueadas) ||
                                (fondo != "normal" &&
                                        fondo !in desbloqueadas)
                            ) {
                                mensaje =
                                    "Selecciona premios desbloqueados."
                            } else {
                                guardando = true
                                mensaje = null

                                db.collection("usuarios")
                                    .document(uid)
                                    .collection("mascota_configuracion")
                                    .document("actual")
                                    .set(
                                        mapOf(
                                            "nombre" to nombre,
                                            "accesorio" to accesorio,
                                            "fondo" to fondo
                                        ),
                                        SetOptions.merge()
                                    )
                                    .addOnSuccessListener {
                                        guardando = false
                                        mensaje =
                                            "Personalización guardada 💚"
                                    }
                                    .addOnFailureListener { error ->
                                        guardando = false
                                        mensaje =
                                            "Error: ${error.localizedMessage}"
                                    }
                            }
                        },
                        enabled = !guardando && uid != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = verde
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Guardar personalización")
                        }
                    }
                }
            }

            mensaje?.let {
                Text(
                    it,
                    color = morado,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DatoMascota(
    icono: String,
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icono, fontSize = 25.sp)
            Text(
                titulo,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                valor,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        }
    }
}

@Composable
private fun EtapaMascota(
    nombre: String,
    requisito: String,
    desbloqueada: Boolean
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (desbloqueada) {
            Color(0xFFE4F7EA)
        } else {
            Color(0xFFF4F4F6)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    nombre,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    requisito,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                if (desbloqueada) "✓" else "🔒",
                color = verde
            )
        }
    }
}

@Composable
private fun PremioSeleccionable(
    premio: PremioMascota,
    seleccionado: Boolean,
    disponible: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(110.dp)
            .clickable(
                enabled = disponible,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (seleccionado) lila else Color.White,
        border = BorderStroke(
            if (seleccionado) 2.dp else 1.dp,
            if (seleccionado) morado
            else Color(0xFFE5E7EB)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (disponible) {
                Text(premio.emoji, fontSize = 30.sp)
            } else {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Bloqueado",
                    tint = Color.Gray
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                premio.nombre,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Text(
                if (disponible) "Disponible" else "Bloqueado",
                fontSize = 10.sp,
                color = if (disponible) verde else Color.Gray
            )
        }
    }
}
