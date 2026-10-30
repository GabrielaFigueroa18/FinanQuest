package com.finanquest.sv.ui.misiones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanquest.sv.R
import com.finanquest.sv.ui.components.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val Fondo: Color
    @Composable get() = MaterialTheme.colorScheme.background
private val Morado: Color
    @Composable get() = MaterialTheme.colorScheme.secondary
private val Verde: Color
    @Composable get() = MaterialTheme.colorScheme.primary
private val Texto: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val Gris: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val Borde: Color
    @Composable get() = MaterialTheme.colorScheme.outline

private data class Mision(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val xp: Int,
    val icono: ImageVector,
)

private val catalogoMisiones =
    listOf(
        Mision(
            id = "primer_ahorro_semanal",
            titulo = "Primer Ahorro Semanal",
            descripcion = "Guarda al menos $5.00 en tu alcancía digital esta semana.",
            xp = 250,
            icono = Icons.Filled.Savings,
        ),
        Mision(
            id = "maestro_presupuesto",
            titulo = "Maestro de Presupuesto",
            descripcion = "Completa el módulo educativo sobre presupuestos universitarios.",
            xp = 150,
            icono = Icons.Filled.School,
        ),
        Mision(
            id = "inversionista_novato",
            titulo = "Inversionista Novato",
            descripcion = "Aprende sobre inversiones y supera la evaluación.",
            xp = 100,
            icono = Icons.Filled.TrendingUp,
        ),
        Mision(
            id = "primer_movimiento",
            titulo = "Primer Movimiento",
            descripcion = "Registra tu primer ingreso o gasto en FinanQuest.",
            xp = 50,
            icono = Icons.Filled.AccountBalanceWallet,
        ),
        Mision(
            id = "organizador_financiero",
            titulo = "Organizador Financiero",
            descripcion = "Registra cinco movimientos financieros.",
            xp = 100,
            icono = Icons.Filled.ReceiptLong,
        ),
        Mision(
            id = "mi_primera_meta",
            titulo = "Mi Primera Meta",
            descripcion = "Crea tu primera meta de ahorro.",
            xp = 100,
            icono = Icons.Filled.Flag,
        ),
        Mision(
            id = "planificador_mes",
            titulo = "Planificador del Mes",
            descripcion = "Crea un presupuesto para organizar tus gastos.",
            xp = 150,
            icono = Icons.Filled.CalendarMonth,
        ),
        Mision(
            id = "ahorrador_constante",
            titulo = "Ahorrador Constante",
            descripcion = "Registra ahorros durante tres días diferentes.",
            xp = 200,
            icono = Icons.Filled.Savings,
        ),
        Mision(
            id = "explorador_financiero",
            titulo = "Explorador Financiero",
            descripcion = "Completa tres lecciones de educación financiera.",
            xp = 200,
            icono = Icons.Filled.MenuBook,
        ),
        Mision(
            id = "experto_finanzas",
            titulo = "Experto en Finanzas",
            descripcion = "Completa cinco misiones verificadas.",
            xp = 300,
            icono = Icons.Filled.EmojiEvents,
        ),
    )

