
package com.finanquest.sv.ui.misiones

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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanquest.sv.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ==========================================
// COLORES FINANQUEST
// ==========================================

private val Fondo: Color @Composable get() = MaterialTheme.colorScheme.background
private val Morado = Color(0xFF7045B7)
private val MoradoClaro = Color(0xFFF2ECFC)
private val Verde = Color(0xFF119F50)
private val VerdeClaro = Color(0xFFE8F8EF)
private val Texto: Color @Composable get() = MaterialTheme.colorScheme.onSurface
private val Gris: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val Borde: Color @Composable get() = MaterialTheme.colorScheme.outline

// ==========================================
// MODELO DE MISIÓN
// ==========================================

private data class Mision(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val xp: Int,
    val icono: ImageVector
)

// ==========================================
// CATÁLOGO ORIGINAL DE MISIONES
// ==========================================

private val catalogoMisiones = listOf(
    Mision(
        "primer_ahorro_semanal",
        "Primer Ahorro Semanal",
        "Guarda al menos $5.00 en tu alcancía digital esta semana.",
        250,
        Icons.Filled.Savings
    ),
    Mision(
        "maestro_presupuesto",
        "Maestro de Presupuesto",
        "Completa el módulo educativo sobre presupuestos universitarios.",
        150,
        Icons.Filled.School
    ),
    Mision(
        "inversionista_novato",
        "Inversionista Novato",
        "Aprende sobre inversiones y supera la evaluación.",
        100,
        Icons.Filled.TrendingUp
    ),
    Mision(
        "primer_movimiento",
        "Primer Movimiento",
        "Registra tu primer ingreso o gasto en FinanQuest.",
        50,
        Icons.Filled.AccountBalanceWallet
    ),
    Mision(
        "organizador_financiero",
        "Organizador Financiero",
        "Registra cinco movimientos financieros.",
        100,
        Icons.Filled.ReceiptLong
    ),
    Mision(
        "mi_primera_meta",
        "Mi Primera Meta",
        "Crea tu primera meta de ahorro.",
        100,
        Icons.Filled.Flag
    ),
    Mision(
        "planificador_mes",
        "Planificador del Mes",
        "Crea un presupuesto para organizar tus gastos.",
        150,
        Icons.Filled.CalendarMonth
    ),
    Mision(
        "ahorrador_constante",
        "Ahorrador Constante",
        "Registra ahorros durante tres días diferentes.",
        200,
        Icons.Filled.Savings
    ),
    Mision(
        "explorador_financiero",
        "Explorador Financiero",
        "Completa tres lecciones de educación financiera.",
        200,
        Icons.Filled.MenuBook
    ),
    Mision(
        "experto_finanzas",
        "Experto en Finanzas",
        "Completa cinco misiones verificadas.",
        300,
        Icons.Filled.EmojiEvents
    )
)

// ==========================================
// PANTALLA PRINCIPAL
// ==========================================

