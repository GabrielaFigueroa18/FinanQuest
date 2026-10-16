
package com.finanquest.sv.ui.perfil

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

private val VerdeSeguridad = Color(0xFF16A34A)
private val MoradoSeguridad = Color(0xFF7C3AED)
private val FondoSeguridad = Color(0xFFF8FAFC)
private val TextoSeguridad = Color(0xFF172033)
private val GrisSeguridad = Color(0xFF64748B)

@Composable
fun SeguridadScreen(
    onVolver: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val usuario = auth.currentUser

    var correoVerificado by remember {
        mutableStateOf(usuario?.isEmailVerified == true)
    }

    var cargando by remember {
        mutableStateOf(false)
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    var mostrarDialogo by remember {
        mutableStateOf(false)
    }

    var mostrarInfoBiometrica by remember {
        mutableStateOf(false)
    }

    Scaffold(
        containerColor = FondoSeguridad,
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(60.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }

                    Text(
                        text = "Seguridad de la cuenta",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoSeguridad
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFFE9F8EF)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = VerdeSeguridad,
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Protege tu cuenta",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoSeguridad
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Administra la seguridad de tu cuenta de FinanQuest SV.",
                        fontSize = 13.sp,
                        color = GrisSeguridad
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "SEGURIDAD Y ACCESO",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GrisSeguridad
            )

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(
                    1.dp,
                    Color(0xFFE5E7EB)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 15.dp)
                ) {

                    OpcionSeguridad(
                        icono = Icons.Filled.Lock,
                        titulo = "Cambiar contraseña",
                        subtitulo = "Recibe un enlace seguro en tu correo",
                        onClick = {
                            mostrarDialogo = true
                        }
                    )

                    HorizontalDivider(
                        color = Color(0xFFE5E7EB)
                    )

                    OpcionSeguridad(
                        icono = Icons.Filled.MarkEmailRead,
                        titulo = "Verificación de correo",
                        subtitulo = if (correoVerificado)
                            "Tu correo está verificado"
                        else
                            "Tu correo todavía no está verificado",
                        onClick = {
                            cargando = true
                            mensaje = ""

                            val usuarioActual = auth.currentUser

                            if (usuarioActual == null) {
                                cargando = false
                                mensaje = "Inicia sesión nuevamente"
                            } else {
                                usuarioActual.reload()
                                    .addOnSuccessListener {
                                        correoVerificado =
                                            auth.currentUser
                                                ?.isEmailVerified == true

                                        cargando = false

                                        mensaje = if (correoVerificado)
                                            "Tu correo está verificado"
                                        else
                                            "Tu correo aún no está verificado"
                                    }
                                    .addOnFailureListener { error ->
                                        cargando = false
                                        mensaje = error.localizedMessage
                                            ?: "No se pudo verificar el correo"
                                    }
                            }
                        }
                    )

                    HorizontalDivider(
                        color = Color(0xFFE5E7EB)
                    )

                    OpcionSeguridad(
                        icono = Icons.Filled.Fingerprint,
                        titulo = "Acceso biométrico",
                        subtitulo = "Próximamente",
                        onClick = {
                            mostrarInfoBiometrica = true
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "VERIFICACIÓN DE CORREO",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GrisSeguridad
            )

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (correoVerificado)
                                Icons.Filled.CheckCircle
                            else
                                Icons.Filled.Info,
                            contentDescription = null,
                            tint = if (correoVerificado)
                                VerdeSeguridad
                            else
                                MoradoSeguridad
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = if (correoVerificado)
                                "Correo verificado"
                            else
                                "Verificación pendiente",
                            fontWeight = FontWeight.Bold,
                            color = TextoSeguridad
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = usuario?.email
                            ?: "Correo no disponible",
                        fontSize = 13.sp,
                        color = GrisSeguridad
                    )

                    if (!correoVerificado) {
                        Spacer(Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                val usuarioActual = auth.currentUser

                                if (usuarioActual == null) {
                                    mensaje = "Inicia sesión nuevamente"
                                } else {
                                    cargando = true
                                    mensaje = ""

                                    usuarioActual
                                        .sendEmailVerification()
                                        .addOnSuccessListener {
                                            cargando = false
                                            mensaje =
                                                "Enviamos un enlace de verificación a tu correo"
                                        }
                                        .addOnFailureListener { error ->
                                            cargando = false
                                            mensaje = error.localizedMessage
                                                ?: "No se pudo enviar el correo"
                                        }
                                }
                            },
                            enabled = !cargando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enviar correo de verificación")
                        }
                    }
                }
            }

            if (cargando) {
                Spacer(Modifier.height(18.dp))

                CircularProgressIndicator(
                    color = VerdeSeguridad,
                    modifier = Modifier.align(
                        Alignment.CenterHorizontally
                    )
                )
            }

            if (mensaje.isNotBlank()) {
                Spacer(Modifier.height(18.dp))

                Surface(
                    color = Color(0xFFF5F3FF),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = mensaje,
                        modifier = Modifier.padding(15.dp),
                        fontSize = 13.sp,
                        color = TextoSeguridad
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "FinanQuest SV · Seguridad de la cuenta",
                fontSize = 12.sp,
                color = GrisSeguridad,
                modifier = Modifier.align(
                    Alignment.CenterHorizontally
                )
            )
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogo = false
            },
            icon = {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = VerdeSeguridad
                )
            },
            title = {
                Text("Cambiar contraseña")
            },
            text = {
                Text(
                    "Te enviaremos un enlace al correo asociado " +
                            "a tu cuenta para restablecer tu contraseña."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogo = false

                        val correo = auth.currentUser?.email

                        if (correo.isNullOrBlank()) {
                            mensaje = "No encontramos tu correo"
                        } else {
                            cargando = true
                            mensaje = ""

                            auth.sendPasswordResetEmail(correo)
                                .addOnSuccessListener {
                                    cargando = false
                                    mensaje =
                                        "Si el correo es válido, recibirás un enlace para cambiar tu contraseña"
                                }
                                .addOnFailureListener { error ->
                                    cargando = false
                                    mensaje = error.localizedMessage
                                        ?: "No se pudo enviar el correo"
                                }
                        }
                    }
                ) {
                    Text("Enviar enlace")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogo = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarInfoBiometrica) {
        AlertDialog(
            onDismissRequest = {
                mostrarInfoBiometrica = false
            },
            title = {
                Text("Acceso biométrico")
            },
            text = {
                Text(
                    "Esta función estará disponible en una próxima versión de FinanQuest SV."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarInfoBiometrica = false
                    }
                ) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
private fun OpcionSeguridad(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(43.dp)
                .background(
                    Color(0xFFF1F5F9),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = GrisSeguridad
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = titulo,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoSeguridad
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = subtitulo,
                fontSize = 12.sp,
                color = GrisSeguridad
            )
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = GrisSeguridad
        )
    }
}