@Composable
fun MisionesScreen(
    onVolver: () -> Unit,
    onInicio: () -> Unit = onVolver,
    onPerfil: () -> Unit = {},
    onAbrirLeccionInversiones: () -> Unit = {},
) {
    QuestVisualTheme {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val firestore = remember { FirebaseFirestore.getInstance() }

        var busqueda by remember { mutableStateOf("") }
        var pestana by remember { mutableIntStateOf(0) }

        var misionActivaId by remember(uid) { mutableStateOf<String?>(null) }

        var progresos by remember(uid) { mutableStateOf<Map<String, Int>>(emptyMap()) }

        var recompensas by remember(uid) { mutableStateOf<Set<String>>(emptySet()) }

        var cargando by remember(uid) { mutableStateOf(uid != null) }

        var aviso by remember { mutableStateOf<String?>(null) }
        var guardando by remember { mutableStateOf(false) }
        var errorProgreso by remember { mutableStateOf<String?>(null) }
        var errorRecompensas by remember { mutableStateOf(false) }

        var detalle by remember { mutableStateOf<Mision?>(null) }

        // ESCUCHAR LOS CAMBIOS EN FIREBASE
        DisposableEffect(uid) {
            if (uid == null) {
                cargando = false
                errorProgreso = "Inicia sesión para consultar tus misiones."
                onDispose {}
            } else {
                val usuarioRef = firestore.collection("usuarios").document(uid)

                val listenerUsuario =
                    usuarioRef.addSnapshotListener { snapshot, error ->
                        if (error == null) {
                            misionActivaId = snapshot?.getString("misionActivaId")
                        }
                    }

                val listenerProgreso =
                    usuarioRef.collection("progreso_misiones").addSnapshotListener { snapshot, error
                        ->
                        cargando = false

                        if (error != null) {
                            errorProgreso = "No se pudo consultar el progreso."
                        } else {
                            errorProgreso = null

                            progresos =
                                snapshot?.documents?.associate { documento ->
                                    documento.id to
                                        (documento.getLong("progreso")?.toInt() ?: 0).coerceIn(
                                            0,
                                            100,
                                        )
                                } ?: emptyMap()
                        }
                    }

                val listenerRecompensas =
                    usuarioRef.collection("recompensas_otorgadas").addSnapshotListener {
                        snapshot,
                        error ->
                        if (error != null) {
                            errorRecompensas = true
                        } else {
                            errorRecompensas = false

                            recompensas = snapshot?.documents?.map { it.id }?.toSet() ?: emptySet()
                        }
                    }

                onDispose {
                    listenerUsuario.remove()
                    listenerProgreso.remove()
                    listenerRecompensas.remove()
                }
            }
        }

        // GUARDAR LA MISION SELECCIONADA
        fun seleccionarMision(mision: Mision) {
            if (uid == null) {
                aviso = "Debes iniciar sesión."
                return
            }

            if (guardando) return

            if (mision.id in recompensas || (progresos[mision.id] ?: 0) >= 100) {
                aviso = "Esta misión ya está completada."
                return
            }

            guardando = true

            firestore
                .collection("usuarios")
                .document(uid)
                .update("misionActivaId", mision.id)
                .addOnSuccessListener {
                    misionActivaId = mision.id
                    guardando = false
                    aviso = "¡Misión activa actualizada!"
                }
                .addOnFailureListener { error ->
                    guardando = false
                    aviso = "No se pudo guardar: ${error.localizedMessage}"
                }
        }

        val misionesFiltradas =
            catalogoMisiones.filter { mision ->
                val coincide =
                    mision.titulo.contains(busqueda.trim(), ignoreCase = true) ||
                        mision.descripcion.contains(busqueda.trim(), ignoreCase = true)

                val completada = (progresos[mision.id] ?: 0) >= 100 || mision.id in recompensas

                coincide &&
                    if (pestana == 0) {
                        !completada
                    } else {
                        completada
                    }
            }

        Scaffold(
            containerColor = Fondo,
            topBar = {
                Surface(color = Color.White) {
                    Row(
                        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onVolver) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Mis Misiones",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Texto,
                            )

                            Text(
                                "Supera desafíos y gana recompensas",
                                fontSize = 12.sp,
                                color = Gris,
                            )
                        }

                        IconButton(onClick = onPerfil) {
                            Icon(
                                Icons.Filled.PersonOutline,
                                contentDescription = "Abrir perfil",
                                tint = Morado,
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar misiones...") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(15.dp),
                )

                Spacer(Modifier.height(18.dp))

                // PESTAÑAS
                TabRow(
                    selectedTabIndex = pestana,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Morado,
                ) {
                    Tab(
                        selected = pestana == 0,
                        onClick = { pestana = 0 },
                        text = { Text("Pendientes") },
                    )

                    Tab(
                        selected = pestana == 1,
                        onClick = { pestana = 1 },
                        text = { Text("Completadas") },
                    )
                }

                Spacer(Modifier.height(18.dp))

                if (cargando) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Morado)
                    Spacer(Modifier.height(16.dp))
                }

                errorProgreso?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                }

                if (errorRecompensas) {
                    Text("No se pudo verificar la entrega de XP.", color = Gris, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                }

                if (!cargando && misionesFiltradas.isEmpty()) {
                    Text(
                        if (pestana == 0) "No hay misiones pendientes."
                        else "Todavía no tienes misiones completadas.",
                        color = Gris,
                        modifier = Modifier.padding(16.dp),
                    )
                }

                misionesFiltradas.forEach { mision ->
                    val progreso = progresos[mision.id] ?: 0

                    val confirmada = mision.id in recompensas

                    val esActiva = misionActivaId == mision.id

                    TarjetaMision(
                        mision = mision,
                        progreso = progreso,
                        recompensaConfirmada = confirmada,
                        esActiva = esActiva,
                        onDetalle = {
                            if (
                                mision.id == "inversionista_novato" && progreso < 100 && !confirmada
                            ) {
                                onAbrirLeccionInversiones()
                            } else {
                                detalle = mision
                            }
                        },
                        onSeleccionar = { seleccionarMision(mision) },
                        guardando = guardando,
                    )

                    Spacer(Modifier.height(14.dp))
                }

                Spacer(Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFEDE4FC),
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("¡Sigue así!", color = Verde, fontWeight = FontWeight.Bold)

                        Spacer(Modifier.height(6.dp))

                        Text(
                            "Completa misiones para ganar XP, " +
                                "subir de nivel y desbloquear recompensas.",
                            color = Gris,
                            fontSize = 13.sp,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        // DETALLES DE LA MISION
        detalle?.let { mision ->
            val progreso = progresos[mision.id] ?: 0
            val confirmada = mision.id in recompensas

            AlertDialog(
                onDismissRequest = { detalle = null },
                title = { Text(mision.titulo) },
                text = {
                    Column {
                        Text(mision.descripcion)

                        Spacer(Modifier.height(12.dp))

                        Text("Progreso: $progreso%")

                        Spacer(Modifier.height(8.dp))

                        Text(
                            when {
                                confirmada -> "Recompensa entregada: +${mision.xp} XP"
                                progreso >= 100 -> "Recompensa pendiente de validación"
                                else -> "Recompensa disponible: +${mision.xp} XP"
                            },
                            color = Verde,
                        )
                    }
                },
                confirmButton = { TextButton(onClick = { detalle = null }) { Text("Cerrar") } },
            )
        }

        aviso?.let { mensaje ->
            AlertDialog(
                onDismissRequest = { aviso = null },
                title = { Text("FinanQuest SV") },
                text = { Text(mensaje) },
                confirmButton = { TextButton(onClick = { aviso = null }) { Text("Aceptar") } },
            )
        }
    }
}

@Composable
private fun TarjetaMision(
    mision: Mision,
    progreso: Int,
    recompensaConfirmada: Boolean,
    esActiva: Boolean,
    onDetalle: () -> Unit,
    onSeleccionar: () -> Unit,
    guardando: Boolean,
) {
    val completada = progreso >= 100 || recompensaConfirmada

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (esActiva) Morado else Borde),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier =
                        Modifier.size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEDE4FC)),
                    contentAlignment = Alignment.Center,
                ) {
                    QuestArtwork(
                        if (mision.titulo.contains("Ahorr", true)) R.drawable.quest_goal
                        else R.drawable.quest_reward,
                        Modifier.size(48.dp),
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        mision.titulo,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Texto,
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(mision.descripcion, fontSize = 12.sp, color = Gris)
                }

                Text(
                    "+${mision.xp} XP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Verde,
                )
            }

            if (esActiva && !completada) {
                Spacer(Modifier.height(10.dp))

                Text(
                    "✓ MISIÓN ACTIVA",
                    color = Morado,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("PROGRESO", color = Gris, fontSize = 11.sp)

                Text("$progreso%", color = Texto, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progreso.coerceIn(0, 100) / 100f },
                modifier = Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(9.dp)),
                color = Morado,
                trackColor = Color(0xFFEAE7F2),
            )

            Spacer(Modifier.height(14.dp))

            Text(
                when {
                    recompensaConfirmada -> "XP registrados correctamente"
                    progreso >= 100 -> "XP pendientes de confirmar"
                    else -> "Recompensa: ${mision.xp} XP"
                },
                color = Gris,
                fontSize = 12.sp,
            )

            Spacer(Modifier.height(12.dp))

            if (!completada) {
                OutlinedButton(
                    onClick = onSeleccionar,
                    enabled = !guardando && !esActiva,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Morado),
                ) {
                    Text(
                        if (esActiva) "✓ Misión seleccionada" else "Elegir como misión activa",
                        color = if (esActiva) Gris else Morado,
                    )
                }
            }

            TextButton(onClick = onDetalle, modifier = Modifier.align(Alignment.End)) {
                Text(
                    if (mision.id == "inversionista_novato" && !completada) "INICIAR LECCIÓN"
                    else "VER DETALLES",
                    color = Verde,
                )

                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Verde)
            }
        }
    }
}
