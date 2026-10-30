package com.finanquest.sv.ui.perfil

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanquest.sv.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File

private val Fondo = Color(0xFFF7F8FA)
private val Verde = Color(0xFF16A34A)
private val Morado = Color(0xFF7C3AED)
private val Rojo = Color(0xFFEF4444)
private val Texto = Color(0xFF22252B)
private val Gris = Color(0xFF7B808A)
private val Borde = Color(0xFFE5E7EB)

@Composable
fun PerfilScreen(
    authViewModel: AuthViewModel,
    onCerrarSesion: () -> Unit,
    onVolver: () -> Unit,
    onInformacionPersonal: () -> Unit = {},
    onSeguridad: () -> Unit = {},
    onMisCuentas: () -> Unit = {},
    onRecompensas: () -> Unit = {},
    onNiveles: () -> Unit = {},
    onMascota: () -> Unit = {},
    onSincronizacion: () -> Unit = {},
    onNotificaciones: () -> Unit = {},
    onCentroAyuda: () -> Unit = {},
    onCalificar: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        authViewModel.cargarPerfil()
    }

    // Fotografía local guardada desde InformacionPersonalScreen.
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val bitmapFoto = remember(uid) {
        if (uid.isBlank()) {
            null
        } else {
            val archivo = File(File(context.filesDir, "fotos_perfil"), "$uid.jpg")
            if (archivo.exists()) {
                try {
                    BitmapFactory.decodeFile(archivo.absolutePath)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    val usuario = authViewModel.usuario
    val nombre = usuario?.nombre ?: "Estudiante"
    val nivel = usuario?.nivel ?: 1
    val xp = usuario?.xp ?: 0
    val universidad = usuario?.universidad
        ?.takeIf { it.isNotBlank() }
        ?: "Universidad no especificada"

    var modoOscuro by remember { mutableStateOf(false) }
    var notificaciones by remember { mutableStateOf(true) }
    var confirmarSalida by remember { mutableStateOf(false) }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Deseas salir de tu cuenta de FinanQuest SV?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    onCerrarSesion()
                }) {
                    Text("Cerrar sesión", color = Rojo)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = Fondo,
        topBar = {
            Surface(color = Fondo, shadowElevation = 1.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(60.dp)
                ) {
                    IconButton(
                        onClick = onVolver,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }

                    Text(
                        text = "Mi Perfil",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Texto,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    IconButton(
                        onClick = onNotificaciones,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            Icons.Filled.NotificationsNone,
                            contentDescription = "Notificaciones"
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
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(105.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEDE9FE)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmapFoto != null) {
                    Image(
                        bitmap = bitmapFoto.asImageBitmap(),
                        contentDescription = "Fotografía de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Avatar del estudiante",
                        tint = Morado,
                        modifier = Modifier.size(65.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text = nombre,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Texto,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = universidad,
                fontSize = 14.sp,
                color = Gris,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(22.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EstadisticaPerfil(
                    icono = Icons.Filled.EmojiEvents,
                    etiqueta = "nivel",
                    valor = nivel.toString(),
                    modifier = Modifier.weight(1f)
                )
                EstadisticaPerfil(
                    icono = Icons.Filled.Bolt,
                    etiqueta = "xp total",
                    valor = "%,d".format(xp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(22.dp))
            Surface(
                onClick = onNiveles,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.Transparent
            ) { ProgresoNivel(xp = xp, nivel = nivel) }
            Spacer(Modifier.height(22.dp))
            MensajeMotivacional()
            Spacer(Modifier.height(28.dp))

            // Nueva sección: el destino se conecta desde AppNavigation.kt.
            TituloSeccion("MIS LOGROS")
            GrupoOpciones {
                OpcionPerfil(
                    icono = Icons.Filled.EmojiEvents,
                    titulo = "Mis niveles",
                    subtitulo = "Tu experiencia y progreso",
                    onClick = onNiveles
                )
                HorizontalDivider(color = Borde)
                OpcionPerfil(
                    icono = Icons.Filled.Pets,
                    titulo = "Mi mascota",
                    subtitulo = "Personaliza tu gatito y sus accesorios",
                    onClick = onMascota
                )
                HorizontalDivider(color = Borde)
                OpcionPerfil(
                    icono = Icons.Filled.EmojiEvents,
                    titulo = "Mis Recompensas",
                    subtitulo = "Insignias, accesorios y fondos",
                    onClick = onRecompensas
                )
            }

            Spacer(Modifier.height(26.dp))
            TituloSeccion("CUENTA Y SEGURIDAD")
            GrupoOpciones {
                OpcionPerfil(
                    icono = Icons.Filled.PersonOutline,
                    titulo = "Información Personal",
                    subtitulo = "Nombre, correo y universidad",
                    onClick = onInformacionPersonal
                )
                HorizontalDivider(color = Borde)
                OpcionPerfil(
                    icono = Icons.Filled.Security,
                    titulo = "Seguridad",
                    subtitulo = "Contraseña y validaciones",
                    onClick = onSeguridad
                )
                HorizontalDivider(color = Borde)
                OpcionPerfil(
                    icono = Icons.Filled.AccountBalanceWallet,
                    titulo = "Mis Cuentas",
                    subtitulo = "Bancos y carteras",
                    onClick = onMisCuentas
                )
                HorizontalDivider(color = Borde)
                OpcionPerfil(
                    icono = Icons.Filled.Storage,
                    titulo = "Sincronización",
                    subtitulo = "Estado de la base de datos",
                    onClick = onSincronizacion
                )
            }

            Spacer(Modifier.height(26.dp))
            TituloSeccion("PREFERENCIAS")
            GrupoOpciones {
                OpcionPerfil(
                    icono = Icons.Filled.NotificationsNone,
                    titulo = "Notificaciones",
                    onClick = { notificaciones = !notificaciones },
                    contenidoFinal = {
                        Switch(
                            checked = notificaciones,
                            onCheckedChange = { notificaciones = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Verde)
                        )
                    }
                )
                HorizontalDivider(color = Borde)
                OpcionPerfil(
                    icono = Icons.Filled.DarkMode,
                    titulo = "Modo Oscuro",
                    onClick = { modoOscuro = !modoOscuro },
                    contenidoFinal = {
                        Switch(
                            checked = modoOscuro,
                            onCheckedChange = { modoOscuro = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Morado)
                        )
                    }
                )
                HorizontalDivider(color = Borde)
                OpcionPerfil(
                    icono = Icons.Filled.Language,
                    titulo = "Idioma",
                    subtitulo = "Español",
                    onClick = {}
                )
            }

            Spacer(Modifier.height(26.dp))
            TituloSeccion("AYUDA Y SOPORTE")
            GrupoOpciones {
                OpcionPerfil(
                    icono = Icons.Filled.HelpOutline,
                    titulo = "Centro de Ayuda",
                    onClick = onCentroAyuda
                )
                HorizontalDivider(color = Borde)
                OpcionPerfil(
                    icono = Icons.Filled.StarBorder,
                    titulo = "Calificar Aplicación",
                    onClick = onCalificar
                )
            }

            Spacer(Modifier.height(30.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { confirmarSalida = true },
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = null, tint = Rojo)
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = "Cerrar Sesión",
                        color = Rojo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Gris)
                }
            }

            Spacer(Modifier.height(28.dp))
            Text("FinanQuest SV · Versión Beta", color = Gris, fontSize = 12.sp)
            Spacer(Modifier.height(7.dp))
            Text("HECHO PARA ESTUDIANTES CON 💚", color = Gris, fontSize = 10.sp)
            Spacer(Modifier.height(35.dp))
        }
    }
}

@Composable
private fun EstadisticaPerfil(
    icono: ImageVector,
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(18.dp),
        color = Fondo,
        border = BorderStroke(1.dp, Borde),
        shadowElevation = 2.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icono, contentDescription = null, tint = Texto)
            Spacer(Modifier.height(8.dp))
            Text(etiqueta, color = Gris, fontSize = 14.sp)
            Spacer(Modifier.height(5.dp))
            Text(valor, color = Texto, fontWeight = FontWeight.Bold, fontSize = 21.sp)
        }
    }
}

@Composable
private fun ProgresoNivel(xp: Int, nivel: Int) {
    // Coincide con XP_POR_NIVEL = 100 en server.js.
    val xpPorNivel = 100
    val xpNivel = xp % xpPorNivel
    val progreso = xpNivel.toFloat() / xpPorNivel

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Fondo,
        border = BorderStroke(1.dp, Borde)
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Text("PROGRESO DE NIVEL", fontSize = 12.sp, color = Gris)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Siguiente Nivel: ${nivel + 1}",
                    fontWeight = FontWeight.Bold,
                    color = Texto,
                    fontSize = 15.sp
                )
                Text(
                    "$xpNivel / $xpPorNivel XP",
                    color = Verde,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = Verde,
                trackColor = Borde
            )
        }
    }
}

@Composable
private fun MensajeMotivacional() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF5F3FF),
        border = BorderStroke(1.dp, Color(0xFFD8CCFF))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🐱", fontSize = 49.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "¡Vas por buen camino!",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Texto
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    "¡Sigue completando misiones para ganar XP y mejorar tus finanzas!",
                    fontSize = 12.sp,
                    color = Texto,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun TituloSeccion(titulo: String) {
    Text(
        text = titulo,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 7.dp, bottom = 12.dp),
        color = Gris,
        fontSize = 12.sp
    )
}

@Composable
private fun GrupoOpciones(contenido: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(19.dp),
        color = Fondo,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 15.dp), content = contenido)
    }
}

@Composable
private fun OpcionPerfil(
    icono: ImageVector,
    titulo: String,
    subtitulo: String? = null,
    onClick: () -> Unit,
    contenidoFinal: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(39.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0xFFF0F1F3)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = Gris,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Texto
            )
            if (subtitulo != null) {
                Spacer(Modifier.height(3.dp))
                Text(subtitulo, fontSize = 12.sp, color = Gris)
            }
        }
        if (contenidoFinal != null) {
            contenidoFinal()
        } else {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Gris
            )
        }
    }
}