@Composable
fun MisionesScreen(
    onVolver: () -> Unit,
    onInicio: () -> Unit = onVolver,
    onPerfil: () -> Unit = {},
    onAbrirLeccionInversiones: () -> Unit = {}
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val firestore = remember {
        FirebaseFirestore.getInstance()
    }

    var busqueda by remember {
        mutableStateOf("")
    }

    var pestana by remember {
        mutableIntStateOf(0)
    }

    var misionActivaId by remember(uid) {
        mutableStateOf<String?>(null)
    }

    var progresos by remember(uid) {
        mutableStateOf<Map<String, Int>>(emptyMap())
    }

    var recompensas by remember(uid) {
        mutableStateOf<Set<String>>(emptySet())
    }

    var cargando by remember(uid) {
        mutableStateOf(uid != null)
    }

    var aviso by remember {
        mutableStateOf<String?>(null)
    }

    var guardando by remember {
        mutableStateOf(false)
    }

    var errorProgreso by remember {
        mutableStateOf<String?>(null)
    }

    var errorRecompensas by remember {
        mutableStateOf(false)
    }

    var detalle by remember {
        mutableStateOf<Mision?>(null)
    }

    // ======================================
    // FIREBASE: ESCUCHAR DATOS REALES
    // ======================================

    DisposableEffect(uid) {
        if (uid == null) {
            cargando = false
            errorProgreso =
                "Inicia sesión para consultar tus misiones."

            onDispose {}
        } else {
            val usuarioRef = firestore
                .collection("usuarios")
                .document(uid)

            val listenerUsuario = usuarioRef
                .addSnapshotListener { snapshot, error ->
                    if (error == null) {
                        misionActivaId =
                            snapshot?.getString("misionActivaId")
                    }
                }

            val listenerProgreso = usuarioRef
                .collection("progreso_misiones")
                .addSnapshotListener { snapshot, error ->
                    cargando = false

                    if (error != null) {
                        errorProgreso =
                            "No se pudo consultar el progreso."
                    } else {
                        errorProgreso = null

                        progresos = snapshot?.documents
                            ?.associate { documento ->
                                documento.id to (
                                        documento.getLong("progreso")
                                            ?.toInt() ?: 0
                                        ).coerceIn(0, 100)
                            } ?: emptyMap()
                    }
                }

            val listenerRecompensas = usuarioRef
                .collection("recompensas_otorgadas")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        errorRecompensas = true
                    } else {
                        errorRecompensas = false

                        recompensas = snapshot?.documents
                            ?.map { it.id }
                            ?.toSet() ?: emptySet()
                    }
                }

            onDispose {
                listenerUsuario.remove()
                listenerProgreso.remove()
                listenerRecompensas.remove()
            }
        }
    }

    // ======================================
    // SELECCIONAR MISIÓN ACTIVA
    // ======================================

    fun seleccionarMision(mision: Mision) {
        if (uid == null) {
            aviso = "Debes iniciar sesión."
            return
        }

        if (guardando) return

        if (
            mision.id in recompensas ||
            (progresos[mision.id] ?: 0) >= 100
        ) {
            aviso = "Esta misión ya está completada."
            return
        }

        guardando = true

        firestore.collection("usuarios")
            .document(uid)
            .update("misionActivaId", mision.id)
            .addOnSuccessListener {
                misionActivaId = mision.id
                guardando = false
                aviso = "¡Misión activa actualizada!"
            }
            .addOnFailureListener { error ->
                guardando = false
                aviso =
                    "No se pudo guardar: ${error.localizedMessage}"
            }
    }

    // ======================================
    // RESUMEN DE MISIONES
    // ======================================

    val completadas = catalogoMisiones.count { mision ->
        (progresos[mision.id] ?: 0) >= 100 ||
                mision.id in recompensas
    }

    val pendientes = catalogoMisiones.size - completadas

    // Suma de XP de misiones con recompensa confirmada.
    val xpConfirmados = catalogoMisiones
        .filter { it.id in recompensas }
        .sumOf { it.xp }

    val misionesFiltradas = catalogoMisiones.filter { mision ->
        val coincide =
            mision.titulo.contains(
                busqueda.trim(),
                ignoreCase = true
            ) ||
                    mision.descripcion.contains(
                        busqueda.trim(),
                        ignoreCase = true
                    )

        val completada =
            (progresos[mision.id] ?: 0) >= 100 ||
                    mision.id in recompensas

        coincide && if (pestana == 0) {
            !completada
        } else {
            completada
        }
    }

    Scaffold(
        containerColor = Fondo,
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(
                            horizontal = 10.dp,
                            vertical = 8.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Texto
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Mis Misiones",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = Texto
                        )

                        Text(
                            "Supera desafíos y gana recompensas",
                            fontSize = 11.sp,
                            color = Gris
                        )
                    }

                    IconButton(onClick = onPerfil) {
                        Icon(
                            Icons.Filled.PersonOutline,
                            contentDescription = "Abrir perfil",
                            tint = Morado
                        )
                    }
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Spacer(Modifier.height(5.dp))

            // ==================================
            // ENCABEZADO CON MASCOTA
            // ==================================

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Verde
                )
            ) {
                Row(
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
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "¡Acepta el reto! ✨",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(Modifier.height(7.dp))

                        Text(
                            "Completa misiones, aprende y gana experiencia.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color.White
                        )

                        Spacer(Modifier.height(10.dp))

                        Surface(
                            color = Color.White.copy(
                                alpha = 0.2f
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "${catalogoMisiones.size} desafíos disponibles",
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 7.dp
                                ),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Image(
                        painter = painterResource(R.drawable.img),
                        contentDescription = "Mascota FinanQuest",
                        modifier = Modifier.size(105.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // ==================================
            // ESTADÍSTICAS
            // ==================================

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResumenMision(
                    titulo = "Pendientes",
                    valor = "$pendientes",
                    icono = Icons.Filled.Flag,
                    color = Morado,
                    fondo = MoradoClaro,
                    modifier = Modifier.weight(1f)
                )

                ResumenMision(
                    titulo = "Completadas",
                    valor = "$completadas",
                    icono = Icons.Filled.CheckCircle,
                    color = Verde,
                    fondo = VerdeClaro,
                    modifier = Modifier.weight(1f)
                )

                ResumenMision(
                    titulo = "XP ganados",
                    valor = "$xpConfirmados",
                    icono = Icons.Filled.EmojiEvents,
                    color = Morado,
                    fondo = MoradoClaro,
                    modifier = Modifier.weight(1f)
                )
            }

            // ==================================
            // BUSCADOR
            // ==================================

            OutlinedTextField(
                value = busqueda,
                onValueChange = {
                    busqueda = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Buscar misiones...")
                },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = Morado
                    )
                },
                trailingIcon = {
                    if (busqueda.isNotBlank()) {
                        IconButton(
                            onClick = {
                                busqueda = ""
                            }
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Limpiar"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Verde,
                    focusedLabelColor = Verde,
                    cursorColor = Verde
                )
            )


            TabRow(
                selectedTabIndex = pestana,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Morado,
                indicator = { posiciones ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(
                            posiciones[pestana]
                        ),
                        color = Morado
                    )
                }
            ) {
                Tab(
                    selected = pestana == 0,
                    onClick = {
                        pestana = 0
                    },
                    text = {
                        Text("Pendientes ($pendientes)")
                    }
                )

                Tab(
                    selected = pestana == 1,
                    onClick = {
                        pestana = 1
                    },
                    text = {
                        Text("Completadas ($completadas)")
                    }
                )
            }

            // ==================================
            // ESTADOS DE FIREBASE
            // ==================================

            if (cargando) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Verde
                )
            }

            errorProgreso?.let { error ->
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            if (errorRecompensas) {
                Text(
                    "No se pudo verificar la entrega de XP.",
                    color = Gris,
                    fontSize = 12.sp
                )
            }

            // ==================================
            // LISTADO DE MISIONES
            // ==================================

            if (!cargando && misionesFiltradas.isEmpty()) {
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
                            if (pestana == 0) {
                                Icons.Filled.Search
                            } else {
                                Icons.Filled.EmojiEvents
                            },
                            contentDescription = null,
                            tint = Morado,
                            modifier = Modifier.size(38.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            if (busqueda.isNotBlank()) {
                                "No encontramos esa misión"
                            } else if (pestana == 0) {
                                "¡No tienes misiones pendientes!"
                            } else {
                                "Aún no hay misiones completadas"
                            },
                            fontWeight = FontWeight.Bold,
                            color = Texto
                        )
                    }
                }
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
                            mision.id == "inversionista_novato" &&
                            progreso < 100 &&
                            !confirmada
                        ) {
                            onAbrirLeccionInversiones()
                        } else {
                            detalle = mision
                        }
                    },
                    onSeleccionar = {
                        seleccionarMision(mision)
                    },
                    guardando = guardando
                )
            }

            // ==================================
            // CONSEJO FINAL
            // ==================================

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MoradoClaro
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = Morado,
                        modifier = Modifier.size(30.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            "¡Cada misión cuenta!",
                            color = Morado,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            "Aprende sobre finanzas, supera desafíos y consigue más XP.",
                            color = Texto,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(90.dp))
        }
    }

    // ======================================
    // DETALLES DE MISIÓN
    // ======================================

    detalle?.let { mision ->
        val progreso = progresos[mision.id] ?: 0
        val confirmada = mision.id in recompensas

        AlertDialog(
            onDismissRequest = {
                detalle = null
            },
            icon = {
                Icon(
                    mision.icono,
                    contentDescription = null,
                    tint = Morado
                )
            },
            title = {
                Text(
                    mision.titulo,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(mision.descripcion)

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Progreso: $progreso%",
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = {
                            progreso.coerceIn(0, 100) / 100f
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = Verde,
                        trackColor = VerdeClaro
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        when {
                            confirmada ->
                                "Recompensa entregada: +${mision.xp} XP"

                            progreso >= 100 ->
                                "Recompensa pendiente de validación"

                            else ->
                                "Recompensa disponible: +${mision.xp} XP"
                        },
                        color = Verde,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        detalle = null
                    }
                ) {
                    Text("Cerrar", color = Morado)
                }
            }
        )
    }

    // ======================================
    // MENSAJES
    // ======================================

    aviso?.let { mensaje ->
        AlertDialog(
            onDismissRequest = {
                aviso = null
            },
            title = {
                Text("FinanQuest SV")
            },
            text = {
                Text(mensaje)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        aviso = null
                    }
                ) {
                    Text("Aceptar", color = Verde)
                }
            }
        )
    }
}

// ==========================================
// TARJETA DE RESUMEN
// ==========================================

@Composable
private fun ResumenMision(
    titulo: String,
    valor: String,
    icono: ImageVector,
    color: Color,
    fondo: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = fondo
        ),
        shape = RoundedCornerShape(17.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 4.dp,
                    vertical = 13.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icono,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(23.dp)
            )

            Text(
                titulo,
                fontSize = 10.sp,
                color = Gris,
                maxLines = 1
            )

            Text(
                valor,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = color
            )
        }
    }
}

// ==========================================
// TARJETA INDIVIDUAL DE MISIÓN
// ==========================================

@Composable
private fun TarjetaMision(
    mision: Mision,
    progreso: Int,
    recompensaConfirmada: Boolean,
    esActiva: Boolean,
    onDetalle: () -> Unit,
    onSeleccionar: () -> Unit,
    guardando: Boolean
) {
    val completada =
        progreso >= 100 || recompensaConfirmada

    val colorPrincipal = if (completada) {
        Verde
    } else {
        Morado
    }

    val fondoIcono = if (completada) {
        VerdeClaro
    } else {
        MoradoClaro
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            if (esActiva && !completada) 1.5.dp else 1.dp,
            if (esActiva && !completada) Morado else Borde
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // INFORMACIÓN PRINCIPAL
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(fondoIcono),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        mision.icono,
                        contentDescription = null,
                        tint = colorPrincipal,
                        modifier = Modifier.size(25.dp)
                    )
                }

                Spacer(Modifier.width(11.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        mision.titulo,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Texto
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        mision.descripcion,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = Gris
                    )
                }
            }

            // RECOMPENSA
            Surface(
                color = VerdeClaro,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 7.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Verde,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        "+${mision.xp} XP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Verde
                    )
                }
            }

            if (esActiva && !completada) {
                Surface(
                    color = MoradoClaro,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "✓ MISIÓN ACTIVA",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        color = Morado,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            if (completada) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Verde,
                        modifier = Modifier.size(19.dp)
                    )

                    Spacer(Modifier.width(7.dp))

                    Text(
                        "Misión completada",
                        color = Verde,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // PROGRESO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "PROGRESO",
                    color = Gris,
                    fontSize = 11.sp
                )

                Text(
                    "$progreso%",
                    color = colorPrincipal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = {
                    progreso.coerceIn(0, 100) / 100f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color = colorPrincipal,
                trackColor = fondoIcono
            )

            Text(
                when {
                    recompensaConfirmada ->
                        "XP registrados correctamente"

                    progreso >= 100 ->
                        "XP pendientes de confirmar"

                    else ->
                        "Recompensa: ${mision.xp} XP"
                },
                color = Gris,
                fontSize = 11.sp
            )

            // BOTÓN PARA SELECCIONAR
            if (!completada) {
                Button(
                    onClick = onSeleccionar,
                    enabled = !guardando && !esActiva,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Verde,
                        disabledContainerColor = VerdeClaro,
                        disabledContentColor = Verde
                    )
                ) {
                    Icon(
                        if (esActiva) {
                            Icons.Filled.CheckCircle
                        } else {
                            Icons.Filled.Flag
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        if (esActiva) {
                            "Misión seleccionada"
                        } else {
                            "Elegir misión activa"
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }

            // DETALLES O LECCIÓN
            TextButton(
                onClick = onDetalle,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    if (
                        mision.id == "inversionista_novato" &&
                        !completada
                    ) {
                        "INICIAR LECCIÓN"
                    } else {
                        "VER DETALLES"
                    },
                    color = Morado,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Spacer(Modifier.width(3.dp))

                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Morado
                )
            }
        }
    }
}
